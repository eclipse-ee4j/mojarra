/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.faces.cdi;

import static java.util.stream.Collectors.toSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.faces.component.behavior.Behavior;
import jakarta.faces.component.behavior.BehaviorBase;
import jakarta.faces.component.behavior.FacesBehavior;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Named;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * Manual performance harness comparing the pre-cache CDI bean-resolution path (raw
 * {@link BeanManager#getBeans}/{@code resolve}/{@code getReference}) against the
 * post-cache {@link CdiUtils} API for every hot spot touched on this branch.
 *
 * <p>Disabled by default. To run: {@code mvn -pl impl test -Dperf=true -Dtest=CdiLookupPerfHarness}.
 * Output goes to stdout; capture and paste into a JIRA comment or PR description.
 *
 * <p>Each scenario runs both paths inside the same Weld SE container so Weld's internal
 * caches are equally warm. Warm-up is performed before measurement; reported numbers are
 * the median of {@value #RUNS} runs of {@value #ITERATIONS} iterations each.
 *
 * <p>For positive cases ({@code testConverter}, {@code testValidator}, {@code testBehavior})
 * a stub bean is registered with the matching {@code managed=true} qualifier. For the
 * {@link FacesContext} producer destroy path, an extension registers a stub bean with both
 * {@link FacesContext} and {@link FacesContextProducer} in its bean types.
 */
@EnabledIfSystemProperty(named = "perf", matches = "true")
public class CdiLookupPerfHarness {

    private static final int WARMUP_ITERATIONS = 100_000;
    private static final int ITERATIONS = 1_000_000;
    private static final int RUNS = 5;

    private static final Type CONVERTER_TYPE = new TypeLiteral<Converter<?>>() {
        private static final long serialVersionUID = 1L;
    }.getType();

    private static final Type VALIDATOR_TYPE = new TypeLiteral<Validator<?>>() {
        private static final long serialVersionUID = 1L;
    }.getType();

    private static WeldContainer container;
    private static BeanManager beanManager;

    @BeforeAll
    static void init() {
        container = new Weld()
                .disableDiscovery()
                .beanClasses(TestConverter.class, TestValidator.class, TestBehavior.class, TestDataModelClassesMap.class)
                .addExtension(new StubFacesContextProducerExtension())
                .initialize();
        beanManager = container.getBeanManager();
        System.out.println();
        System.out.println("CdiLookupPerfHarness (warmup=" + WARMUP_ITERATIONS + ", iterations=" + ITERATIONS + ", runs=" + RUNS + ")");
        System.out.println();
        System.out.printf("%-55s %14s %14s %10s%n", "Scenario", "raw BM ns/op", "cached ns/op", "speedup");
        System.out.printf("%-55s %14s %14s %10s%n", "-".repeat(55), "-".repeat(14), "-".repeat(14), "-".repeat(10));
    }

    @AfterAll
    static void shutdown() {
        if (container != null) {
            container.shutdown();
        }
    }

    @Test
    void createConverter_byId_unknown() {
        compare("createConverter(String) -- unknown ID",
                () -> rawCreateConverterById(beanManager, "jakarta.faces.Integer"),
                () -> CdiUtils.createConverter(beanManager, "jakarta.faces.Integer"));
    }

    @Test
    void createConverter_byId_managed() {
        // Measures the BM-lookup portion via getBeanReference (the wrapped CdiUtils.createConverter
        // would also fire Mojarra's ApplicationAssociate annotation post-processing, which has no
        // ApplicationAssociate in standalone Weld; that's downstream of the cache anyway).
        FacesConverter qualifier = FacesConverter.Literal.of("testConverter", Object.class, true);
        compare("getBeanReference(Converter.class, qualifier) -- managed match",
                () -> rawGetBeanReference(beanManager, Converter.class, qualifier),
                () -> CdiUtils.getBeanReference(beanManager, Converter.class, qualifier));
    }

    @Test
    void createConverter_byClass_unknown() {
        compare("createConverter(Class) -- Long (no match, walks Long->Number)",
                () -> rawCreateConverterByClass(beanManager, Long.class),
                () -> CdiUtils.createConverter(beanManager, Long.class));
    }

    @Test
    void createValidator_byId_unknown() {
        compare("createValidator(String) -- unknown ID",
                () -> rawCreateValidator(beanManager, "jakarta.faces.Required"),
                () -> CdiUtils.createValidator(beanManager, "jakarta.faces.Required"));
    }

    @Test
    void createValidator_byId_managed() {
        compare("createValidator(String) -- managed CDI validator",
                () -> rawCreateValidator(beanManager, "testValidator"),
                () -> CdiUtils.createValidator(beanManager, "testValidator"));
    }

    @Test
    void createBehavior_byId_unknown() {
        compare("createBehavior(String) -- unknown ID",
                () -> rawCreateBehavior(beanManager, "jakarta.faces.Ajax"),
                () -> CdiUtils.createBehavior(beanManager, "jakarta.faces.Ajax"));
    }

    @Test
    void createBehavior_byId_managed() {
        compare("createBehavior(String) -- managed CDI behavior",
                () -> rawCreateBehavior(beanManager, "testBehavior"),
                () -> CdiUtils.createBehavior(beanManager, "testBehavior"));
    }

    @Test
    void resolveBeanByName_dataModelClassesMap() {
        compare("resolveBeanByName -- comSunFacesDataModelClassesMap",
                () -> rawResolveBeanByName(beanManager, "comSunFacesDataModelClassesMap"),
                () -> CdiUtils.resolveBeanByName(beanManager, "comSunFacesDataModelClassesMap"));
    }

    @Test
    void resolveFacesContextProducerBean() {
        compare("resolveFacesContextProducerBean -- per request.release()",
                () -> rawResolveFacesContextProducerBean(beanManager),
                () -> CdiUtils.resolveFacesContextProducerBean(beanManager));
    }

    @Test
    void getCurrentInjectionPoint_path() {
        // getCurrentInjectionPoint requires a CreationalContext supplied by a producer;
        // we measure the bean-resolution portion only, which is what the cache fix targets.
        compare("resolveBean(InjectionPoint.class) -- producer hot path",
                () -> rawResolveBeanByType(beanManager, InjectionPoint.class),
                () -> CdiUtils.resolveBean(beanManager, InjectionPoint.class));
    }

    private static void compare(String label, Runnable rawPath, Runnable cachedPath) {
        warmUp(rawPath);
        warmUp(cachedPath);
        long rawMedian = medianRun(rawPath);
        long cachedMedian = medianRun(cachedPath);
        double speedup = rawMedian == 0 ? 0.0 : (double) rawMedian / Math.max(cachedMedian, 1);
        System.out.printf("%-55s %14d %14d %9.1fx%n", label, rawMedian, cachedMedian, speedup);
    }

    private static void warmUp(Runnable r) {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            r.run();
        }
    }

    private static long medianRun(Runnable r) {
        long[] times = new long[RUNS];
        for (int run = 0; run < RUNS; run++) {
            long t0 = System.nanoTime();
            for (int i = 0; i < ITERATIONS; i++) {
                r.run();
            }
            times[run] = (System.nanoTime() - t0) / ITERATIONS;
        }
        Arrays.sort(times);
        return times[RUNS / 2];
    }

    // --- Pre-cache reproductions of the patched CdiUtils methods ---------------------
    //
    // Each method below replicates exactly what the unpatched code did, by going
    // straight to the BeanManager API without consulting the new CdiUtils cache.

    private static Object rawCreateConverterById(BeanManager bm, String value) {
        FacesConverter qualifier = FacesConverter.Literal.of(value, Object.class, true);
        Object ref = rawGetBeanReference(bm, CONVERTER_TYPE, qualifier);
        if (ref == null) {
            ref = rawGetBeanReference(bm, Converter.class, qualifier);
        }
        return ref;
    }

    private static Object rawCreateConverterByClass(BeanManager bm, Class<?> forClass) {
        Object ref = null;
        for (Class<?> c = forClass; ref == null && c != null && c != Object.class; c = c.getSuperclass()) {
            FacesConverter qualifier = FacesConverter.Literal.of("", c, true);
            ref = rawGetBeanReference(bm, CONVERTER_TYPE, qualifier);
            if (ref == null) {
                ref = rawGetBeanReference(bm, Converter.class, qualifier);
            }
        }
        return ref;
    }

    private static Object rawCreateValidator(BeanManager bm, String value) {
        FacesValidator qualifier = FacesValidator.Literal.of(value, false, true);
        Object ref = rawGetBeanReference(bm, VALIDATOR_TYPE, qualifier);
        if (ref == null) {
            ref = rawGetBeanReference(bm, Validator.class, qualifier);
        }
        if (ref == null) {
            FacesValidator defaultQualifier = FacesValidator.Literal.of("", false, true);
            ref = rawGetBeanReferenceFilteredByName(bm, VALIDATOR_TYPE, value, defaultQualifier);
        }
        if (ref == null) {
            FacesValidator defaultQualifier = FacesValidator.Literal.of("", false, true);
            ref = rawGetBeanReferenceFilteredByName(bm, Validator.class, value, defaultQualifier);
        }
        return ref;
    }

    private static Object rawCreateBehavior(BeanManager bm, String value) {
        return rawGetBeanReference(bm, Behavior.class, FacesBehavior.Literal.of(value, true));
    }

    private static Bean<?> rawResolveBeanByName(BeanManager bm, String name) {
        Set<Bean<?>> beans = bm.getBeans(name);
        return bm.resolve(beans);
    }

    private static Bean<?> rawResolveBeanByType(BeanManager bm, Type type) {
        Set<Bean<?>> beans = bm.getBeans(type);
        return bm.resolve(beans);
    }

    private static Bean<?> rawResolveFacesContextProducerBean(BeanManager bm) {
        Set<Bean<?>> beans = bm.getBeans(FacesContext.class).stream()
                .filter(bean -> bean.getTypes().contains(FacesContextProducer.class))
                .collect(toSet());
        return bm.resolve(beans);
    }

    private static Object rawGetBeanReference(BeanManager bm, Type type, Annotation... qualifiers) {
        Set<Bean<?>> beans = bm.getBeans(type, qualifiers);
        Bean<?> bean = bm.resolve(beans);
        if (bean == null) {
            return null;
        }
        return bm.getReference(bean, type, bm.createCreationalContext(bean));
    }

    private static Object rawGetBeanReferenceFilteredByName(BeanManager bm, Type type, String beanName, Annotation... qualifiers) {
        Set<Bean<?>> beans = bm.getBeans(type, qualifiers).stream()
                .filter(bean -> beanName.equals(getBeanName(bean)))
                .collect(toSet());
        Bean<?> bean = bm.resolve(beans);
        if (bean == null) {
            return null;
        }
        return bm.getReference(bean, type, bm.createCreationalContext(bean));
    }

    private static String getBeanName(Bean<?> bean) {
        String name = bean.getName();
        if (name != null) {
            return name;
        }
        String className = bean.getBeanClass().getSimpleName();
        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    // --- Test stub beans -------------------------------------------------------------

    @FacesConverter(value = "testConverter", managed = true)
    @Dependent
    public static class TestConverter implements Converter<Object> {
        @Override
        public Object getAsObject(FacesContext context, jakarta.faces.component.UIComponent component, String value) throws ConverterException {
            return value;
        }

        @Override
        public String getAsString(FacesContext context, jakarta.faces.component.UIComponent component, Object value) throws ConverterException {
            return value == null ? "" : value.toString();
        }
    }

    @FacesValidator(value = "testValidator", managed = true)
    @Dependent
    public static class TestValidator implements Validator<Object> {
        @Override
        public void validate(FacesContext context, jakarta.faces.component.UIComponent component, Object value) throws ValidatorException {
            // no-op
        }
    }

    @FacesBehavior(value = "testBehavior", managed = true)
    @Dependent
    public static class TestBehavior extends BehaviorBase {
    }

    @Named("comSunFacesDataModelClassesMap")
    @ApplicationScoped
    public static class TestDataModelClassesMap {
        public java.util.Map<Class<?>, Class<?>> getMap() {
            return Collections.emptyMap();
        }
    }

    /**
     * Registers a stub {@link Bean} whose bean types include both {@link FacesContext} and
     * {@link FacesContextProducer}, so {@link CdiUtils#resolveFacesContextProducerBean}
     * (and its uncached counterpart {@link #rawResolveFacesContextProducerBean}) find a
     * match instead of returning null. Mirrors how Mojarra's real
     * {@link com.sun.faces.cdi.CdiExtension} registers the FacesContext producer at runtime.
     */
    public static class StubFacesContextProducerExtension implements Extension {
        void addStubBean(@Observes AfterBeanDiscovery event, BeanManager bm) {
            event.addBean()
                    .types(FacesContext.class, FacesContextProducer.class, Object.class)
                    .scope(ApplicationScoped.class)
                    .createWith(ctx -> null)
                    .destroyWith((instance, ctx) -> { /* no-op */ });
        }
    }
}
