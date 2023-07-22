/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

import static com.sun.faces.cdi.CdiUtils.addAnnotatedTypes;
import static com.sun.faces.cdi.CdiUtils.getAnnotation;
import static java.util.Collections.unmodifiableMap;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.push.WebsocketChannelManager;
import com.sun.faces.push.WebsocketSessionManager;
import com.sun.faces.push.WebsocketUserManager;
import com.sun.faces.util.FacesLogger;

import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.AnnotatedField;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.BeforeBeanDiscovery;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessBean;
import jakarta.enterprise.inject.spi.ProcessManagedBean;
import jakarta.faces.annotation.ManagedProperty;
import jakarta.faces.model.DataModel;
import jakarta.faces.model.FacesDataModel;

/**
 * The CDI extension.
 */
public class CdiExtension implements Extension {

    /**
     * Array of Mojarra impl specific CDI managed bean classes to add to CDI.
     * This is necessary in case Mojarra is provided by server instead of by webapp as is often the case in Jakarta EE environments.
     */
    private static final Class<?>[] MOJARRA_MANAGED_BEANS = {
            WebsocketUserManager.class,
            WebsocketSessionManager.class,
            WebsocketChannelManager.class,
            WebsocketChannelManager.ViewScope.class,
            InjectionPointGenerator.class,
            WebsocketPushContextProducer.class
    };

    /**
     * Map of classes that can be wrapped by a data model to data model implementation classes
     */
    private Map<Class<?>, Class<? extends DataModel<?>>> forClassToDataModelClass = new HashMap<>();

    /**
     * Map of {@code @ManagedProperty} target types
     */
    private final Set<Type> managedPropertyTargetTypes = new HashSet<>();

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = FacesLogger.APPLICATION_VIEW.getLogger();

    // As per CDI spec this is the invocation order:
    //  1. BeforeBeanDiscovery
    //  2. ProcessAnnotatedType and ProcessSyntheticAnnotatedType
    //  3. AfterTypeDiscovery
    //  4. ProcessInjectionTarget and ProcessProducer
    //  5. ProcessInjectionPoint
    //  6. ProcessBeanAttributes
    //  7. ProcessBean, ProcessManagedBean, ProcessSessionBean, ProcessProducerMethod, ProcessProducerField and ProcessSyntheticBean
    //  8. ProcessObserverMethod and ProcessSyntheticObserverMethod
    //  9. AfterBeanDiscovery
    // 10. AfterDeploymentValidation

    /**
     * BeforeBeanDiscovery:
     * <ul>
     * <li>add impl specific managed beans
     * </ul>
     *
     * @param beforeBeanDiscovery the before bean discovery.
     * @param beanManager the bean manager.
     */
    public void beforeBeanDiscovery(@Observes BeforeBeanDiscovery beforeBeanDiscovery, BeanManager beanManager) {
        addAnnotatedTypes(beforeBeanDiscovery, beanManager, MOJARRA_MANAGED_BEANS);
    }

    /**
     * ProcessBean:
     * <ul>
     * <li>if bean is annotated with {@code @FacesDataModel} then collect it for {@link #afterDeploymentValidation(AfterDeploymentValidation, BeanManager)}
     * </ul>
     *
     * @param <T> the generic bean type
     * @param processBeanEvent the process bean event
     * @param beanManager the current bean manager
     */
    @SuppressWarnings("unchecked")
    public <T extends DataModel<?>> void processBean(@Observes ProcessBean<T> processBeanEvent, BeanManager beanManager) {
        try {
            ProcessBean<T> event = processBeanEvent; // JDK8 u60 workaround - https://web.archive.org/web/20161007164846/http://mail.openjdk.java.net/pipermail/lambda-dev/2015-August/012146.html/012146.html

            getAnnotation(beanManager, event.getAnnotated(), FacesDataModel.class)
                .ifPresent(model -> forClassToDataModelClass.put(model.forClass(), (Class<T>) event.getBean().getBeanClass()));
        }
        catch (Exception e) {
            // Log and continue; if we are not allowed somehow to investigate this ManagedBean, we're unlikely to be interested in it anyway,
            // but logging at WARNING level is important
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.warning("Exception happened during processBean: " + e);
            }
        }
    }

    /**
     * ProcessManagedBean:
     * <ul>
     * <li>if bean has field with {@code @ManagedProperty} then collect its type for {@link #afterBeanDiscovery(AfterBeanDiscovery, BeanManager)}
     * </ul>
     *
     * @param <T> the generic bean type
     * @param processManagedBeanEvent the process managed bean event
     * @param beanManager the current bean manager
     */
    public <T> void processManagedBean(@Observes ProcessManagedBean<T> processManagedBeanEvent, BeanManager beanManager) {
        try {
            for (AnnotatedField<? super T> field : processManagedBeanEvent.getAnnotatedBeanClass().getFields()) {
                Type type = field.getBaseType();

                if (field.isAnnotationPresent(ManagedProperty.class) && (type instanceof Class || type instanceof ParameterizedType)) {
                    managedPropertyTargetTypes.add(type);
                }
            }
        }
        catch (Exception e) {
            // Log and continue; if we are not allowed somehow to investigate this ManagedBean, we're unlikely to be interested in it anyway,
            // but logging at WARNING level is important
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.warning("Exception happened during processManagedBean: " + e);
            }
        }
    }

    /**
     * AfterBeanDiscovery:
     * <ul>
     * <li>add all CDI producer beans allowing EL resolving of Faces specific artifacts
     * <li>add a managed property type producer bean for each managed property type discovered in {@link #processManagedBean(ProcessManagedBean, BeanManager)}
     * </ul>
     *
     * @param afterBeanDiscovery the after bean discovery.
     * @param beanManager the bean manager.
     */
    public void afterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {

        // Ideally below should only happen if Jakarta Faces is considered active,
        // but this is not detectable as ServletContext is not necessarily available at this moment.

        afterBeanDiscovery.addBean(new ApplicationProducer());
        afterBeanDiscovery.addBean(new ApplicationMapProducer());
        afterBeanDiscovery.addBean(new CompositeComponentProducer());
        afterBeanDiscovery.addBean(new ComponentProducer());
        afterBeanDiscovery.addBean(new FlashProducer());
        afterBeanDiscovery.addBean(new FlowMapProducer());
        afterBeanDiscovery.addBean(new HeaderMapProducer());
        afterBeanDiscovery.addBean(new HeaderValuesMapProducer());
        afterBeanDiscovery.addBean(new InitParameterMapProducer());
        afterBeanDiscovery.addBean(new RequestParameterMapProducer());
        afterBeanDiscovery.addBean(new RequestParameterValuesMapProducer());
        afterBeanDiscovery.addBean(new RequestProducer());
        afterBeanDiscovery.addBean(new RequestMapProducer());
        afterBeanDiscovery.addBean(new ResourceHandlerProducer());
        afterBeanDiscovery.addBean(new ExternalContextProducer());
        afterBeanDiscovery.addBean(new FacesContextProducer());
        afterBeanDiscovery.addBean(new RequestCookieMapProducer());
        afterBeanDiscovery.addBean(new SessionProducer());
        afterBeanDiscovery.addBean(new SessionMapProducer());
        afterBeanDiscovery.addBean(new ViewMapProducer());
        afterBeanDiscovery.addBean(new ViewProducer());
        afterBeanDiscovery.addBean(new DataModelClassesMapProducer());

        for (Type type : managedPropertyTargetTypes) {
            afterBeanDiscovery.addBean(new ManagedPropertyProducer(type, beanManager));
        }
    }

    /**
     * AfterDeploymentValidation:
     * <ul>
     * <li>sort faces data models discovered in {@link #processBean(ProcessBean, BeanManager)} for use by {@link DataModelClassesMapProducer}
     * </ul>
     *
     * @param event the after deployment validation event
     * @param beanManager the current bean manager
     */
    public void afterDeploymentValidation(@Observes AfterDeploymentValidation event, BeanManager beanManager) {

        // Sort the classes wrapped by a DataModel that we collected in processBean() such that
        // for any 2 classes X and Y from this collection, if an object of X is an instanceof an object of Y,
        // X appears in the collection before Y. The collection's sorting is otherwise arbitrary.
        //
        // E.g.
        //
        // Given class B, class A extends B and class Q, two possible orders are;
        // 1. {A, B, Q}
        // 2. {Q, A, B}
        //
        // The only requirement here is that A appears before B, since A is a subclass of B.
        //
        // This sorting is used so given an instance of type Z that's being bound to a UIData or UIRepeat
        // component, we can find the most specific DataModel that can wrap Z by iterating through the sorted
        // collection from beginning to end and stopping this iteration at the first match.

        List<Class<?>> sortedForDataModelClasses = new ArrayList<>();
        for (Class<?> clazz : forClassToDataModelClass.keySet()) {
            int highestSuper = -1;
            boolean added = false;
            for (int i = 0; i < sortedForDataModelClasses.size(); i++) {
                if (sortedForDataModelClasses.get(i).isAssignableFrom(clazz)) {
                    sortedForDataModelClasses.add(i, clazz);
                    added = true;
                    break;
                } else if (clazz.isAssignableFrom(sortedForDataModelClasses.get(i))) {
                    highestSuper = i;
                }
            }
            if (!added) {
                if (highestSuper > -1) {
                    sortedForDataModelClasses.add(highestSuper + 1, clazz);
                } else {
                    sortedForDataModelClasses.add(clazz);
                }
            }
        }

        // Use the sorting computed above to order the Map on this. Note that a linked hash map is used
        // to preserve this ordering.

        Map<Class<?>, Class<? extends DataModel<?>>> linkedForClassToDataModelClass = new LinkedHashMap<>();
        for (Class<?> sortedClass : sortedForDataModelClasses) {
            linkedForClassToDataModelClass.put(sortedClass, forClassToDataModelClass.get(sortedClass));
        }

        forClassToDataModelClass = unmodifiableMap(linkedForClassToDataModelClass);
    }

    /**
     * Gets the map of classes that can be wrapped by a data model to data model implementation classes
     *
     * @return Map of classes that can be wrapped by a data model to data model implementation classes
     */
    public Map<Class<?>, Class<? extends DataModel<?>>> getForClassToDataModelClass() {
        return forClassToDataModelClass;
    }
}
