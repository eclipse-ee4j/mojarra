/*
 * Copyright (c) 1997, 2019 Oracle and/or its affiliates. All rights reserved.
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

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import jakarta.enterprise.context.ContextNotActiveException;
import jakarta.enterprise.context.spi.Context;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Annotated;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.enterprise.util.TypeLiteral;
import jakarta.faces.annotation.View;
import jakarta.faces.component.behavior.Behavior;
import jakarta.faces.component.behavior.FacesBehavior;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.faces.model.DataModel;
import jakarta.faces.model.FacesDataModel;
import jakarta.faces.validator.FacesValidator;
import jakarta.faces.validator.Validator;
import jakarta.faces.view.facelets.Facelet;

/**
 * A static utility class for CDI.
 */
public final class CdiUtils {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = FacesLogger.APPLICATION_VIEW.getLogger();

    private final static Type CONVERTER_TYPE = new TypeLiteral<Converter<?>>() {
        private static final long serialVersionUID = 1L;
    }.getType();

    private final static Type VALIDATOR_TYPE = new TypeLiteral<Validator<?>>() {
        private static final long serialVersionUID = 1L;
    }.getType();

    /**
     * The per-application bean-resolution caches, as an immutable snapshot keyed by {@link BeanManager}.
     *
     * <p>Post-bootstrap the set of beans is immutable for the application lifetime, so {@code getBeans} +
     * {@code resolve} (the expensive part: type/qualifier matching over the whole bean registry) is a pure
     * function of its arguments and can be cached. {@link BeanManager#getReference(Bean, Type, CreationalContext)}
     * is intentionally kept out of the cache to preserve scope semantics &mdash; that call is cheap.
     *
     * <p><strong>Why a copy-on-write snapshot rather than a concurrent or synchronized map.</strong> This map
     * is read on every {@code Application#createConverter/createValidator/createBehavior} call, i.e. per
     * component per render, from every request thread. A {@code synchronizedMap} serializes all of them on one
     * process-wide monitor even for a pure cache hit, which shows up in thread dumps as request threads
     * {@code BLOCKED} in {@code Collections$SynchronizedMap}. Entries are added and removed only at deployment
     * boundaries (see {@link #registerBeanManager} / {@link #unregisterBeanManager}), so the hot path is a
     * plain volatile read plus an immutable lookup, with no lock at all. The field must be {@code volatile}:
     * readers never take the {@link #REGISTRATION_LOCK} that writers hold, so without it there is no
     * happens-before edge and a reader could keep using a stale snapshot indefinitely &mdash; including one
     * still holding an undeployed application's {@link Bean}s.
     *
     * <p><strong>The registration invariant.</strong> Nothing may enter this map except through
     * {@link #registerBeanManager}, which is called once per application while the application is being set
     * up. A {@link BeanManager} that is not registered &mdash; because it was never set up, or because its
     * application has been undeployed &mdash; resolves without caching: correct, merely uncached. That single
     * rule is what makes an unbounded map, a classloader leak and stale post-undeployment entries structurally
     * impossible here, rather than something a cleanup hook has to be trusted to prevent.
     */
    private static volatile Map<BeanManager, CdiCache> CACHES = Map.of();

    /**
     * Guards writes to {@link #CACHES}. Taken twice in an application's lifetime; never on the read path.
     */
    private static final Object REGISTRATION_LOCK = new Object();

    private static final Bean<?> NO_BEAN = new NoBean();

    private static final Function<CdiCache, ConcurrentMap<Object, Bean<?>>> CONVERTERS = cache -> cache.converterBeans;
    private static final Function<CdiCache, ConcurrentMap<Object, Bean<?>>> VALIDATORS = cache -> cache.validatorBeans;
    private static final Function<CdiCache, ConcurrentMap<Object, Bean<?>>> BEHAVIORS = cache -> cache.behaviorBeans;
    private static final Function<CdiCache, ConcurrentMap<Object, Bean<?>>> VIEW_FACELETS = cache -> cache.viewFaceletBeans;

    /**
     * Starts caching bean resolutions for {@code beanManager}, discarding anything cached for an equal key
     * before. Called from {@link Util#getCdiBeanManager(FacesContext)} at the point where Faces settles on the
     * {@link BeanManager} instance it will use for the application's lifetime, and from {@link CdiExtension} on
     * {@code AfterDeploymentValidation}. Registering the very object that will later be used for lookups is
     * deliberate: the CDI specification says nothing about {@code BeanManager} equality or instance identity
     * (Weld derives {@code equals}/{@code hashCode} from the bean archive id, OpenWebBeans leaves them as
     * identity), so matching by {@code ==} on both ends is the only portable guarantee.
     *
     * <p>Replacing rather than keeping an existing entry matters for redeployment: on an implementation whose
     * {@code BeanManager} equality is archive-derived, a redeployed application produces a key equal to the
     * previous deployment's, and reusing that entry would resolve against {@link Bean}s whose context is gone
     * (a {@code ContextNotActiveException} on first use). Both call sites run at deployment time, before the
     * application serves traffic, so nothing warm is lost.
     *
     * @param beanManager the bean manager to cache resolutions for; {@code null} is ignored
     */
    public static void registerBeanManager(BeanManager beanManager) {
        if (beanManager == null) {
            return;
        }

        synchronized (REGISTRATION_LOCK) {
            Map<BeanManager, CdiCache> updated = new HashMap<>(CACHES);
            updated.put(beanManager, new CdiCache());
            CACHES = Map.copyOf(updated);
        }
    }

    /**
     * Stops caching bean resolutions for {@code beanManager} and drops everything cached for it, so an
     * undeployed application's {@link Bean}s (and through them its class loader) are released and can never be
     * handed to a later deployment. Subsequent lookups for it resolve uncached.
     *
     * <p>Called from {@link CdiExtension} on {@code BeforeShutdown} with the instance that extension
     * registered, and from {@code ConfigureListener#contextDestroyed} with the instance held in the
     * {@code ServletContext}. Each call site passes back the same object it registered, so no assumption about
     * {@code BeanManager} equality is needed. Removing an unregistered bean manager is a no-op, which makes the
     * two call sites safe to both fire for the same application.
     *
     * @param beanManager the bean manager to stop caching resolutions for; {@code null} is ignored
     */
    public static void unregisterBeanManager(BeanManager beanManager) {
        if (beanManager == null || !CACHES.containsKey(beanManager)) {
            return;
        }

        synchronized (REGISTRATION_LOCK) {
            Map<BeanManager, CdiCache> updated = new HashMap<>(CACHES);
            if (updated.remove(beanManager) == null) {
                return;
            }
            CACHES = Map.copyOf(updated);
        }
    }

    /**
     * One application's resolution caches. Handed out as a unit so that registering and unregistering an
     * application is a single atomic change to {@link #CACHES}. The inner maps are concurrent because they are
     * written on the request path; the holder itself is immutable and safely published by the volatile write to
     * {@link #CACHES}.
     */
    private static final class CdiCache {

        /** Resolved {@code (type, beanName, qualifiers) -> Bean<?>} lookups. Misses are cached as {@link #NO_BEAN}. */
        final ConcurrentMap<BeanLookupKey, Bean<?>> resolvedBeans = new ConcurrentHashMap<>();

        /**
         * Target-class / id keyed caches of the resolved managed converter, validator and behavior beans. The
         * by-class converter lookup otherwise walks the superclass chain building a {@link FacesConverter}
         * qualifier + {@link BeanLookupKey} at each level (and the by-id paths build two to four such keys) on
         * every call -- per cell during render. Keying directly on the target {@code Class} or id collapses
         * that to a single cheap map lookup. Misses are cached as {@link #NO_BEAN}.
         */
        final ConcurrentMap<Object, Bean<?>> converterBeans = new ConcurrentHashMap<>();
        final ConcurrentMap<Object, Bean<?>> validatorBeans = new ConcurrentHashMap<>();
        final ConcurrentMap<Object, Bean<?>> behaviorBeans = new ConcurrentHashMap<>();

        /** View-id keyed cache of the {@link View} annotated programmatic facelet beans, one lookup per view. */
        final ConcurrentMap<Object, Bean<?>> viewFaceletBeans = new ConcurrentHashMap<>();

        /**
         * The single {@link FacesContextProducer}-typed {@link FacesContext} bean, resolved once per
         * application for the {@link FacesContextImpl} release path. Held separately from
         * {@link #resolvedBeans} because the lookup filters by {@code bean.getTypes().contains(...)} rather
         * than by a CDI qualifier, so it does not fit the {@link BeanLookupKey} shape.
         */
        final AtomicReference<Bean<?>> facesContextProducerBean = new AtomicReference<>();
    }

    /**
     * Constructor.
     */
    private CdiUtils() {
    }

    /**
     * Create a converter using the FacesConverter value attribute.
     *
     * @param beanManager the bean manager.
     * @param value the value attribute.
     * @return the converter, or null if we could not match one.
     */
    public static Converter<?> createConverter(BeanManager beanManager, String value) {
        Bean<?> bean = cachedBean(CONVERTERS, beanManager, value,
                () -> resolveConverterBean(beanManager, FacesConverter.Literal.of(value, Object.class, true)));

        if (bean == null) {
            return null;
        }

        Converter<?> managedConverter = (Converter<?>) getReferenceInstance(beanManager, bean.getBeanClass(), bean);
        ApplicationAssociate associate = ApplicationAssociate.getCurrentInstance();
        associate.getAnnotationManager().applyConverterAnnotations(FacesContext.getCurrentInstance(), managedConverter); // #4913

        return new CdiConverter(value, Object.class, managedConverter);
    }

    /**
     * Create a converter using the FacesConverter forClass attribute.
     *
     * @param beanManager the bean manager.
     * @param forClass the for class.
     * @return the converter, or null if we could not match one.
     */
    public static Converter<?> createConverter(BeanManager beanManager, Class<?> forClass) {
        Bean<?> bean = cachedBean(CONVERTERS, beanManager, forClass,
                () -> resolveConverterBeanForClass(beanManager, forClass));

        if (bean == null) {
            return null;
        }

        Converter<?> managedConverter = (Converter<?>) getReferenceInstance(beanManager, bean.getBeanClass(), bean);
        ApplicationAssociate associate = ApplicationAssociate.getCurrentInstance();
        associate.getAnnotationManager().applyConverterAnnotations(FacesContext.getCurrentInstance(), managedConverter); // #4913

        return new CdiConverter("", forClass, managedConverter);
    }

    private static Bean<?> resolveConverterBeanForClass(BeanManager beanManager, Class<?> forClass) {
        for (Class<?> forClassOrSuperclass = forClass; forClassOrSuperclass != null
                && forClassOrSuperclass != Object.class; forClassOrSuperclass = forClassOrSuperclass.getSuperclass()) {
            Bean<?> bean = resolveConverterBean(beanManager, FacesConverter.Literal.of("", forClassOrSuperclass, true));
            if (bean != null) {
                return bean;
            }
        }
        return null;
    }

    private static Bean<?> resolveConverterBean(BeanManager beanManager, Annotation qualifier) {
        // Try the parameterized converter type first, then the raw converter type.
        Bean<?> bean = resolveBeanUncached(beanManager, CONVERTER_TYPE, null, qualifier);
        if (bean == null) {
            bean = resolveBeanUncached(beanManager, Converter.class, null, qualifier);
        }
        return bean;
    }

    /**
     * Returns the resolved managed {@link Bean} for {@code key}, computing it via {@code resolver} and caching
     * the outcome (a miss as {@link #NO_BEAN}) on first use. {@code getReference} is left to the caller so
     * scope stays per-invocation.
     *
     * <p>If {@code beanManager} is not registered the resolution still happens, it is simply not cached; see
     * the registration invariant on {@link #CACHES}.
     */
    private static Bean<?> cachedBean(Function<CdiCache, ConcurrentMap<Object, Bean<?>>> which,
            BeanManager beanManager, Object key, Supplier<Bean<?>> resolver) {
        CdiCache cdiCache = CACHES.get(beanManager);
        if (cdiCache == null) {
            return resolver.get();
        }

        ConcurrentMap<Object, Bean<?>> cache = which.apply(cdiCache);
        Bean<?> cached = cache.get(key);
        if (cached != null) {
            return cached == NO_BEAN ? null : cached;
        }
        Bean<?> bean = resolver.get();
        cache.put(key, bean == null ? NO_BEAN : bean);
        return bean;
    }

    private static Object getReferenceInstance(BeanManager beanManager, Type type, Bean<?> bean) {
        return beanManager.getReference(bean, type, beanManager.createCreationalContext(bean));
    }

    /**
     * Create a behavior using the FacesBehavior value attribute.
     *
     * @param beanManager the bean manager.
     * @param value the value attribute.
     * @return the behavior, or null if we could not match one.
     */
    public static Behavior createBehavior(BeanManager beanManager, String value) {
        Bean<?> bean = cachedBean(BEHAVIORS, beanManager, value,
                () -> resolveBeanUncached(beanManager, Behavior.class, null, FacesBehavior.Literal.of(value, true)));

        if (bean == null) {
            return null;
        }

        Behavior managedBehavior = (Behavior) getReferenceInstance(beanManager, bean.getBeanClass(), bean);
        return new CdiBehavior(value, managedBehavior);
    }

    /**
     * Obtain the programmatic facelet the application declares for the given view id with {@link View}.
     *
     * @param beanManager the bean manager.
     * @param viewId the view id.
     * @return the facelet, or null if the application declares none for that view id.
     */
    public static Facelet getViewFacelet(BeanManager beanManager, String viewId) {
        Bean<?> bean = cachedBean(VIEW_FACELETS, beanManager, viewId,
                () -> resolveBeanUncached(beanManager, Facelet.class, null, View.Literal.of(viewId)));

        if (bean == null) {
            return null;
        }

        return (Facelet) getReferenceInstance(beanManager, Facelet.class, bean);
    }

    /**
     * Create a validator using the FacesValidator value attribute.
     *
     * @param beanManager the bean manager.
     * @param value the value attribute.
     * @return the validator, or null if we could not match one.
     */
    public static Validator<?> createValidator(BeanManager beanManager, String value) {
        Bean<?> bean = cachedBean(VALIDATORS, beanManager, value,
                () -> resolveValidatorBean(beanManager, value));

        if (bean == null) {
            return null;
        }

        Validator<?> managedValidator = (Validator<?>) getReferenceInstance(beanManager, bean.getBeanClass(), bean);
        return new CdiValidator(value, managedValidator);
    }

    private static Bean<?> resolveValidatorBean(BeanManager beanManager, String value) {
        // Try the parameterized validator type first, then the raw type.
        Annotation qualifier = FacesValidator.Literal.of(value, false, true);
        Bean<?> bean = resolveBeanUncached(beanManager, VALIDATOR_TYPE, null, qualifier);
        if (bean == null) {
            bean = resolveBeanUncached(beanManager, Validator.class, null, qualifier);
        }
        if (bean == null) {
            // Still nothing found: try the default qualifier with value as the bean name.
            qualifier = FacesValidator.Literal.of("", false, true);
            bean = resolveBeanUncached(beanManager, VALIDATOR_TYPE, value, qualifier);
            if (bean == null) {
                bean = resolveBeanUncached(beanManager, Validator.class, value, qualifier);
            }
        }
        return bean;
    }

    public static void addAnnotatedTypes(BeforeBeanDiscovery beforeBean, BeanManager beanManager, Class<?>... types) {
        for (Class<?> type : types) {
            beforeBean.addAnnotatedType(beanManager.createAnnotatedType(type), "Mojarra " + type.getName());
        }
    }

    public static <T> T getBeanReference(Class<T> type, Annotation... qualifiers) {
        return type.cast(getBeanReferenceByType(Util.getCdiBeanManager(FacesContext.getCurrentInstance()), type, qualifiers));
    }

    public static <T> T getBeanReference(FacesContext facesContext, Class<T> type, Annotation... qualifiers) {
        return type.cast(getBeanReferenceByType(Util.getCdiBeanManager(facesContext), type, qualifiers));
    }

    /**
     * @param <T> the generic bean type
     * @param beanManager the bean manager
     * @param type the required bean type the reference must have
     * @param qualifiers the required qualifiers the reference must have
     * @return a bean reference adhering to the required type and qualifiers
     */
    public static <T> T getBeanReference(BeanManager beanManager, Class<T> type, Annotation... qualifiers) {
        return getBeanReference(beanManager, type, null, qualifiers);
    }

    public static Object getBeanReferenceByType(BeanManager beanManager, Type type, Annotation... qualifiers) {
        return getBeanReferenceByType(beanManager, type, null, qualifiers);
    }

    private static <T> T getBeanReference(BeanManager beanManager, Class<T> type, String beanName, Annotation... qualifiers) {
        return type.cast(getBeanReferenceByType(beanManager, type, beanName, qualifiers));
    }

    private static Object getBeanReferenceByType(BeanManager beanManager, Type type, String beanName, Annotation... qualifiers) {
        Bean<?> bean = resolveBean(beanManager, type, beanName, qualifiers);
        if (bean == null) {
            return null;
        }
        return beanManager.getReference(bean, type, beanManager.createCreationalContext(bean));
    }

    private static Bean<?> resolveBean(BeanManager beanManager, Type type, String beanName, Annotation... qualifiers) {
        CdiCache cdiCache = CACHES.get(beanManager);
        if (cdiCache == null) {
            return resolveBeanUncached(beanManager, type, beanName, qualifiers);
        }

        ConcurrentMap<BeanLookupKey, Bean<?>> cache = cdiCache.resolvedBeans;
        BeanLookupKey key = new BeanLookupKey(type, beanName, new HashSet<>(Arrays.asList(qualifiers)));
        Bean<?> cached = cache.get(key);
        if (cached != null) {
            return cached == NO_BEAN ? null : cached;
        }
        Bean<?> resolved = resolveBeanUncached(beanManager, type, beanName, qualifiers);
        cache.put(key, resolved == null ? NO_BEAN : resolved);
        return resolved;
    }

    /**
     * The raw {@code getBeans} + {@code resolve} (with optional bean-name filtering), without the
     * {@link #RESOLVED_BEANS} cache. The Faces-artifact paths (converter/validator/behavior) cache the
     * resolved {@link Bean} themselves keyed by class/id, so they resolve through this directly rather than
     * also populating {@link #RESOLVED_BEANS} with entries they would never read again.
     */
    private static Bean<?> resolveBeanUncached(BeanManager beanManager, Type type, String beanName, Annotation... qualifiers) {
        Set<Bean<?>> beans = beanManager.getBeans(type, qualifiers);
        if (beanName != null) {
            beans = beans.stream()
                .filter(bean -> beanName.equals(getBeanName(bean)))
                .collect(toSet());
        }
        return beanManager.resolve(beans);
    }

    /**
     * Resolves the {@link Bean} for the given required type and qualifiers using the application's
     * cache. Returns {@code null} if no bean matches. Unlike {@link #getBeanReference}, this does
     * not invoke {@code getReference} -- the caller is responsible for that when an instance is
     * needed, so scope semantics remain correct on each invocation.
     */
    public static Bean<?> resolveBean(BeanManager beanManager, Type type, Annotation... qualifiers) {
        return resolveBean(beanManager, type, null, qualifiers);
    }

    /**
     * Resolves the {@link Bean} for the given EL name using the application's cache.
     * Returns {@code null} if no bean has that name.
     */
    public static Bean<?> resolveBeanByName(BeanManager beanManager, String name) {
        CdiCache cdiCache = CACHES.get(beanManager);
        if (cdiCache == null) {
            return beanManager.resolve(beanManager.getBeans(name));
        }

        ConcurrentMap<BeanLookupKey, Bean<?>> cache = cdiCache.resolvedBeans;
        BeanLookupKey key = new BeanLookupKey(null, name, Collections.emptySet());
        Bean<?> cached = cache.get(key);
        if (cached != null) {
            return cached == NO_BEAN ? null : cached;
        }
        Bean<?> resolved = beanManager.resolve(beanManager.getBeans(name));
        cache.put(key, resolved == null ? NO_BEAN : resolved);
        return resolved;
    }

    /**
     * Resolves and caches the {@link FacesContextProducer}-typed {@link FacesContext} bean
     * once per {@link BeanManager}. Used by the per-request {@link FacesContext#release()}
     * destruction path: the producer is registered exactly once per application, so re-running
     * the type-containment filter on every request is wasted work.
     */
    public static Bean<?> resolveFacesContextProducerBean(BeanManager beanManager) {
        CdiCache cdiCache = CACHES.get(beanManager);
        if (cdiCache == null) {
            return resolveFacesContextProducerBeanUncached(beanManager);
        }

        Bean<?> cached = cdiCache.facesContextProducerBean.get();
        if (cached != null) {
            return cached == NO_BEAN ? null : cached;
        }
        Bean<?> resolved = resolveFacesContextProducerBeanUncached(beanManager);
        cdiCache.facesContextProducerBean.set(resolved == null ? NO_BEAN : resolved);
        return resolved;
    }

    private static Bean<?> resolveFacesContextProducerBeanUncached(BeanManager beanManager) {
        Set<Bean<?>> beans = beanManager.getBeans(FacesContext.class).stream()
            .filter(bean -> bean.getTypes().contains(FacesContextProducer.class))
            .collect(toSet());
        return beanManager.resolve(beans);
    }

    private static final class BeanLookupKey {
        private final Type type;
        private final String beanName;
        private final Set<Annotation> qualifiers;
        private final int hash;

        BeanLookupKey(Type type, String beanName, Set<Annotation> qualifiers) {
            this.type = type;
            this.beanName = beanName;
            this.qualifiers = qualifiers;
            this.hash = Objects.hash(type, beanName, qualifiers);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof BeanLookupKey)) {
                return false;
            }
            BeanLookupKey k = (BeanLookupKey) other;
            return hash == k.hash && Objects.equals(type, k.type)
                    && Objects.equals(beanName, k.beanName) && qualifiers.equals(k.qualifiers);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    /**
     * Sentinel for "no bean resolves for this key". Cannot be a {@code null} value because
     * {@link ConcurrentHashMap} forbids null values, and we want to distinguish "cached miss"
     * from "not yet cached" without a second containsKey call.
     */
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

    private static String getBeanName(Bean<?> bean) {
        String name = bean.getName();

        if (name != null) {
            return name;
        }

        String className = bean.getBeanClass().getSimpleName();

        return Character.toLowerCase(className.charAt(0)) + className.substring(1);
    }

    /**
     * Returns concrete (non-proxied) bean instance of given class in current context.
     *
     * @param <T> the generic bean type
     * @param type the required bean type the instance must have
     * @param create whether to auto-create bean if not exist
     * @return a bean instance adhering to the required type
     */
    public static <T> T getBeanInstance(Class<T> type, boolean create) {
        BeanManager beanManager = Util.getCdiBeanManager(FacesContext.getCurrentInstance());
        @SuppressWarnings("unchecked")
        Bean<T> bean = (Bean<T>) beanManager.resolve(beanManager.getBeans(type));

        if (bean != null) {
            Context context = beanManager.getContext(bean.getScope());

            if (create) {
                return context.get(bean, beanManager.createCreationalContext(bean));
            } else {
                return context.get(bean);
            }
        } else {
            return null;
        }
    }

    /**
     * Finds an annotation in an Annotated, taking stereo types into account
     *
     * @param <A> the generic annotation type
     * @param beanManager the current bean manager
     * @param annotated the Annotated in which to search
     * @param annotationType the type of the annotation to search for
     * @return An Optional that contains an instance of annotation type for which was searched if the annotated contained
     * this.
     */
    public static <A extends Annotation> Optional<A> getAnnotation(BeanManager beanManager, Annotated annotated, Class<A> annotationType) {

        annotated.getAnnotation(annotationType);

        if (annotated.getAnnotations().isEmpty()) {
            return empty();
        }

        if (annotated.isAnnotationPresent(annotationType)) {
            return Optional.of(annotated.getAnnotation(annotationType));
        }

        Queue<Annotation> annotations = new LinkedList<>(annotated.getAnnotations());

        while (!annotations.isEmpty()) {
            Annotation annotation = annotations.remove();

            if (annotation.annotationType().equals(annotationType)) {
                return Optional.of(annotationType.cast(annotation));
            }

            try {
                if (beanManager.isStereotype(annotation.annotationType())) {
                    annotations.addAll(beanManager.getStereotypeDefinition(annotation.annotationType()));
                }
            } catch (Exception e) {
                // Log and continue, if it's not allowed to test if it's a stereo type
                // we're unlikely to be interested in this annotation
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.warning("Exception happened when finding an annotation: " + e);
                }
            }
        }

        return empty();
    }

    public static DataModel<?> createDataModel(final Class<?> forClass) {

        List<DataModel<?>> dataModel = new ArrayList<>(1);
        CDI<Object> cdi = CDI.current();

        // Scan the map in order, the first class that is a super class or equal to the class for which
        // we're looking for a DataModel is the closest match, since the Map is sorted on inheritance relation
        getDataModelClassesMap(cdi).entrySet().stream().filter(e -> e.getKey().isAssignableFrom(forClass)).findFirst().ifPresent(

                // Get the bean from CDI which is of the class type that we found during annotation scanning
                // and has the @FacesDataModel annotation, with the "forClass" attribute set to the closest
                // super class of our target class.

                e -> dataModel.add(cdi.select(e.getValue(), FacesDataModel.Literal.of(e.getKey())).get()));

        return dataModel.isEmpty() ? null : dataModel.get(0);
    }

    @SuppressWarnings("unchecked")
    public static Map<Class<?>, Class<? extends DataModel<?>>> getDataModelClassesMap(CDI<Object> cdi) {
        BeanManager beanManager = cdi.getBeanManager();

        // Get the Map with classes for which a custom DataModel implementation is available from CDI
        Bean<?> bean = resolveBeanByName(beanManager, "comSunFacesDataModelClassesMap");
        Object beanReference = beanManager.getReference(bean, Map.class, beanManager.createCreationalContext(bean));

        return (Map<Class<?>, Class<? extends DataModel<?>>>) beanReference;
    }

    /**
     * Returns the current injection point.
     * @param beanManager the involved bean manager
     * @param creationalContext the involved creational context
     * @return the current injection point
     */
    public static InjectionPoint getCurrentInjectionPoint(BeanManager beanManager, CreationalContext<?> creationalContext) {
        Bean<?> bean = resolveBean(beanManager, InjectionPoint.class);
        InjectionPoint injectionPoint = (InjectionPoint) beanManager.getReference(bean, InjectionPoint.class, creationalContext);

        if (injectionPoint == null) { // It's broken in some Weld versions. Below is a work around.
            bean = resolveBean(beanManager, InjectionPointGenerator.class);
            injectionPoint = (InjectionPoint) beanManager.getInjectableReference(bean.getInjectionPoints().iterator().next(), creationalContext);
        }

        return injectionPoint;
    }

    /**
     * Returns the qualifier annotation of the given qualifier class from the given injection point.
     * @param <A> the type of given qualifier class 
     * @param injectionPoint the injection point
     * @param qualifierClass the qualifier class to be filtered
     * @return the qualifier annotation
     */
    public static <A extends Annotation> A getQualifier(InjectionPoint injectionPoint, Class<A> qualifierClass) {
        for (Annotation annotation : injectionPoint.getQualifiers()) {
            if (qualifierClass.isAssignableFrom(annotation.getClass())) {
                return qualifierClass.cast(annotation);
            }
        }

        return null;
    }

    /**
     * Returns true if given scope is active in current context.
     * @param <S> the type of given scope
     * @param scope the scope to be checked
     * @return whether given scope is active
     */
    public static <S extends Annotation> boolean isScopeActive(Class<S> scope) {
        BeanManager beanManager = Util.getCdiBeanManager(FacesContext.getCurrentInstance());

        try {
            Context context = beanManager.getContext(scope);
            return context.isActive();
        } catch (ContextNotActiveException ignore) {
            return false;
        }
    }

    /**
     * Returns true if Weld is used as CDI impl.
     */
    public static boolean isWeld(BeanManager beanManager) {
        return beanManager.getClass().getPackageName().startsWith("org.jboss.weld.");
    }

}
