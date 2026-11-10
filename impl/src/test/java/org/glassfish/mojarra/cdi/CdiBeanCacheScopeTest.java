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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;

import org.glassfish.mojarra.application.ApplicationAssociate;
import org.glassfish.mojarra.config.InitFacesContext;
import org.glassfish.mojarra.mock.MockServletContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Covers the scope of the {@link CdiUtils} bean-resolution caches: they belong to one application and are reachable from nothing else. That is what keeps a
 * container-provided Mojarra -- one set of classes shared by every application of a server that runs for months -- from accumulating the {@link Bean}s, and
 * through them the class loaders, of applications that were undeployed long ago.
 */
public class CdiBeanCacheScopeTest {

    private static final String BEHAVIOR_ID = "jakarta.faces.Ajax";
    private static final int UNCACHED_CALLS = 10;

    /** Types a static field may hold here, because an instance of one cannot reach an application. */
    private static final Set<Class<?>> STATELESS_TYPES = Set.of(String.class, Logger.class, Type.class);

    /** The one static bean: a stateless sentinel for a cached miss, asserted to stay stateless. */
    private static final String SENTINEL_FIELD = "NO_BEAN";

    private final Deque<TestApplicationScope> deployed = new ArrayDeque<>();

    private TestApplicationScope deployApplication() {
        TestApplicationScope application = TestApplicationScope.deploy();
        deployed.push(application);
        return application;
    }

    @AfterEach
    public void undeployAll() {
        while (!deployed.isEmpty()) {
            deployed.pop().undeploy();
        }

        TestApplicationScope.clearThreadState();
    }

    /**
     * Nothing cached may be reachable from a static field, because a static field outlives every application that a container-provided Mojarra serves.
     */
    @Test
    public void keepsNoStaticState() throws Exception {
        for (Class<?> owner : new Class<?>[] { CdiUtils.class, CdiBeanCache.class }) {
            for (Field field : owner.getDeclaredFields()) {
                if (!Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                    continue;
                }

                if (SENTINEL_FIELD.equals(field.getName())) {
                    field.setAccessible(true);
                    assertEquals(
                        "NoBean", field.get(null).getClass().getSimpleName(),
                        "the sentinel must stay the stateless NoBean, which reaches no application"
                    );
                    continue;
                }

                Class<?> type = field.getType();
                assertTrue(
                    type.isPrimitive() || STATELESS_TYPES.contains(type),
                    owner.getSimpleName() + "." + field.getName() + " is static and of type " + type.getName()
                        + "; cached bean resolutions must be held per application so that an undeployed "
                        + "application is released along with them"
                );
            }
        }
    }

    /**
     * Deployment runs with an {@code InitFacesContext} current, which answers almost nothing -- asking it whether it is released throws. Looking up the cache
     * must survive that, because every {@code create*} call made while an application is still being set up goes through it.
     */
    @Test
    public void lookupDuringDeployment_survivesTheInitFacesContext() {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();
        ApplicationAssociate.setCurrentInstance(null);
        TestApplicationScope.clearCurrentFacesContext();
        MockServletContext servletContext = new MockServletContext();
        InitFacesContext initFacesContext = new InitFacesContext(servletContext);

        try {
            assertTrue(
                FacesContext.getCurrentInstance() instanceof InitFacesContext,
                "the InitFacesContext is the one this thread reaches"
            );
            assertNull(CdiUtils.createBehavior(counter.beanManager, BEHAVIOR_ID), "resolution works during deployment");
        }
        finally {
            initFacesContext.release();
        }
    }

    /**
     * A {@link FacesContext} that has been released can no longer name its application. The lookup must then resolve uncached rather than propagate, because
     * the cache is an optimization and every {@code create*} call would otherwise start failing on a thread holding such a context.
     */
    @Test
    public void lookupWithAReleasedContextCurrent_resolvesUncached() throws Exception {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();
        TestApplicationScope application = deployApplication();
        ApplicationAssociate.setCurrentInstance(null);

        FacesContext facesContext = FacesContext.getCurrentInstance();
        Field released = facesContext.getClass().getDeclaredField("released");
        released.setAccessible(true);
        released.setBoolean(facesContext, true);

        try {
            assertNull(CdiUtils.createBehavior(counter.beanManager, BEHAVIOR_ID), "resolution still works");
            assertEquals(1, counter.getBeansByType.get(), "and it resolved for real, because nothing was cached");
            assertNotNull(
                CdiUtils.createConverter(new BeanManagerLookupCounter(new ConverterBean(), new StubConverter()).beanManager, "someConverterId"),
                "including the paths that need the annotation manager, which is simply skipped"
            );
        }
        finally {
            released.setBoolean(facesContext, false);
        }

        application.makeCurrent();
    }

    /**
     * Undeploying one application must not cost the others their warm caches.
     */
    @Test
    public void undeploy_leavesOtherApplicationsUntouched() {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();

        TestApplicationScope applicationOne = deployApplication();
        CdiUtils.createBehavior(counter.beanManager, BEHAVIOR_ID);

        TestApplicationScope applicationTwo = deployApplication();
        CdiUtils.createBehavior(counter.beanManager, BEHAVIOR_ID);
        assertEquals(2, counter.getBeansByType.get(), "each application resolves once for itself");

        applicationOne.undeploy();

        applicationTwo.makeCurrent();
        CdiUtils.createBehavior(counter.beanManager, BEHAVIOR_ID);
        assertEquals(2, counter.getBeansByType.get(), "the surviving application keeps its warm cache");
    }

    /**
     * A redeployment resolves against the beans of the deployment it is, never against those of the deployment it replaces, whose context is gone and which
     * would fail with a {@code ContextNotActiveException} on first use. It gets a cache of its own because it gets an application of its own.
     */
    @Test
    public void redeploy_startsFromAnEmptyCache() throws Exception {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();

        TestApplicationScope application = deployApplication();
        CdiUtils.createBehavior(counter.beanManager, BEHAVIOR_ID);
        CdiUtils.createBehavior(counter.beanManager, BEHAVIOR_ID);
        assertEquals(1, counter.getBeansByType.get(), "warm");
        CdiBeanCache cacheOfFirstDeployment = application.getAssociate().getCdiBeanCache(counter.beanManager);
        application.undeploy();

        TestApplicationScope redeployment = deployApplication();
        CdiUtils.createBehavior(counter.beanManager, BEHAVIOR_ID);

        assertEquals(2, counter.getBeansByType.get(), "the redeployed application resolves for itself");
        assertNotSame(
            cacheOfFirstDeployment, redeployment.getAssociate().getCdiBeanCache(counter.beanManager),
            "the redeployed application caches into one of its own"
        );
        assertEquals(
            0, heldCacheCount(application.getAssociate()),
            "undeploying left the first deployment's associate holding no cache to hand out"
        );
    }

    /**
     * An application can see more than one bean manager -- the one Faces resolves for itself, and the one CDI hands to an extension -- and those need not
     * resolve a given id to the same bean, so each gets a cache of its own.
     */
    @Test
    public void distinctBeanManagers_haveDistinctCaches() {
        BeanManagerLookupCounter counter1 = new BeanManagerLookupCounter();
        BeanManagerLookupCounter counter2 = new BeanManagerLookupCounter();

        TestApplicationScope application = deployApplication();

        CdiUtils.createBehavior(counter1.beanManager, BEHAVIOR_ID);
        CdiUtils.createBehavior(counter2.beanManager, BEHAVIOR_ID);
        CdiUtils.createBehavior(counter1.beanManager, BEHAVIOR_ID);
        CdiUtils.createBehavior(counter2.beanManager, BEHAVIOR_ID);

        assertEquals(1, counter1.getBeansByType.get(), "each bean manager resolves once");
        assertEquals(1, counter2.getBeansByType.get(), "each bean manager resolves once");
        assertNotSame(
            application.getAssociate().getCdiBeanCache(counter1.beanManager),
            application.getAssociate().getCdiBeanCache(counter2.beanManager)
        );
    }

    /**
     * The same bean manager of the same application yields the same cache, however often it is asked for.
     */
    @Test
    public void sameBeanManager_yieldsTheSameCache() {
        BeanManager beanManager = mock(BeanManager.class);
        TestApplicationScope application = deployApplication();

        assertSame(
            application.getAssociate().getCdiBeanCache(beanManager),
            application.getAssociate().getCdiBeanCache(beanManager)
        );
    }

    /**
     * A thread that is serving no application -- a background thread, or a request that arrives after the application is gone -- still resolves, it is simply
     * not cached. Correctness never depends on the cache.
     */
    @Test
    public void callFromOutsideAnyApplication_resolvesUncached() throws Exception {
        BeanManagerLookupCounter counter = new BeanManagerLookupCounter();
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Thread background = new Thread(() -> {
            try {
                for (int i = 0; i < UNCACHED_CALLS; i++) {
                    assertNull(CdiUtils.createBehavior(counter.beanManager, BEHAVIOR_ID), "resolution still works");
                }
                assertEquals(
                    UNCACHED_CALLS, counter.getBeansByType.get(),
                    "every call resolved for real, nothing was cached"
                );
            }
            catch (Throwable e) {
                failure.set(e);
            }
        }, "serves-no-application");

        background.start();
        background.join();

        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
    }

    /**
     * Resolving a converter applies the Faces annotations it declares, which needs the application's annotation manager. Outside an application there is none,
     * and the converter is still returned.
     */
    @Test
    public void resolvableConverterFromOutsideAnyApplication_skipsTheAnnotationPass() throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Thread background = new Thread(() -> {
            try {
                assertNull(ApplicationAssociate.getCurrentInstance(), "this thread serves no application");
                assertNotNull(
                    CdiUtils.createConverter(new BeanManagerLookupCounter(new ConverterBean(), new StubConverter()).beanManager, "someConverterId"),
                    "the converter is returned, only its annotation pass is skipped"
                );
            }
            catch (Throwable e) {
                failure.set(e);
            }
        }, "serves-no-application");

        background.start();
        background.join();

        if (failure.get() != null) {
            throw new AssertionError(failure.get());
        }
    }

    private static int heldCacheCount(ApplicationAssociate associate) throws Exception {
        Field field = ApplicationAssociate.class.getDeclaredField("cdiBeanCaches");
        field.setAccessible(true);
        return ((Map<?, ?>) field.get(associate)).size();
    }

    private static final class ConverterBean implements Bean<Converter<?>> {

        @Override
        public Set<Type> getTypes() {
            return Collections.singleton(Converter.class);
        }

        @Override
        public Set<Annotation> getQualifiers() {
            return Collections.emptySet();
        }

        @Override
        public Class<? extends Annotation> getScope() {
            return Dependent.class;
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
            return StubConverter.class;
        }

        @Override
        public boolean isAlternative() {
            return false;
        }

        @Override
        public Converter<?> create(CreationalContext<Converter<?>> ctx) {
            return new StubConverter();
        }

        @Override
        public void destroy(Converter<?> instance, CreationalContext<Converter<?>> ctx) {
        }

        @Override
        public Set<InjectionPoint> getInjectionPoints() {
            return Collections.emptySet();
        }

    }

    private static final class StubConverter implements Converter<Object> {

        @Override
        public Object getAsObject(FacesContext context, UIComponent component, String value) {
            return value;
        }

        @Override
        public String getAsString(FacesContext context, UIComponent component, Object value) {
            return String.valueOf(value);
        }

    }

}
