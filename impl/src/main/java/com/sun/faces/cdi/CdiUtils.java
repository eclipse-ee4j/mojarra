/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.CDI;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.TypeLiteral;
import javax.faces.component.behavior.Behavior;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.model.DataModel;
import javax.faces.validator.Validator;

import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

/**
 * A static utility class for CDI.
 */
public final class CdiUtils {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = FacesLogger.APPLICATION_VIEW.getLogger();

    private final static Type CONVERTER_TYPE = 
        new TypeLiteral<Converter<?>>() { private static final long serialVersionUID = 1L;}.getType();
        
    private final static Type VALIDATOR_TYPE = 
        new TypeLiteral<Validator<?>>() { private static final long serialVersionUID = 1L;}.getType();

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
        Converter<?> managedConverter = createConverter(beanManager, new FacesConverterAnnotationLiteral(value, Object.class));
        
        if (managedConverter != null) {
            return new CdiConverter(value, Object.class, managedConverter);
        }
       
        return null;
    }

    /**
     * Create a converter using the FacesConverter forClass attribute.
     *
     * @param beanManager the bean manager.
     * @param forClass the for class.
     * @return the converter, or null if we could not match one.
     */
    public static Converter<?> createConverter(BeanManager beanManager, Class<?> forClass) {
        Converter<?> managedConverter = null;

        for (
       	    Class<?> forClassOrSuperclass = forClass; 
       	    managedConverter == null && forClassOrSuperclass != null && forClassOrSuperclass != Object.class;
       	    forClassOrSuperclass = forClassOrSuperclass.getSuperclass()
        ) {
       	    managedConverter = createConverter(beanManager, new FacesConverterAnnotationLiteral("", forClassOrSuperclass));
        }

        if (managedConverter != null) {
            return new CdiConverter("", forClass, managedConverter);
        }
       
        return null;
    }
    
    private static Converter<?> createConverter(BeanManager beanManager, Annotation qualifier) {
        
        // Try to find parameterized converter first     
        Converter<?> managedConverter = (Converter<?>) getBeanReferenceByType(
            beanManager,
            CONVERTER_TYPE,
            qualifier);
        
        if (managedConverter == null) {
            // No parameterized converter, try raw converter            
            managedConverter = getBeanReference(
                beanManager,
                Converter.class,
                qualifier);
        }
        
        return managedConverter;
    }

    /**
     * Create a behavior using the FacesBehavior value attribute.
     * 
     * @param beanManager the bean manager.
     * @param value the value attribute.
     * @return the behavior, or null if we could not match one.
     */
    public static Behavior createBehavior(BeanManager beanManager, String value) {
        Behavior delegatingBehavior = null;
        
        Behavior managedBehavior = getBeanReference(
            beanManager,
            Behavior.class,
            new FacesBehaviorAnnotationLiteral(value)
        );
        
        if (managedBehavior != null) {
            delegatingBehavior = new CdiBehavior(value, managedBehavior);
        }
        
        return delegatingBehavior;
    }
    
    /**
     * Create a validator using the FacesValidator value attribute.
     *
     * @param beanManager the bean manager.
     * @param value the value attribute.
     * @return the validator, or null if we could not match one.
     */
    public static Validator<?> createValidator(BeanManager beanManager, String value) {
        
        Annotation qualifier = new FacesValidatorAnnotationLiteral(value);
        
        // Try to find parameterized validator first  
        Validator<?> managedValidator = (Validator<?>) getBeanReferenceByType(
            beanManager,
            VALIDATOR_TYPE,
            qualifier);
                
                
        if (managedValidator == null) {
            // No parameterized validator, try raw validator
            managedValidator = getBeanReference(
                beanManager,
                Validator.class,
                qualifier);
        }
        
        if (managedValidator != null) {
            return new CdiValidator(value, managedValidator);
        }
    
        return null;
    }

    public static <T> T getBeanReference(Class<T> type, Annotation... qualifiers) {
        return type.cast(getBeanReferenceByType(Util.getCdiBeanManager(FacesContext.getCurrentInstance()), type, qualifiers));
    }

    /**
     * @param beanManager the bean manager
     * @param type the required bean type the reference must have
     * @param qualifier the required qualifiers the reference must have
     * @return a bean reference adhering to the required type and qualifiers
     */
    public static <T> T getBeanReference(BeanManager beanManager, Class<T> type, Annotation... qualifiers) {
        return type.cast(getBeanReferenceByType(beanManager, type, qualifiers));
    }

    public static Object getBeanReferenceByType(BeanManager beanManager, Type type, Annotation... qualifiers) {

        Object beanReference = null;

        Bean<?> bean = beanManager.resolve(beanManager.getBeans(type, qualifiers));
        if (bean != null) {
            beanReference = beanManager.getReference(bean, type, beanManager.createCreationalContext(bean));
        }

        return beanReference;
    }

    /**
     * Returns concrete (non-proxied) bean instance of given class in current context.
     * 
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
                    annotations.addAll(
                        beanManager.getStereotypeDefinition(
                             annotation.annotationType()
                        )
                     );
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
        
        List<DataModel<?>> dataModel = new ArrayList<DataModel<?>>(1);
        CDI<Object> cdi = CDI.current();
        
        // Scan the map in order, the first class that is a super class or equal to the class for which
        // we're looking for a DataModel is the closest match, since the Map is sorted on inheritance relation
        getDataModelClassesMap(cdi).entrySet().stream()
            .filter(e -> e.getKey().isAssignableFrom(forClass))
            .findFirst()
            .ifPresent(
                    
                 // Get the bean from CDI which is of the class type that we found during annotation scanning
                 // and has the @FacesDataModel annotation, with the "forClass" attribute set to the closest
                 // super class of our target class.
                    
                e -> dataModel.add(
                    cdi.select(
                        e.getValue(),
                        new FacesDataModelAnnotationLiteral(e.getKey())
                    ).get())
            );
        
        return dataModel.isEmpty()? null : dataModel.get(0);
    }
    
    @SuppressWarnings("unchecked")
    public static Map<Class<?>, Class<? extends DataModel<?>>> getDataModelClassesMap(CDI<Object> cdi) {
        BeanManager beanManager = cdi.getBeanManager();

        // Get the Map with classes for which a custom DataModel implementation is available from CDI
        Bean<?> bean = beanManager.resolve(beanManager.getBeans("comSunFacesDataModelClassesMap"));
        Object beanReference = beanManager.getReference(bean, Map.class, beanManager.createCreationalContext(bean));
        
        return (Map<Class<?>, Class<? extends DataModel<?>>>) beanReference;
    }

    /** 
     * Returns the current injection point.
     */
    public static InjectionPoint getCurrentInjectionPoint(BeanManager beanManager, CreationalContext<?> creationalContext) {
        Bean<? extends Object> bean = beanManager.resolve(beanManager.getBeans(InjectionPoint.class));
        InjectionPoint injectionPoint = (InjectionPoint) beanManager.getReference(bean, InjectionPoint.class, creationalContext);

        if (injectionPoint == null) { // It's broken in some Weld versions. Below is a work around. 
            bean = beanManager.resolve(beanManager.getBeans(InjectionPointGenerator.class));
            injectionPoint = (InjectionPoint) beanManager.getInjectableReference(bean.getInjectionPoints().iterator().next(), creationalContext);
        }

        return injectionPoint;
    }

    /**
     * Returns the qualifier annotation of the given qualifier class from the given injection point.
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

}
