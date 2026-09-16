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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

import com.sun.faces.application.ApplicationAssociate;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.faces.annotation.View;
import jakarta.faces.context.FacesContext;

/**
 * The CDI bean resolutions cached for one {@link BeanManager} of one application.
 *
 * <p>Post-bootstrap the set of beans is fixed for the lifetime of the application, so
 * {@code BeanManager.getBeans} + {@code resolve} -- the expensive part, a type and qualifier match over the
 * whole bean registry -- is a pure function of its arguments and can be cached. Only resolve through this cache
 * once CDI is ready: a negative outcome cached against a bean manager that is still bootstrapping would outlive
 * the bootstrap. The resolved {@link Bean} is cached, never the reference obtained from it, so that
 * {@code BeanManager.getReference} stays per invocation and scope semantics are preserved.
 *
 * <p>An instance is held by the {@link ApplicationAssociate} of the application it belongs to, which is what
 * bounds its lifetime: an undeployed application takes its cached {@link Bean}s -- and through them its class
 * loader -- with it, and a redeployment starts from an empty cache. Read the caches without a lock; they are
 * read per component per render from every request thread.
 *
 * @see ApplicationAssociate#getCdiBeanCache(BeanManager)
 */
public final class CdiBeanCache {

    /**
     * The key-addressed caches, one per kind of resolution. A converter id, a validator id and a view id are all
     * plain strings, so they need a cache each rather than a shared one.
     */
    enum Kind {

        /** Managed converters, keyed by converter id or by target class. */
        CONVERTER,

        /** Managed validators, keyed by validator id. */
        VALIDATOR,

        /** Managed behaviors, keyed by behavior id. */
        BEHAVIOR,

        /** Programmatic facelets annotated with {@link View}, keyed by view id. */
        VIEW_FACELET,

        /** Anything else, keyed by required type, bean name and qualifiers. */
        RESOLVED
    }

    /**
     * Sentinel for "no bean resolves for this key". It cannot be a {@code null} value because
     * {@code ConcurrentHashMap} forbids those, and telling a cached miss apart from a cold key without it would
     * cost a second lookup. It holds nothing of any application, so one instance serves them all.
     */
    private static final Bean<?> NO_BEAN = new NoBean();

    private final Map<Kind, ConcurrentMap<Object, Bean<?>>> beansByKind = new EnumMap<>(Kind.class);

    private volatile Bean<?> facesContextProducerBean;

    public CdiBeanCache() {
        for (Kind kind : Kind.values()) {
            beansByKind.put(kind, new ConcurrentHashMap<>());
        }
    }

    /**
     * Returns the {@link Bean} cached for the given kind and key, resolving and caching it on first use. A
     * resolution that matches no bean is cached too, and returned as {@code null}.
     *
     * @param kind the kind of resolution.
     * @param key the key to cache the resolution under, unique within its kind.
     * @param resolver resolves the bean when the key is not cached yet.
     * @return the cached bean, or null if no bean matches the key.
     */
    Bean<?> cached(Kind kind, Object key, Supplier<Bean<?>> resolver) {
        ConcurrentMap<Object, Bean<?>> beans = beansByKind.get(kind);
        Bean<?> cached = beans.get(key);

        if (cached != null) {
            return cached == NO_BEAN ? null : cached;
        }

        Bean<?> resolved = resolver.get();
        beans.put(key, resolved == null ? NO_BEAN : resolved);
        return resolved;
    }

    /**
     * Returns the {@link FacesContextProducer} typed {@link FacesContext} bean, resolving and caching it on
     * first use. Held apart from {@link #cached(Kind, Object, Supplier)} because the lookup filters on the bean
     * types rather than on a qualifier, so there is nothing to key it by.
     *
     * @param resolver resolves the bean when it is not cached yet.
     * @return the FacesContextProducer typed FacesContext bean, or null if there is none.
     */
    Bean<?> cachedFacesContextProducerBean(Supplier<Bean<?>> resolver) {
        Bean<?> cached = facesContextProducerBean;

        if (cached != null) {
            return cached == NO_BEAN ? null : cached;
        }

        Bean<?> resolved = resolver.get();
        facesContextProducerBean = resolved == null ? NO_BEAN : resolved;
        return resolved;
    }

    private static final class NoBean implements Bean<Object> {
        @Override public Set<Type> getTypes() { throw new UnsupportedOperationException(); }
        @Override public Set<Annotation> getQualifiers() { throw new UnsupportedOperationException(); }
        @Override public Class<? extends Annotation> getScope() { throw new UnsupportedOperationException(); }
        @Override public String getName() { throw new UnsupportedOperationException(); }
        @Override public Set<Class<? extends Annotation>> getStereotypes() { throw new UnsupportedOperationException(); }
        @Override public Class<?> getBeanClass() { throw new UnsupportedOperationException(); }
        @Override public boolean isAlternative() { throw new UnsupportedOperationException(); }
        @Override public Object create(CreationalContext<Object> ctx) { throw new UnsupportedOperationException(); }
        @Override public void destroy(Object instance, CreationalContext<Object> ctx) { throw new UnsupportedOperationException(); }
        @Override public Set<InjectionPoint> getInjectionPoints() { throw new UnsupportedOperationException(); }
    }
}
