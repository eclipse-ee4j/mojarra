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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.faces.component.behavior.Behavior;
import jakarta.faces.component.behavior.BehaviorBase;

import com.sun.faces.mock.MockBeanManager;

import org.junit.jupiter.api.Test;

/**
 * Measures the number of {@link jakarta.enterprise.inject.spi.BeanManager} lookups that
 * {@link CdiUtils} performs per {@code createConverter}/{@code createValidator}/{@code createBehavior}
 * call when no matching CDI bean is registered &mdash; the common case for built-in Faces IDs such as
 * {@code jakarta.faces.Integer} or {@code jakarta.faces.Required}.
 *
 * <p>Each {@code beanManager.getBeans(type, qualifiers)} call forces the CDI implementation
 * (typically Weld) to walk the bean registry and match types + qualifiers. The numbers below
 * quantify the per-call cost and act as a regression guard against an uncached lookup path.
 */
public class CdiLookupCountTest {

    @Test
    public void createConverter_byId_unknown_performsTwoLookups() {
        CountingBeanManager bm = new CountingBeanManager();

        assertNull(CdiUtils.createConverter(bm, "jakarta.faces.Integer"));

        // 2 type attempts (parameterized Converter<?>, raw Converter), managed=true only.
        assertEquals(2, bm.getBeansByType.get(), "getBeans(Type, qualifiers) calls per createConverter(String)");
        assertEquals(2, bm.resolves.get(), "resolve(Set) calls per createConverter(String)");
    }

    @Test
    public void createConverter_byId_repeatedCalls_areCached() {
        CountingBeanManager bm = new CountingBeanManager();

        for (int i = 0; i < 10; i++) {
            CdiUtils.createConverter(bm, "jakarta.faces.Integer");
        }

        // Only the first call walks the BeanManager; the other 9 hit the resolution cache.
        // Without caching this would be 20 (2 x 10) -- guards against regression to the uncached path.
        assertEquals(2, bm.getBeansByType.get(), "Only first createConverter(String) call hits BeanManager");
    }

    @Test
    public void createValidator_byId_unknown_performsFourLookups() {
        CountingBeanManager bm = new CountingBeanManager();

        assertNull(CdiUtils.createValidator(bm, "jakarta.faces.Required"));

        // 2 (parameterized + raw with original qualifier) + 2 (parameterized + raw with beanName fallback).
        assertEquals(4, bm.getBeansByType.get(), "getBeans(Type, qualifiers) calls per createValidator(String)");
        assertEquals(4, bm.resolves.get(), "resolve(Set) calls per createValidator(String)");
    }

    @Test
    public void createBehavior_byId_unknown_performsOneLookup() {
        CountingBeanManager bm = new CountingBeanManager();

        assertNull(CdiUtils.createBehavior(bm, "jakarta.faces.Ajax"));

        // Raw Behavior type, managed=true only.
        assertEquals(1, bm.getBeansByType.get(), "getBeans(Type, qualifiers) calls per createBehavior(String)");
        assertEquals(1, bm.resolves.get(), "resolve(Set) calls per createBehavior(String)");
    }

    @Test
    public void createConverter_byClass_walksSuperclassChain() {
        CountingBeanManager bm = new CountingBeanManager();

        // java.lang.Long -> java.lang.Number -> (stops at Object): 2 hops.
        // Each hop: 1 qualifier x 2 type attempts (parameterized + raw) = 2 lookups.
        assertNull(CdiUtils.createConverter(bm, Long.class));

        assertEquals(4, bm.getBeansByType.get(), "getBeans calls per createConverter(Class) for Long");
    }

    @Test
    public void createConverter_byClass_repeatedCalls_areCached() {
        CountingBeanManager bm = new CountingBeanManager();

        for (int i = 0; i < 10; i++) {
            CdiUtils.createConverter(bm, Long.class);
        }

        // Only the first call walks Long -> Number (4 lookups); the other 9 hit the class-keyed cache.
        // This is the per-cell render path (renderer calls createConverter(value.getClass()) every render).
        assertEquals(4, bm.getBeansByType.get(), "Only first createConverter(Class) call hits BeanManager");
    }

    @Test
    public void createValidator_byId_repeatedCalls_areCached() {
        CountingBeanManager bm = new CountingBeanManager();

        for (int i = 0; i < 10; i++) {
            CdiUtils.createValidator(bm, "jakarta.faces.Required");
        }

        assertEquals(4, bm.getBeansByType.get(), "Only first createValidator(String) call hits BeanManager");
    }

    @Test
    public void createBehavior_byId_repeatedCalls_areCached() {
        CountingBeanManager bm = new CountingBeanManager();

        for (int i = 0; i < 10; i++) {
            CdiUtils.createBehavior(bm, "jakarta.faces.Ajax");
        }

        assertEquals(1, bm.getBeansByType.get(), "Only first createBehavior(String) call hits BeanManager");
    }

    /**
     * Simulates a typical render of one input bound to an Integer property: the renderer calls
     * {@code application.createConverter(Integer.class)} per render, plus
     * {@code application.createConverter("jakarta.faces.Integer")} when the component declares
     * {@code converter="jakarta.faces.Integer"}. Together this is what a single component costs.
     */
    @Test
    public void typicalIntegerInputRender_costPerComponent() {
        CountingBeanManager bm = new CountingBeanManager();

        CdiUtils.createConverter(bm, Integer.class);          // by-class walk (Integer -> Number) = 4
        CdiUtils.createConverter(bm, "jakarta.faces.Integer"); // by-id                            = 2

        assertEquals(6, bm.getBeansByType.get(), "Total getBeans calls for one Integer input component");
    }

    @Test
    public void resolveBeanByName_repeatedCalls_areCached() {
        CountingBeanManager bm = new CountingBeanManager();

        for (int i = 0; i < 10; i++) {
            CdiUtils.resolveBeanByName(bm, "comSunFacesDataModelClassesMap");
        }

        // Name-based lookups use a different BeanManager API (getBeans(String) vs getBeans(Type, ...))
        // so the test exposes a separate counter. First call probes the registry; rest hit the cache.
        assertEquals(1, bm.getBeansByName.get(), "Only first resolveBeanByName call hits BeanManager");
    }

    @Test
    public void resolveBean_byType_repeatedCalls_areCached() {
        CountingBeanManager bm = new CountingBeanManager();

        for (int i = 0; i < 10; i++) {
            CdiUtils.resolveBean(bm, InjectionPoint.class);
        }

        assertEquals(1, bm.getBeansByType.get(), "Only first resolveBean(Type) call hits BeanManager");
    }

    @Test
    public void resolveFacesContextProducerBean_repeatedCalls_areCached() {
        CountingBeanManager bm = new CountingBeanManager();

        for (int i = 0; i < 10; i++) {
            CdiUtils.resolveFacesContextProducerBean(bm);
        }

        // Without caching the type-filtered FacesContextProducer lookup runs on every request release.
        assertEquals(1, bm.getBeansByType.get(), "FacesContextProducer bean lookup runs once per BeanManager");
    }

    @Test
    public void createConverter_byId_distinctBeanManagers_haveIndependentCaches() {
        CountingBeanManager bm1 = new CountingBeanManager();
        CountingBeanManager bm2 = new CountingBeanManager();

        CdiUtils.createConverter(bm1, "jakarta.faces.Integer");
        CdiUtils.createConverter(bm2, "jakarta.faces.Integer");

        assertEquals(2, bm1.getBeansByType.get(), "Each BeanManager has its own cache");
        assertEquals(2, bm2.getBeansByType.get(), "Each BeanManager has its own cache");
    }

    @Test
    public void createConverter_byId_concurrent_isThreadSafeAndCached() throws Exception {
        CountingBeanManager bm = new CountingBeanManager();

        int threads = 10;
        int callsPerThread = 100;
        Thread[] workers = new Thread[threads];
        for (int t = 0; t < threads; t++) {
            workers[t] = new Thread(() -> {
                for (int i = 0; i < callsPerThread; i++) {
                    CdiUtils.createConverter(bm, "jakarta.faces.Integer");
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
        int total = bm.getBeansByType.get();
        assertTrue(total >= 2 && total <= threads * 2,
                "getBeans calls under contention should land between 2 and " + (threads * 2) + " but was " + total);
    }

    /**
     * Even when a bean does match, the resolution is cached. Only {@code getReference} is called
     * per invocation &mdash; that's intentional, because it produces correctly-scoped references via
     * a fresh {@link CreationalContext} on every call.
     */
    @Test
    public void createBehavior_byId_matchingBean_resolutionCachedButReferenceCalledEveryTime() {
        final Behavior dummyBehavior = new BehaviorBase() {};
        final Bean<Behavior> matchingBean = new StubBean<>(dummyBehavior);
        CountingBeanManager bm = new CountingBeanManager() {
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers) {
                getBeansByType.incrementAndGet();
                return (Set) Collections.singleton(matchingBean);
            }

            @Override
            @SuppressWarnings("unchecked")
            public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans) {
                resolves.incrementAndGet();
                return (Bean<? extends X>) matchingBean;
            }

            @Override
            public Object getReference(Bean<?> bean, Type beanType, CreationalContext<?> ctx) {
                references.incrementAndGet();
                return dummyBehavior;
            }
        };

        for (int i = 0; i < 10; i++) {
            CdiUtils.createBehavior(bm, "jakarta.faces.Ajax");
        }

        // First call resolves; subsequent 9 hit the cache. Total: 1 getBeans + 1 resolve, 10 getReferences.
        assertEquals(1, bm.getBeansByType.get(), "Resolution cached after first hit");
        assertEquals(1, bm.resolves.get(), "Resolution cached after first hit");
        assertEquals(10, bm.references.get(), "getReference invoked per call to preserve scope semantics");
    }

    private static class CountingBeanManager extends MockBeanManager {

        final AtomicInteger getBeansByType = new AtomicInteger();
        final AtomicInteger getBeansByName = new AtomicInteger();
        final AtomicInteger resolves = new AtomicInteger();
        final AtomicInteger references = new AtomicInteger();

        @Override
        public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers) {
            getBeansByType.incrementAndGet();
            return Collections.emptySet();
        }

        @Override
        public Set<Bean<?>> getBeans(String name) {
            getBeansByName.incrementAndGet();
            return Collections.emptySet();
        }

        @Override
        public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans) {
            resolves.incrementAndGet();
            return null;
        }

        @Override
        public Object getReference(Bean<?> bean, Type beanType, CreationalContext<?> ctx) {
            references.incrementAndGet();
            return null;
        }
    }

    private static final class StubBean<T> implements Bean<T> {
        private final T instance;
        StubBean(T instance) { this.instance = instance; }
        @Override public Set<Type> getTypes() { return Collections.singleton(instance.getClass()); }
        @Override public Set<Annotation> getQualifiers() { return Collections.emptySet(); }
        @Override public Class<? extends Annotation> getScope() { return jakarta.enterprise.context.Dependent.class; }
        @Override public String getName() { return null; }
        @Override public Set<Class<? extends Annotation>> getStereotypes() { return Collections.emptySet(); }
        @Override public Class<?> getBeanClass() { return instance.getClass(); }
        @Override public boolean isAlternative() { return false; }
        @Override public T create(CreationalContext<T> ctx) { return instance; }
        @Override public void destroy(T instance, CreationalContext<T> ctx) { }
        @Override public Set<InjectionPoint> getInjectionPoints() { return Collections.emptySet(); }
    }
}
