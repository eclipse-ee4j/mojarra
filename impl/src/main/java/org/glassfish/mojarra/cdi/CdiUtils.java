/*
 * Copyright (c) 2020, 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.mojarra.cdi;

import static java.util.Optional.empty;
import static java.util.stream.Collectors.toSet;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

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

import org.glassfish.mojarra.application.ApplicationAssociate;
import org.glassfish.mojarra.cdi.CdiBeanCache.Kind;
import org.glassfish.mojarra.util.FacesLogger;
import org.glassfish.mojarra.util.Util;

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
     * Returns the {@link ApplicationAssociate} of the application the calling thread reached, or {@code null} when it reached none: a thread serving no
     * request, with no usable {@link FacesContext} either. A context that has been released can no longer name its application and counts as none, because
     * everything here is an optimization and must cost a resolution rather than an exception.
     */
    private static ApplicationAssociate currentAssociate() {
        try {
            return ApplicationAssociate.getCurrentInstance();
        }
        catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * Returns the bean resolutions cached for the given bean manager by the application whose associate is current on the calling thread, or {@code null} when
     * there is none and the caller must resolve uncached.
     *
     * <p>
     * Under a cross-context dispatch the current associate is the dispatching application's while {@code beanManager} is the target's, so the target's beans
     * are cached in the dispatcher. Keying by bean manager is what keeps that correct: no application is ever served another's beans.
     */
    private static CdiBeanCache cdiBeanCache(ApplicationAssociate associate, BeanManager beanManager) {
        return associate == null ? null : associate.getCdiBeanCache(beanManager);
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
        ApplicationAssociate associate = currentAssociate();
        Bean<?> bean = cachedBean(
            associate, Kind.CONVERTER, beanManager, value,
            () -> resolveConverterBean(beanManager, FacesConverter.Literal.of(value, Object.class, false))
        );

        if (bean == null) {
            return null;
        }

        Converter<?> managedConverter = (Converter<?>) getReferenceInstance(beanManager, bean.getBeanClass(), bean);
        applyConverterAnnotations(associate, managedConverter);

        return managedConverter;
    }

    /**
     * Create a converter using the FacesConverter forClass attribute.
     *
     * @param beanManager the bean manager.
     * @param forClass the for class.
     * @return the converter, or null if we could not match one.
     */
    public static Converter<?> createConverter(BeanManager beanManager, Class<?> forClass) {
        ApplicationAssociate associate = currentAssociate();
        Bean<?> bean = cachedBean(
            associate, Kind.CONVERTER, beanManager, forClass,
            () -> resolveConverterBeanForClass(beanManager, forClass)
        );

        if (bean == null) {
            return null;
        }

        Converter<?> managedConverter = (Converter<?>) getReferenceInstance(beanManager, bean.getBeanClass(), bean);
        applyConverterAnnotations(associate, managedConverter);

        return managedConverter;
    }

    private static Bean<?> resolveConverterBeanForClass(BeanManager beanManager, Class<?> forClass) {
        for (Class<?> forClassOrSuperclass = forClass; forClassOrSuperclass != null
            && forClassOrSuperclass != Object.class; forClassOrSuperclass = forClassOrSuperclass.getSuperclass()) {
            Bean<?> bean = resolveConverterBean(beanManager, FacesConverter.Literal.of("", forClassOrSuperclass, false));
            if (bean != null) {
                return bean;
            }
        }
        return null;
    }

    private static Bean<?> resolveConverterBean(BeanManager beanManager, Annotation qualifier) {
        // Try the parameterized converter type first, then the raw converter type.
        Bean<?> bean = resolveBean(beanManager, CONVERTER_TYPE, qualifier);
        if (bean == null) {
            bean = resolveBean(beanManager, Converter.class, qualifier);
        }
        return bean;
    }

    /**
     * Returns the resolved {@link Bean} for {@code key} from the calling application's cache, resolving it uncached when there is no such application.
     * {@code getReference} is left to the caller so scope stays per-invocation.
     */
    private static Bean<?> cachedBean(
        ApplicationAssociate associate, Kind kind,
        BeanManager beanManager, Object key, Supplier<Bean<?>> resolver
    )
    {
        CdiBeanCache cdiBeanCache = cdiBeanCache(associate, beanManager);
        return cdiBeanCache == null ? resolver.get() : cdiBeanCache.cached(kind, key, resolver);
    }

    /**
     * Applies the Faces annotations a managed converter declares, such as {@code FacesConverter} on a superclass. The annotation manager belongs to the
     * application, so a converter resolved from a thread that reached none is returned without this pass.
     */
    private static void applyConverterAnnotations(ApplicationAssociate associate, Converter<?> managedConverter) {
        if (associate != null) {
            associate.getAnnotationManager().applyConverterAnnotations(FacesContext.getCurrentInstance(), managedConverter);
        }
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
        Bean<?> bean = cachedBean(
            currentAssociate(), Kind.BEHAVIOR, beanManager, value,
            () -> resolveBean(beanManager, Behavior.class, FacesBehavior.Literal.of(value, false))
        );

        if (bean == null) {
            return null;
        }

        return (Behavior) getReferenceInstance(beanManager, bean.getBeanClass(), bean);
    }

    /**
     * Obtain the programmatic facelet the application declares for the given view id with {@link View}.
     *
     * @param beanManager the bean manager.
     * @param viewId the view id.
     * @return the facelet, or null if the application declares none for that view id.
     */
    public static Facelet getViewFacelet(BeanManager beanManager, String viewId) {
        Bean<?> bean = cachedBean(
            currentAssociate(), Kind.VIEW_FACELET, beanManager, viewId,
            () -> resolveBean(beanManager, Facelet.class, View.Literal.of(viewId))
        );

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
        Bean<?> bean = cachedBean(
            currentAssociate(), Kind.VALIDATOR, beanManager, value,
            () -> resolveValidatorBean(beanManager, value)
        );

        if (bean == null) {
            return null;
        }

        return (Validator<?>) getReferenceInstance(beanManager, bean.getBeanClass(), bean);
    }

    private static Bean<?> resolveValidatorBean(BeanManager beanManager, String value) {
        // Try the parameterized validator type first, then the raw type.
        Annotation qualifier = FacesValidator.Literal.of(value, false, false);
        Bean<?> bean = resolveBean(beanManager, VALIDATOR_TYPE, qualifier);
        if (bean == null) {
            bean = resolveBean(beanManager, Validator.class, qualifier);
        }
        if (bean == null) {
            // Still nothing found: try the default qualifier with value as the bean name.
            Annotation defaultQualifier = FacesValidator.Literal.of("", false, false);
            bean = resolveBean(beanManager, VALIDATOR_TYPE, value, defaultQualifier);
            if (bean == null) {
                bean = resolveBean(beanManager, Validator.class, value, defaultQualifier);
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
        return cachedBean(
            currentAssociate(), Kind.RESOLVED, beanManager, new BeanLookupKey(type, beanName, new HashSet<>(Arrays.asList(qualifiers))),
            () -> resolveBeanUncached(beanManager, type, beanName, qualifiers)
        );
    }

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
     * Resolves the {@link Bean} for the given required type and qualifiers using the application's cache. Returns {@code null} if no bean matches. Unlike
     * {@link #getBeanReference}, this does not invoke {@code getReference} -- the caller is responsible for that when an instance is needed, so scope semantics
     * remain correct on each invocation.
     */
    public static Bean<?> resolveBean(BeanManager beanManager, Type type, Annotation... qualifiers) {
        return resolveBean(beanManager, type, null, qualifiers);
    }

    /**
     * Resolves the {@link Bean} for the given EL name using the application's cache. Returns {@code null} if no bean has that name.
     */
    public static Bean<?> resolveBeanByName(BeanManager beanManager, String name) {
        return cachedBean(
            currentAssociate(), Kind.RESOLVED, beanManager, new BeanLookupKey(null, name, Collections.emptySet()),
            () -> beanManager.resolve(beanManager.getBeans(name))
        );
    }

    /**
     * Resolves and caches the {@link FacesContextProducer}-typed {@link FacesContext} bean once per {@link BeanManager} of each application. Used by the
     * per-request {@link FacesContext#release()} destruction path: the producer is registered exactly once per application, so re-running the type-containment
     * filter on every request is wasted work.
     */
    public static Bean<?> resolveFacesContextProducerBean(BeanManager beanManager) {
        CdiBeanCache cdiBeanCache = cdiBeanCache(currentAssociate(), beanManager);
        return cdiBeanCache == null
            ? resolveFacesContextProducerBeanUncached(beanManager)
            : cdiBeanCache.cachedFacesContextProducerBean(() -> resolveFacesContextProducerBeanUncached(beanManager));
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
            }
            else {
                return context.get(bean);
            }
        }
        else {
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
     * @return An Optional that contains an instance of annotation type for which was searched if the annotated contained this.
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
            }
            catch (Exception e) {
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

            e -> dataModel.add(cdi.select(e.getValue(), FacesDataModel.Literal.of(e.getKey())).get())
        );

        return dataModel.isEmpty() ? null : dataModel.get(0);
    }

    @SuppressWarnings("unchecked")
    public static Map<Class<?>, Class<? extends DataModel<?>>> getDataModelClassesMap(CDI<Object> cdi) {
        BeanManager beanManager = cdi.getBeanManager();

        // Get the Map with classes for which a custom DataModel implementation is available from CDI
        Bean<?> bean = resolveBeanByName(beanManager, DataModelClassesMapProducer.DATA_MODEL_CLASSES_MAP_BEAN_NAME);
        Object beanReference = beanManager.getReference(bean, Map.class, beanManager.createCreationalContext(bean));

        return (Map<Class<?>, Class<? extends DataModel<?>>>) beanReference;
    }

    /**
     * Returns the current injection point.
     *
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
     *
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
     *
     * @param <S> the type of given scope
     * @param scope the scope to be checked
     * @return whether given scope is active
     */
    public static <S extends Annotation> boolean isScopeActive(Class<S> scope) {
        BeanManager beanManager = Util.getCdiBeanManager(FacesContext.getCurrentInstance());

        try {
            Context context = beanManager.getContext(scope);
            return context.isActive();
        }
        catch (ContextNotActiveException ignore) {
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
