/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.mojarra.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.Behavior;
import jakarta.faces.component.behavior.BehaviorBase;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.Facelet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Measures the number of {@link jakarta.enterprise.inject.spi.BeanManager} lookups that {@link CdiUtils} performs per
 * {@code createConverter}/{@code createValidator}/{@code createBehavior} call when no matching CDI bean is registered &mdash; the common case for built-in
 * Faces IDs such as {@code jakarta.faces.Integer} or {@code jakarta.faces.Required}.
 *
 * <p>
 * Each {@code beanManager.getBeans(type, qualifiers)} call forces the CDI implementation (typically Weld) to walk the bean registry and match types +
 * qualifiers. The numbers below quantify the per-call cost and act as a regression guard against an uncached lookup path.
 */
public class CdiLookupCountTest {

    private TestApplicationScope application;

    @BeforeEach
    public void deploy() {
        application = TestApplicationScope.deploy();
    }

    @AfterEach
    public void undeploy() {
        application.undeploy();
        TestApplicationScope.clearThreadState();
    }

    @Test
    public void createConverter_byId_unknown_performsTwoLookups() {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();

        assertNull(CdiUtils.createConverter(counter.beanManager, "jakarta.faces.Integer"));

        // 2 type attempts (parameterized Converter<?>, raw Converter).
        assertEquals(2, counter.getBeansByType.get(), "getBeans(Type, qualifiers) calls per createConverter(String)");
        assertEquals(2, counter.resolves.get(), "resolve(Set) calls per createConverter(String)");
    }

    @Test
    public void createConverter_byId_repeatedCalls_areCached() {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();

        for (int i = 0; i < 10; i++) {
            CdiUtils.createConverter(counter.beanManager, "jakarta.faces.Integer");
        }

        // Only the first call walks the BeanManager; the other 9 hit the resolution cache.
        // Without caching this would be 20 (2 x 10) -- guards against regression to the uncached path.
        assertEquals(2, counter.getBeansByType.get(), "Only first createConverter(String) call hits BeanManager");
    }

    @Test
    public void createValidator_byId_unknown_performsFourLookups() {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();

        assertNull(CdiUtils.createValidator(counter.beanManager, "jakarta.faces.Required"));

        // 2 (parameterized + raw with original qualifier) + 2 (parameterized + raw with beanName fallback).
        assertEquals(4, counter.getBeansByType.get(), "getBeans(Type, qualifiers) calls per createValidator(String)");
        assertEquals(4, counter.resolves.get(), "resolve(Set) calls per createValidator(String)");
    }

    @Test
    public void createBehavior_byId_unknown_performsOneLookup() {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();

        assertNull(CdiUtils.createBehavior(counter.beanManager, "jakarta.faces.Ajax"));

        // Raw Behavior type.
        assertEquals(1, counter.getBeansByType.get(), "getBeans(Type, qualifiers) calls per createBehavior(String)");
        assertEquals(1, counter.resolves.get(), "resolve(Set) calls per createBehavior(String)");
    }

    @Test
    public void createConverter_byClass_walksSuperclassChain() {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();

        // java.lang.Long -> java.lang.Number -> (stops at Object): 2 hops.
        // Each hop: 1 qualifier x 2 type attempts (parameterized + raw) = 2 lookups.
        assertNull(CdiUtils.createConverter(counter.beanManager, Long.class));

        assertEquals(4, counter.getBeansByType.get(), "getBeans calls per createConverter(Class) for Long");
    }

    @Test
    public void createConverter_byClass_repeatedCalls_areCached() {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();

        for (int i = 0; i < 10; i++) {
            CdiUtils.createConverter(counter.beanManager, Long.class);
        }

        // Only the first call walks Long -> Number (4 lookups); the other 9 hit the class-keyed cache.
        // This is the per-cell render path (renderer calls createConverter(value.getClass()) every render).
        assertEquals(4, counter.getBeansByType.get(), "Only first createConverter(Class) call hits BeanManager");
    }

    @Test
    public void createValidator_byId_repeatedCalls_areCached() {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();

        for (int i = 0; i < 10; i++) {
            CdiUtils.createValidator(counter.beanManager, "jakarta.faces.Required");
        }

        assertEquals(4, counter.getBeansByType.get(), "Only first createValidator(String) call hits BeanManager");
    }

    @Test
    public void createBehavior_byId_repeatedCalls_areCached() {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();

        for (int i = 0; i < 10; i++) {
            CdiUtils.createBehavior(counter.beanManager, "jakarta.faces.Ajax");
        }

        assertEquals(1, counter.getBeansByType.get(), "Only first createBehavior(String) call hits BeanManager");
    }

    /**
     * Simulates a typical render of one input bound to an Integer property: the renderer calls {@code application.createConverter(Integer.class)} per render,
     * plus {@code application.createConverter("jakarta.faces.Integer")} when the component declares {@code converter="jakarta.faces.Integer"}. Together this is
     * what a single component costs.
     */
    @Test
    public void typicalIntegerInputRender_costPerComponent() {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();

        CdiUtils.createConverter(counter.beanManager, Integer.class); // by-class walk (Integer -> Number) = 4
        CdiUtils.createConverter(counter.beanManager, "jakarta.faces.Integer"); // by-id = 2

        assertEquals(6, counter.getBeansByType.get(), "Total getBeans calls for one Integer input component");
    }

    @Test
    public void getViewFacelet_unknown_performsOneLookup() {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();

        assertNull(CdiUtils.getViewFacelet(counter.beanManager, "/index.xhtml"));

        assertEquals(1, counter.getBeansByType.get(), "getBeans(Type, qualifiers) calls per getViewFacelet");
        assertEquals(1, counter.resolves.get(), "resolve(Set) calls per getViewFacelet");
    }

    @Test
    public void getViewFacelet_repeatedCalls_areCached() {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();

        for (int i = 0; i < 10; i++) {
            CdiUtils.getViewFacelet(counter.beanManager, "/index.xhtml");
        }

        // Every request looks up the facelet for the view it renders -- twice on a postback, once for the
        // metadata facelet and once for the build. Only the first call may walk the BeanManager.
        assertEquals(1, counter.getBeansByType.get(), "Only first getViewFacelet call hits BeanManager");
    }

    /**
     * A {@link jakarta.faces.annotation.View} facelet may be request scoped, so caching the resolution must not cache the instance: the reference is obtained
     * per call and its scope decides what that yields.
     */
    @Test
    public void getViewFacelet_matchingBean_resolutionCachedButReferenceCalledEveryTime() {
        final Facelet dummyFacelet = new Facelet() {

            @Override
            public void apply(FacesContext facesContext, UIComponent parent) {
            }

        };
        final Bean<Facelet> matchingBean = new StubBean<>(dummyFacelet);
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter(matchingBean, dummyFacelet);

        for (int i = 0; i < 10; i++) {
            assertSame(dummyFacelet, CdiUtils.getViewFacelet(counter.beanManager, "/index.xhtml"));
        }

        assertEquals(1, counter.getBeansByType.get(), "Resolution cached after first hit");
        assertEquals(10, counter.references.get(), "getReference invoked per call to preserve scope semantics");
    }

    @Test
    public void getViewFacelet_distinctViewIds_resolveIndependently() {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();

        CdiUtils.getViewFacelet(counter.beanManager, "/index.xhtml");
        CdiUtils.getViewFacelet(counter.beanManager, "/other.xhtml");
        CdiUtils.getViewFacelet(counter.beanManager, "/index.xhtml");

        assertEquals(2, counter.getBeansByType.get(), "Each view id resolves once; a view id is never conflated with another");
    }

    @Test
    public void resolveBeanByName_repeatedCalls_areCached() {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();

        for (int i = 0; i < 10; i++) {
            CdiUtils.resolveBeanByName(counter.beanManager, DataModelClassesMapProducer.DATA_MODEL_CLASSES_MAP_BEAN_NAME);
        }

        // Name-based lookups use a different BeanManager API (getBeans(String) vs getBeans(Type, ...))
        // so the test exposes a separate counter. First call probes the registry; rest hit the cache.
        assertEquals(1, counter.getBeansByName.get(), "Only first resolveBeanByName call hits BeanManager");
    }

    @Test
    public void resolveBean_byType_repeatedCalls_areCached() {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();

        for (int i = 0; i < 10; i++) {
            CdiUtils.resolveBean(counter.beanManager, InjectionPoint.class);
        }

        assertEquals(1, counter.getBeansByType.get(), "Only first resolveBean(Type) call hits BeanManager");
    }

    @Test
    public void resolveFacesContextProducerBean_repeatedCalls_areCached() {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();

        for (int i = 0; i < 10; i++) {
            CdiUtils.resolveFacesContextProducerBean(counter.beanManager);
        }

        // Without caching the type-filtered FacesContextProducer lookup runs on every request release.
        assertEquals(1, counter.getBeansByType.get(), "FacesContextProducer bean lookup runs once per BeanManager");
    }

    @Test
    public void createConverter_byId_distinctBeanManagers_haveIndependentCaches() {
        BeanManagerLookupCounter counter1 = new BeanManagerLookupCounter();
        BeanManagerLookupCounter counter2 = new BeanManagerLookupCounter();

        CdiUtils.createConverter(counter1.beanManager, "jakarta.faces.Integer");
        CdiUtils.createConverter(counter2.beanManager, "jakarta.faces.Integer");

        assertEquals(2, counter1.getBeansByType.get(), "Each BeanManager has its own cache");
        assertEquals(2, counter2.getBeansByType.get(), "Each BeanManager has its own cache");
    }

    @Test
    public void createConverter_byId_concurrent_isThreadSafeAndCached() throws Exception {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();

        int threads = 10;
        int callsPerThread = 100;
        Thread[] workers = new Thread[threads];
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                application.makeCurrent(); // Every request thread serves this application.
                for (int i = 0; i < callsPerThread; i++) {
                    CdiUtils.createConverter(counter.beanManager, "jakarta.faces.Integer");
                }
            });
        }
        for (Thread w : workers) {
            w.start();
        }
        for (Thread w : workers) {
            w.join();
        }

        // First N racing threads may all miss the cache before any of them writes; bound the total.
        // Strict upper bound = threads x 2 (each thread loses every race). Realistic: <= 2 + a few.
        int total = counter.getBeansByType.get();
        assertTrue(
            total >= 2 && total <= threads * 2,
            "getBeans calls under contention should land between 2 and " + (threads * 2) + " but was " + total
        );
    }

    /**
     * Even when a bean does match, the resolution is cached. Only {@code getReference} is called per invocation &mdash; that's intentional, because it produces
     * correctly-scoped references via a fresh {@link CreationalContext} on every call.
     */
    @Test
    public void createBehavior_byId_matchingBean_resolutionCachedButReferenceCalledEveryTime() {
        final Behavior dummyBehavior = new BehaviorBase() {
        };
        final Bean<Behavior> matchingBean = new StubBean<>(dummyBehavior);
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter(matchingBean, dummyBehavior);

        for (int i = 0; i < 10; i++) {
            CdiUtils.createBehavior(counter.beanManager, "jakarta.faces.Ajax");
        }

        // First call resolves; subsequent 9 hit the cache. Total: 1 getBeans + 1 resolve, 10 getReferences.
        assertEquals(1, counter.getBeansByType.get(), "Resolution cached after first hit");
        assertEquals(1, counter.resolves.get(), "Resolution cached after first hit");
        assertEquals(10, counter.references.get(), "getReference invoked per call to preserve scope semantics");
    }

    private static final class StubBean<T> implements Bean<T> {

        private final T instance;

        StubBean(T instance) {
            this.instance = instance;
        }

        @Override
        public Set<Type> getTypes() {
            return Collections.singleton(instance.getClass());
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return Collections.emptySet();
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return jakarta.enterprise.context.Dependent.class;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Set<Class<? extends Annotation>> getStereotypes() {
            return Collections.emptySet();
        }

        @Override
        public Class<?> getBeanClass() {
            return instance.getClass();
        }

        @Override
        public boolean isAlternative() {
            return false;
        }

        @Override
        public T create(CreationalContext<T> ctx) {
            return instance;
        }

        @Override
        public void destroy(T instance, CreationalContext<T> ctx) {
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return Collections.emptySet();
        }

    }

}
