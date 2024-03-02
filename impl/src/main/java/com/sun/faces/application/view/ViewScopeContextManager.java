/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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
// Portions Copyright [2018] [Payara Foundation and/or its affiliates]

package com.sun.faces.application.view;

import static com.sun.faces.application.view.ViewScopeManager.VIEW_MAP_ID;
import static com.sun.faces.cdi.CdiUtils.getBeanReference;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableDistributable;
import static com.sun.faces.context.SessionMap.getMutex;
import static com.sun.faces.util.Util.getCdiBeanManager;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.WARNING;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.util.FacesLogger;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;

/**
 * The manager that deals with CDI ViewScoped beans.
 */
public class ViewScopeContextManager {

    private static final Logger LOGGER = FacesLogger.APPLICATION_VIEW.getLogger();

    /**
     * Stores the constant to keep track of all the active view scope contexts.
     */
    private static final String ACTIVE_VIEW_CONTEXTS = "com.sun.faces.application.view.activeViewContexts";

    private final BeanManager beanManager;
    private final boolean distributable;

    public ViewScopeContextManager() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        beanManager = getCdiBeanManager(facesContext);
        distributable = WebConfiguration.getInstance(facesContext.getExternalContext())
                                        .isOptionEnabled(EnableDistributable);
    }

    /**
     * Clear the current view map using the Faces context.
     *
     * @param facesContext the Faces context.
     */
    public void clear(FacesContext facesContext) {
        LOGGER.log(FINEST, "Clearing @ViewScoped CDI beans for current view map");

        Map<String, ViewScopeContextObject> contextMap = getContextMap(facesContext, false);
        if (contextMap != null) {
            destroyBeans(facesContext.getViewRoot().getViewMap(false), contextMap);
        }
    }

    /**
     * Clear the given view map. Use the version with the viewMapId.
     *
     * @param facesContext the Faces context.
     * @param viewMap the given view map.
     */
    @Deprecated
    public void clear(FacesContext facesContext, Map<String, Object> viewMap) {
        String viewMapId = ViewScopeManager.locateViewMapId(facesContext, viewMap);
        if (viewMapId != null) {
            clear(facesContext, viewMapId, viewMap);
        } else {
            LOGGER.log(WARNING, "Cannot locate the view map to clear in the active maps: {0}", viewMap);
        }
    }

    /**
     * Clear the given view map.
     *
     * @param facesContext the Faces context.
     * @param viewMapId The ID of the view map
     * @param viewMap the given view map.
     */
    public void clear(FacesContext facesContext, String viewMapId, Map<String, Object> viewMap) {
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.log(FINEST, "Clearing @ViewScoped CDI beans for given view map: {0}");
        }
        Map<String, ViewScopeContextObject> contextMap = getContextMap(facesContext, viewMapId);
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
        LOGGER.log(FINEST, "Creating @ViewScoped CDI bean using contextual: {0}", contextual);

        if (!(contextual instanceof PassivationCapable)) {
            throw new IllegalArgumentException("ViewScoped bean " + contextual.toString() + " must be PassivationCapable, but is not.");
        }

        T contextualInstance = contextual.create(creational);

        if (contextualInstance != null) {
            String name = getName(contextualInstance);
            facesContext.getViewRoot().getViewMap(true).put(name, contextualInstance);
            String passivationCapableId = ((PassivationCapable) contextual).getId();

            getContextMap(facesContext).put(passivationCapableId, new ViewScopeContextObject(passivationCapableId, name));
        }

        return contextualInstance;
    }

    /**
     * Destroy the view scoped beans for the given view and context map.
     *
     * @param viewMap the view map.
     * @param contextMap the context map.
     */
    private void destroyBeans(Map<String, Object> viewMap, Map<String, ViewScopeContextObject> contextMap) {
        ArrayList<String> removalNameList = new ArrayList<>();

        if (contextMap != null) {
            for (Map.Entry<String, ViewScopeContextObject> entry : contextMap.entrySet()) {
                String passivationCapableId = entry.getKey();
                Contextual contextual = beanManager.getPassivationCapableBean(passivationCapableId);

                ViewScopeContextObject contextObject = entry.getValue();
                CreationalContext creationalContext = beanManager.createCreationalContext(contextual);
                // We can no longer get this from the contextObject. Instead we must call
                // beanManager.createCreationalContext(contextual)
                Object contextualInstance = viewMap.get(contextObject.getName());

                // Contextual instance may be null if already removed from view map (and thus already destroyed).
                // This can happen when a mid-request navigation happens and a new view root is being set, and then
                // in the same request a session.invalidate is called.
                // See https://github.com/javaserverfaces/mojarra/issues/3454
                // Also see https://github.com/payara/Payara/issues/2506 for why we can't just clean the contextMap
                // (it contains abstract descriptors for all instances, not just the one we want to destroy here).
                if (contextualInstance != null) {
                    contextual.destroy(contextualInstance, creationalContext);
                }

                removalNameList.add(contextObject.getName());
            }

            Iterator<String> removalNames = removalNameList.iterator();
            while (removalNames.hasNext()) {
                String name = removalNames.next();
                viewMap.remove(name);
            }

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
    @SuppressWarnings("unchecked")
    public <T> T getBean(FacesContext facesContext, Contextual<T> contextual) {
        T result = null;
        Map<String, ViewScopeContextObject> contextMap = getContextMap(facesContext);

        if (contextMap != null) {
            if (!(contextual instanceof PassivationCapable)) {
                throw new IllegalArgumentException("ViewScoped bean " + contextual.toString() + " must be PassivationCapable, but is not.");
            }

            ViewScopeContextObject contextObject = contextMap.get(((PassivationCapable) contextual).getId());

            if (contextObject != null) {
                String name = contextObject.getName();
                LOGGER.log(FINEST, "Getting value for @ViewScoped bean with name: {0}", name);
                result = (T) facesContext.getViewRoot().getViewMap(true).get(name);
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
    @SuppressWarnings("unchecked")
    private Map<String, ViewScopeContextObject> getContextMap(FacesContext facesContext, boolean create) {
        Map<String, ViewScopeContextObject> result = null;

        ExternalContext externalContext = facesContext.getExternalContext();
        if (externalContext != null) {
            Map<String, Object> sessionMap = externalContext.getSessionMap();
            Object session = externalContext.getSession(create);

            if (session != null) {
                Map<Object, Map<String, ViewScopeContextObject>> activeViewScopeContexts = (Map<Object, Map<String, ViewScopeContextObject>>)
                    sessionMap.get(ACTIVE_VIEW_CONTEXTS);
                Map<String, Object> viewMap = facesContext.getViewRoot().getViewMap(false);
                String viewMapId = (String) facesContext.getViewRoot().getTransientStateHelper().getTransient(VIEW_MAP_ID);

                if (activeViewScopeContexts == null && create) {
                    synchronized (getMutex(session)) {
                        activeViewScopeContexts = new ConcurrentHashMap<>();
                        sessionMap.put(ACTIVE_VIEW_CONTEXTS, activeViewScopeContexts);
                    }
                }

                if (activeViewScopeContexts != null && viewMapId != null && create) {
                    synchronized (activeViewScopeContexts) {
                        if (!activeViewScopeContexts.containsKey(viewMapId)) {
                            activeViewScopeContexts.put(viewMapId,
                                    new ConcurrentHashMap<String, ViewScopeContextObject>());
                            if (distributable) {
                                // If we are distributable, this will result in a dirtying of the
                                // session data, forcing replication. If we are not distributable,
                                // this is a no-op.
                                sessionMap.put(ACTIVE_VIEW_CONTEXTS, activeViewScopeContexts);
                            }
                        }
                    }
                }

                if (activeViewScopeContexts != null && viewMapId != null) {
                    result = activeViewScopeContexts.get(viewMapId);
                }
            }
        }

        return result;
    }

    /**
     * Get the context map.
     *
     * @param facesContext the Faces context.
     * @param viewMapId The viewMapId of the map.
     * @return the context map.
     */
    private Map<String, ViewScopeContextObject> getContextMap(FacesContext facesContext, String viewMapId) {
        Map<String, ViewScopeContextObject> result = null;

        ExternalContext externalContext = facesContext.getExternalContext();
        if (externalContext != null) {
            Map<String, Object> sessionMap = externalContext.getSessionMap();
            @SuppressWarnings("unchecked")
            Map<Object, Map<String, ViewScopeContextObject>> activeViewScopeContexts =
                    (Map<Object, Map<String, ViewScopeContextObject>>) sessionMap.get(ACTIVE_VIEW_CONTEXTS);

            if (activeViewScopeContexts != null) {
                result = activeViewScopeContexts.get(viewMapId);
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
        String name = instance.getClass().getSimpleName().substring(0, 1).toLowerCase() + instance.getClass().getSimpleName().substring(1);

        Named named = instance.getClass().getAnnotation(Named.class);
        if (named != null && named.value() != null && !named.value().trim().equals("")) {
            name = named.value();
        }

        return name;
    }

    /**
     * Called when a session destroyed.
     *
     * @param httpSessionEvent the HTTP session event.
     */
    @SuppressWarnings("unchecked")
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.log(FINEST, "Cleaning up session for CDI @ViewScoped beans");
        }

        HttpSession session = httpSessionEvent.getSession();

        Map<Object, Map<String, ViewScopeContextObject>> activeViewScopeContexts = (Map<Object, Map<String, ViewScopeContextObject>>)
                session.getAttribute(ACTIVE_VIEW_CONTEXTS);
        if (activeViewScopeContexts != null) {
            Map<String, Object> activeViewMaps = (Map<String, Object>) session.getAttribute(ViewScopeManager.ACTIVE_VIEW_MAPS);
            if (activeViewMaps != null) {
                for (Map.Entry<String, Object> viewMapEntry : activeViewMaps.entrySet()) {
                    Map<String, ViewScopeContextObject> contextMap = activeViewScopeContexts.get(viewMapEntry.getKey());
                    destroyBeans((Map<String, Object>) viewMapEntry.getValue(), contextMap);
                }
            }

            activeViewScopeContexts.clear();
            session.removeAttribute(ACTIVE_VIEW_CONTEXTS);
        }
    }

    public void fireInitializedEvent(FacesContext facesContext, UIViewRoot root) {
        getBeanReference(beanManager, ViewScopedCDIEventFireHelperImpl.class).fireInitializedEvent(root);
    }

    public void fireDestroyedEvent(FacesContext facesContext, UIViewRoot root) {
        getBeanReference(beanManager, ViewScopedCDIEventFireHelperImpl.class).fireDestroyedEvent(root);
    }
}
