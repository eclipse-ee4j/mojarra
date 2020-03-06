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

import static com.sun.faces.cdi.CdiUtils.addAnnotatedTypes;
import static com.sun.faces.cdi.CdiUtils.getAnnotation;
import static jakarta.faces.annotation.FacesConfig.Version.JSF_2_3;
import static java.util.Collections.unmodifiableMap;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import jakarta.faces.annotation.FacesConfig;
import jakarta.faces.annotation.ManagedProperty;
import jakarta.faces.model.DataModel;
import jakarta.faces.model.FacesDataModel;

/**
 * The CDI extension.
 */
public class CdiExtension implements Extension {

    /**
     * Map of classes that can be wrapped by a data model to data model implementation classes
     */
    private Map<Class<?>, Class<? extends DataModel<?>>> forClassToDataModelClass = new HashMap<>();

    private Set<Type> managedPropertyTargetTypes = new HashSet<>();

    private boolean addBeansForJSFImplicitObjects;

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = FacesLogger.APPLICATION_VIEW.getLogger();

    /**
     * Before bean discovery.
     *
     * @param beforeBeanDiscovery the before bean discovery.
     * @param beanManager the bean manager.
     */
    public void beforeBean(@Observes BeforeBeanDiscovery beforeBeanDiscovery, BeanManager beanManager) {
        addAnnotatedTypes(beforeBeanDiscovery, beanManager, WebsocketUserManager.class, WebsocketSessionManager.class, WebsocketChannelManager.class,
                WebsocketChannelManager.ViewScope.class, InjectionPointGenerator.class, WebsocketPushContextProducer.class);
    }

    /**
     * After bean discovery.
     *
     * @param afterBeanDiscovery the after bean discovery.
     */
    public void afterBean(final @Observes AfterBeanDiscovery afterBeanDiscovery, BeanManager beanManager) {
        if (addBeansForJSFImplicitObjects) {
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
        }

        afterBeanDiscovery.addBean(new DataModelClassesMapProducer());

        for (Type type : managedPropertyTargetTypes) {
            afterBeanDiscovery.addBean(new ManagedPropertyProducer(type, beanManager));
        }
    }

    /**
     * Processing of beans
     *
     * @param event the process bean event
     * @param beanManager the current bean manager
     */
    @SuppressWarnings("unchecked")
    public <T extends DataModel<?>> void processBean(@Observes ProcessBean<T> event, BeanManager beanManager) {

        Optional<FacesDataModel> optionalFacesDataModel = getAnnotation(beanManager, event.getAnnotated(), FacesDataModel.class);
        if (optionalFacesDataModel.isPresent()) {
            forClassToDataModelClass.put(optionalFacesDataModel.get().forClass(), (Class<? extends DataModel<?>>) event.getBean().getBeanClass());
        }
    }

    public <T> void collect(@Observes ProcessManagedBean<T> eventIn, BeanManager beanManager) {

        try {
            ProcessManagedBean<T> event = eventIn; // JDK8 u60 workaround

            getAnnotation(beanManager, event.getAnnotated(), FacesConfig.class)
                    .ifPresent(config -> setAddBeansForJSFImplicitObjects(config.version().ordinal() >= JSF_2_3.ordinal()));

            for (AnnotatedField<? super T> field : event.getAnnotatedBeanClass().getFields()) {
                if (field.isAnnotationPresent(ManagedProperty.class)
                        && (field.getBaseType() instanceof Class || field.getBaseType() instanceof ParameterizedType)) {
                    managedPropertyTargetTypes.add(field.getBaseType());
                }
            }
        } catch (Exception e) {
            // Log and continue; if we are not allowed somehow to investigate this ManagedBean, we're unlikely to be interested in
            // it anyway
            // but logging at SEVERE level is important
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.warning("Exception happened when collecting: " + e);
            }
        }
    }

    /**
     * After deployment validation
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

    public boolean isAddBeansForJSFImplicitObjects() {
        return addBeansForJSFImplicitObjects;
    }

    public void setAddBeansForJSFImplicitObjects(boolean addBeansForJSFImplicitObjects) {
        this.addBeansForJSFImplicitObjects = addBeansForJSFImplicitObjects;
    }

}
