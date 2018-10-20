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

import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

/**
 * The manager that deals with CDI ViewScoped beans.
 */
public class ViewScopeContextManager {

    private static final Logger LOGGER = FacesLogger.APPLICATION_VIEW.getLogger();
    private boolean isCdiOneOneOrGreater;
    private Class viewScopedCDIEventFireHelperImplClass;
    /**
     * Stores the constant to keep track of all the active view scope contexts.
     */
    private static final String ACTIVE_VIEW_CONTEXTS = "com.sun.faces.application.view.activeViewContexts";
    /**
     * Stores the constants to keep track of the active view maps.
     */
    private static final String ACTIVE_VIEW_MAPS = "com.sun.faces.application.view.activeViewMaps";
    private final BeanManager beanManager;

    public ViewScopeContextManager() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        isCdiOneOneOrGreater = Util.isCdiOneOneOrLater(facesContext);
        try {
            viewScopedCDIEventFireHelperImplClass = Class.forName("com.sun.faces.application.view.ViewScopedCDIEventFireHelperImpl");
        } catch (ClassNotFoundException ex) {
            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, "CDI 1.1 events not enabled", ex);
            }
        }
        beanManager = (BeanManager) Util.getCdiBeanManager(facesContext);
    }

    /**
     * Clear the current view map using the Faces context.
     *
     * @param facesContext the Faces context.
     */
    public void clear(FacesContext facesContext) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Clearing @ViewScoped CDI beans for current view map");
        }
        Map<String, Object> viewMap = facesContext.getViewRoot().getViewMap(false);
        Map<String, ViewScopeContextObject> contextMap = getContextMap(facesContext, false);
        if (contextMap != null) {
            destroyBeans(viewMap, contextMap);
        }
    }

    /**
     * Clear the given view map.
     *
     * @param facesContext the Faces context.
     * @param viewMap the given view map.
     */
    public void clear(FacesContext facesContext, Map<String, Object> viewMap) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Clearing @ViewScoped CDI beans for given view map: {0}");
        }
        Map<String, ViewScopeContextObject> contextMap = getContextMap(facesContext, viewMap);
        if (contextMap != null) {
            destroyBeans(viewMap, contextMap);
        }
    }

    /**
     * Create the bean.
     *
     * @param <T> the type.
     * @param facesContext the faces context.
     * @param contextual the contextual.
     * @param creational the creational.
     * @return the value or null if not found.
     */
    public <T> T createBean(FacesContext facesContext, Contextual<T> contextual, CreationalContext<T> creational) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Creating @ViewScoped CDI bean using contextual: {0}", contextual);
        }

        if (!(contextual instanceof PassivationCapable)) {
            throw new IllegalArgumentException("ViewScoped bean " + contextual.toString() + " must be PassivationCapable, but is not.");
        }
            
        T result = contextual.create(creational);

        if (result != null) {
            String name = getName(result);
            facesContext.getViewRoot().getViewMap(true).put(name, result);
            String passivationCapableId = ((PassivationCapable)contextual).getId();
            
            getContextMap(facesContext).put(passivationCapableId, 
                    new ViewScopeContextObject(passivationCapableId, name));
        }

        return result;
    }

    /**
     * Destroy the view scoped beans for the given view and context map.
     *
     * @param viewMap the view map.
     * @param contextMap the context map.
     */
    private void destroyBeans(
            Map<String, Object> viewMap, Map<String, ViewScopeContextObject> contextMap) {
        ArrayList<String> removalNameList = new ArrayList<>();

        if (contextMap != null) {
            for (Map.Entry<String, ViewScopeContextObject> entry : contextMap.entrySet()) {
                String passivationCapableId = entry.getKey();
                Contextual contextual = beanManager.getPassivationCapableBean(passivationCapableId);
                
                ViewScopeContextObject contextObject = entry.getValue();
                CreationalContext creationalContext = beanManager.createCreationalContext(contextual);
                // We can no longer get this from the contextObject. Instead we must call
                // beanManager.createCreationalContext(contextual)
                contextual.destroy(viewMap.get(contextObject.getName()), creationalContext);
                removalNameList.add(contextObject.getName());
            }

            Iterator<String> removalNames = removalNameList.iterator();
            while (removalNames.hasNext()) {
                String name = removalNames.next();
                viewMap.remove(name);
            }
            
            contextMap.clear();
        }
    }

    /**
     * Get the value from the view map (or null if not found).
     *
     * @param <T> the type.
     * @param facesContext the faces context.
     * @param contextual the contextual.
     * @return the value or null if not found.
     */
    public <T> T getBean(FacesContext facesContext, Contextual<T> contextual) {
        T result = null;
        Map<String, ViewScopeContextObject> contextMap = getContextMap(facesContext);

        if (contextMap != null) {
            if (!(contextual instanceof PassivationCapable)) {
                throw new IllegalArgumentException("ViewScoped bean " + contextual.toString() + " must be PassivationCapable, but is not.");
            }
            
            ViewScopeContextObject contextObject = contextMap.get(((PassivationCapable)contextual).getId());

            if (contextObject != null) {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.log(Level.FINEST, "Getting value for @ViewScoped bean with name: {0}", contextObject.getName());
                }
                result = (T) facesContext.getViewRoot().getViewMap(true).get(contextObject.getName());
            }
        }

        return result;
    }

    /**
     * Get the context map.
     *
     * @param facesContext the Faces context.
     * @return the context map.
     */
    private Map<String, ViewScopeContextObject> getContextMap(FacesContext facesContext) {
        return getContextMap(facesContext, true);
    }

    /**
     * Get the context map.
     *
     * @param facesContext the Faces context.
     * @param create flag to indicate if we are creating the context map.
     * @return the context map.
     */
    private Map<String, ViewScopeContextObject> getContextMap(FacesContext facesContext, boolean create) {
        Map<String, ViewScopeContextObject> result = null;

        ExternalContext externalContext = facesContext.getExternalContext();
        if (externalContext != null) {
            Map<String, Object> sessionMap = externalContext.getSessionMap();
            Object session = externalContext.getSession(create);

            if (session != null) {
                Map<Object, Map<String, ViewScopeContextObject>> activeViewScopeContexts =
                        (Map<Object, Map<String, ViewScopeContextObject>>) sessionMap.get(ACTIVE_VIEW_CONTEXTS);
                Map<String, Object> viewMap = facesContext.getViewRoot().getViewMap(false);

                if (activeViewScopeContexts == null && create) {
                    synchronized (session) {
                        activeViewScopeContexts = new ConcurrentHashMap<>();
                        sessionMap.put(ACTIVE_VIEW_CONTEXTS, activeViewScopeContexts);
                    }
                }

                if (activeViewScopeContexts != null && create) {
                    synchronized (activeViewScopeContexts) {
                        if (!activeViewScopeContexts.containsKey(System.identityHashCode(viewMap)) && create) {
                            // since viewMap identity may have changed, copy view scope contexts from the session
                            copyViewScopeContextsFromSession(activeViewScopeContexts, viewMap);
                            // If we are distributable, this will result in a dirtying of the
                            // session data, forcing replication.  If we are not distributable,
                            // this is a no-op.
                            sessionMap.put(ACTIVE_VIEW_CONTEXTS, activeViewScopeContexts);

                        }
                    }
                }

                if (activeViewScopeContexts != null) {
                    result = activeViewScopeContexts.get(System.identityHashCode(viewMap));
                }
            }
        }

        return result;
    }
    
    /**
     * Copies view-scope context from the session, in case the view map identity has changed,
     * which is the case when cluster failover or a session-saving reload occurs
     */
    private void copyViewScopeContextsFromSession(Map<Object, Map<String, ViewScopeContextObject>> contexts,
            Map<String, Object> viewMap) {
        if(viewMap == null) {
            return;
        }
        Set<Object> toReplace = new HashSet<>();
        Map<String, ViewScopeContextObject> resultMap = new ConcurrentHashMap<>();
        // try to copy a view map from the session, in case of a failover or a restart
        for(Map.Entry<Object, Map<String, ViewScopeContextObject>> contextEntry : contexts.entrySet()) {
            Set<String> beanNames = new HashSet<>();
            // gather all bean names from the session's context
            for(ViewScopeContextObject viewObject : contextEntry.getValue().values()) {
                beanNames.add(viewObject.getName());
            }
            for(String name : beanNames) {
                // mark all contexts that are in the view map for copying
                if(viewMap.keySet().contains(name)) {                    
                    toReplace.add(contextEntry.getKey());
                    break;
                }
            }
        }
        for(Object key : toReplace) {  
            Map<String, ViewScopeContextObject> contextObject = contexts.get(key);
            contexts.remove(key);
            resultMap.putAll(contextObject);
        }
        contexts.put(System.identityHashCode(viewMap), resultMap);
    }

    /**
     * Get the context map.
     *
     * @param facesContext the Faces context.
     * @param create flag to indicate if we are creating the context map.
     * @return the context map.
     */
    private Map<String, ViewScopeContextObject> getContextMap(FacesContext facesContext, Map<String, Object> viewMap) {
        Map<String, ViewScopeContextObject> result = null;

        ExternalContext externalContext = facesContext.getExternalContext();
        if (externalContext != null) {
            Map<String, Object> sessionMap = externalContext.getSessionMap();
            Map<Object, Map<String, ViewScopeContextObject>> activeViewScopeContexts =
                    (Map<Object, Map<String, ViewScopeContextObject>>) sessionMap.get(ACTIVE_VIEW_CONTEXTS);

            if (activeViewScopeContexts != null) {
                result = activeViewScopeContexts.get(System.identityHashCode(viewMap));
            }
        }

        return result;
    }
    
    /**
     * Get the name of the bean for the given object.
     *
     * @param instance the object.
     * @return the name.
     */
    private String getName(Object instance) {
        String name = instance.getClass().getSimpleName().substring(0, 1).toLowerCase()
                + instance.getClass().getSimpleName().substring(1);

        Named named = instance.getClass().getAnnotation(Named.class);
        if (named != null && named.value() != null && !named.value().trim().equals("")) {
            name = named.value();
        }
        return name;
    }

    /**
     * Called when a session destroyed.
     *
     * @param hse the HTTP session event.
     */
    public void sessionDestroyed(HttpSessionEvent hse) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Cleaning up session for CDI @ViewScoped beans");
        }

        HttpSession session = hse.getSession();

        Map<Object, Map<String, ViewScopeContextObject>> activeViewScopeContexts =
                (Map<Object, Map<String, ViewScopeContextObject>>) session.getAttribute(ACTIVE_VIEW_CONTEXTS);

        if (activeViewScopeContexts != null) {
            Map<String, Object> activeViewMaps = (Map<String, Object>) session.getAttribute(ACTIVE_VIEW_MAPS);
            if (activeViewMaps != null) {
                Iterator<Object> activeViewMapsIterator = activeViewMaps.values().iterator();
                while (activeViewMapsIterator.hasNext()) {
                    Map<String, Object> instanceMap = (Map<String, Object>) activeViewMapsIterator.next();
                    Map<String, ViewScopeContextObject> contextMap =
                            activeViewScopeContexts.get(System.identityHashCode(instanceMap));
                    destroyBeans(instanceMap, contextMap);
                }
            }

            activeViewScopeContexts.clear();
            session.removeAttribute(ACTIVE_VIEW_CONTEXTS);
        }
    }

    public void fireInitializedEvent(FacesContext facesContext, UIViewRoot root) {
        if (isCdiOneOneOrGreater && null != viewScopedCDIEventFireHelperImplClass) {
            BeanManager beanManager = (BeanManager) Util.getCdiBeanManager(facesContext);
            if (null != beanManager) {
                Set<Bean<?>> availableBeans = beanManager.getBeans(viewScopedCDIEventFireHelperImplClass);
                if (null != availableBeans && !availableBeans.isEmpty()) {
                    Bean<?> bean = beanManager.resolve(availableBeans);
                    CreationalContext<?> creationalContext =
                            beanManager.createCreationalContext(null);
                    ViewScopedCDIEventFireHelper eventHelper =
                            (ViewScopedCDIEventFireHelper) beanManager.getReference(bean, bean.getBeanClass(),
                            creationalContext);
                    eventHelper.fireInitializedEvent(root);
                }
            }

        }

    }

    public void fireDestroyedEvent(FacesContext facesContext, UIViewRoot root) {
        if (isCdiOneOneOrGreater && null != viewScopedCDIEventFireHelperImplClass) {
            BeanManager beanManager = (BeanManager) Util.getCdiBeanManager(facesContext);
            if (null != beanManager) {
                Set<Bean<?>> availableBeans = beanManager.getBeans(viewScopedCDIEventFireHelperImplClass);
                if (null != availableBeans && !availableBeans.isEmpty()) {
                    Bean<?> bean = beanManager.resolve(availableBeans);
                    CreationalContext<?> creationalContext =
                            beanManager.createCreationalContext(null);
                    ViewScopedCDIEventFireHelper eventHelper =
                            (ViewScopedCDIEventFireHelper) beanManager.getReference(bean, bean.getBeanClass(),
                            creationalContext);
                    eventHelper.fireDestroyedEvent(root);
                }
            }
        }

    }
}
