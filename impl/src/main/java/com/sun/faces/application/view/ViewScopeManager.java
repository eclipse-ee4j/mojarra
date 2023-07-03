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

package com.sun.faces.application.view;

import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableDistributable;
import static com.sun.faces.config.WebConfiguration.WebContextInitParameter.NumberOfActiveViewMaps;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.WARNING;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.util.LRUMap;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.PostConstructViewMapEvent;
import jakarta.faces.event.PreDestroyViewMapEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.ViewMapListener;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * The manager that deals with non-CDI and CDI ViewScoped beans.
 */
public class ViewScopeManager implements HttpSessionListener, ViewMapListener {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ViewScopeManager.class.getName());
    /**
     * Stores the constants to keep track of the active view maps.
     */
    public static final String ACTIVE_VIEW_MAPS = "com.sun.faces.application.view.activeViewMaps";
    /**
     * Stores the constant for the maximum active view map size.
     */
    public static final String ACTIVE_VIEW_MAPS_SIZE = "com.sun.faces.application.view.activeViewMapsSize";
    /**
     * Stores the view map.
     */
    public static final String VIEW_MAP = "com.sun.faces.application.view.viewMap";
    /**
     * Stores the view map id.
     */
    public static final String VIEW_MAP_ID = "com.sun.faces.application.view.viewMapId";
    /**
     * Stores the constant to keep track of the ViewScopeManager.
     */
    public static final String VIEW_SCOPE_MANAGER = "com.sun.faces.application.view.viewScopeManager";
    /**
     * Stores the CDI context manager.
     */
    private final ViewScopeContextManager contextManager;

    private final boolean distributable;
    
    private Integer numberOfActiveViewMapsInWebXml;

    /**
     * Constructor.
     */
    public ViewScopeManager() {
        FacesContext context = FacesContext.getCurrentInstance();
        contextManager = new ViewScopeContextManager();
        WebConfiguration config = WebConfiguration.getInstance(context.getExternalContext());
        distributable = config.isOptionEnabled(EnableDistributable);

        String numberOfActiveViewMapsAsString = config.getOptionValue(NumberOfActiveViewMaps);
        if (numberOfActiveViewMapsAsString != null) {
            try {
                numberOfActiveViewMapsInWebXml = Integer.parseInt(numberOfActiveViewMapsAsString);
            }
            catch (NumberFormatException e) {
                if (LOGGER.isLoggable(WARNING)) {
                    LOGGER.log(WARNING, "Cannot parse " + NumberOfActiveViewMaps.getQualifiedName(), e);
                }
            }
        }
    }
    
    /**
     * Static method that locates the ID for a view map in the active view maps
     * stored in the session. It just performs a == over the view map because
     * it should be the same object.
     *
     * @param facesContext The faces context
     * @param viewMap The view to locate
     * @return located ID
     */
    protected static String locateViewMapId(FacesContext facesContext, Map<String, Object> viewMap) {
        Object session = facesContext.getExternalContext().getSession(true);
        
        if (session != null) {
            Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
            @SuppressWarnings("unchecked")
            Map<String, Object> viewMaps = (Map<String, Object>) sessionMap.get(ACTIVE_VIEW_MAPS);
            if (viewMaps != null) {
                for (Map.Entry<String,Object> entry : viewMaps.entrySet()) {
                    if (viewMap == entry.getValue()) {
                        return entry.getKey();
                    }
                }
            }
        }
        
        return null;
    }

    /**
     * Clear the current view map using the Faces context.
     *
     * @param facesContext the Faces context.
     */
    public void clear(FacesContext facesContext) {
        LOGGER.log(FINEST, "Clearing @ViewScoped beans from current view map");

        if (contextManager != null) {
            contextManager.clear(facesContext);
        }
    }

    /**
     * Clear the given view map. Use the version with viewMapId.
     *
     * @param facesContext the Faces context.
     * @param viewMap the view map.
     */
    @Deprecated
    public void clear(FacesContext facesContext, Map<String, Object> viewMap) {
        String viewMapId = locateViewMapId(facesContext, viewMap);
        if (viewMapId != null) {
            this.clear(facesContext, viewMapId, viewMap);
        } else {
            LOGGER.log(WARNING, "Cannot locate the view map to clear in the active maps: {0}", viewMap);
        }
    }
    
    /**
     * Clear the given view map.
     *
     * @param facesContext the Faces context.
     * @param viewMapId The ID of the view map
     * @param viewMap the view map.
     */
    public void clear(FacesContext facesContext, String viewMapId, Map<String, Object> viewMap) {
        LOGGER.log(FINEST, "Clearing @ViewScoped beans from view map: {0}", viewMap);

        if (contextManager != null) {
            contextManager.clear(facesContext, viewMapId, viewMap);
        }

        destroyBeans(facesContext, viewMap);
    }
    
    /**
     * Destroy the managed beans from the given view map.
     *
     * @param facesContext the Faces Context.
     * @param viewMap the view map.
     */
    public void destroyBeans(FacesContext facesContext, Map<String, Object> viewMap) {
        LOGGER.log(FINEST, "Destroying @ViewScoped beans from view map: {0}", viewMap);
        
        ApplicationAssociate applicationAssociate = ApplicationAssociate.getInstance(facesContext.getExternalContext());
        if (applicationAssociate != null) {
            destroyBeans(applicationAssociate, viewMap);
        }
    }
    
    /**
     * Destroy the managed beans from the given view map.
     *
     * @param applicationAssociate the application associate.
     * @param viewMap the view map.
     */
    private void destroyBeans(ApplicationAssociate applicationAssociate, Map<String, Object> viewMap) {
    }

    /**
     * Get the CDI context manager.
     *
     * @return the CDI context manager.
     */
    ViewScopeContextManager getContextManager() {
        return contextManager;
    }

    /**
     * Get our instance.
     *
     * @param facesContext the FacesContext.
     * @return our instance.
     */
    public static ViewScopeManager getInstance(FacesContext facesContext) {
        if (!facesContext.getExternalContext().getApplicationMap().containsKey(VIEW_SCOPE_MANAGER)) {
            facesContext.getExternalContext().getApplicationMap().put(VIEW_SCOPE_MANAGER, new ViewScopeManager());
        }
        return (ViewScopeManager) facesContext.getExternalContext().getApplicationMap().get(VIEW_SCOPE_MANAGER);
    }

    /**
     * Is a listener for the given source.
     *
     * @param source the source.
     * @return true if UIViewRoot, false otherwise.
     */
    @Override
    public boolean isListenerForSource(Object source) {
        return source instanceof UIViewRoot;
    }

    /**
     * Process the system event.
     *
     * @param systemEvent the system event.
     * @throws AbortProcessingException when processing needs to be aborted.
     */
    @Override
    public void processEvent(SystemEvent systemEvent) throws AbortProcessingException {
        if (systemEvent instanceof PreDestroyViewMapEvent) {
            processPreDestroyViewMap(systemEvent);
        }

        if (systemEvent instanceof PostConstructViewMapEvent) {
            processPostConstructViewMap(systemEvent);
        }
    }

    /**
     * Process the PostConstructViewMap system event.
     *
     * @param systemEvent the system event.
     */
    private void processPostConstructViewMap(SystemEvent systemEvent) {
        LOGGER.log(FINEST, "Handling PostConstructViewMapEvent");

        UIViewRoot viewRoot = (UIViewRoot) systemEvent.getSource();
        Map<String, Object> viewMap = viewRoot.getViewMap(false);

        if (viewMap != null) {
            FacesContext facesContext = FacesContext.getCurrentInstance();

            if (viewRoot.isTransient() && facesContext.isProjectStage(ProjectStage.Development)) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN, "@ViewScoped beans are not supported on stateless views",
                        "@ViewScoped beans are not supported on stateless views");
                facesContext.addMessage(viewRoot.getClientId(facesContext), message);

                LOGGER.log(WARNING, "@ViewScoped beans are not supported on stateless views");
            }

            Object session = facesContext.getExternalContext().getSession(true);

            if (session != null) {
                Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
                Integer size = (Integer) sessionMap.get(ACTIVE_VIEW_MAPS_SIZE);
                if (size == null) {
                    size = numberOfActiveViewMapsInWebXml;
                    
                    if (size == null) {
                        size = Integer.parseInt(NumberOfActiveViewMaps.getDefaultValue());
                    }
                }

                if (sessionMap.get(ACTIVE_VIEW_MAPS) == null) {
                    sessionMap.put(ACTIVE_VIEW_MAPS, Collections.synchronizedMap(new LRUMap<String, Object>(size)));
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> viewMaps = (Map<String, Object>) sessionMap.get(ACTIVE_VIEW_MAPS);
                synchronized (viewMaps) {
                    String viewMapId = UUID.randomUUID().toString();
                    while (viewMaps.containsKey(viewMapId)) {
                        viewMapId = UUID.randomUUID().toString();
                    }

                    if (viewMaps.size() == size) {
                        String eldestViewMapId = viewMaps.keySet().iterator().next();
                        @SuppressWarnings("unchecked")
                        Map<String, Object> eldestViewMap = (Map<String, Object>) viewMaps.remove(eldestViewMapId);
                        removeEldestViewMap(facesContext, eldestViewMapId, eldestViewMap);
                    }

                    viewMaps.put(viewMapId, viewMap);
                    viewRoot.getTransientStateHelper().putTransient(VIEW_MAP_ID, viewMapId);
                    viewRoot.getTransientStateHelper().putTransient(VIEW_MAP, viewMap);
                    if (distributable) {
                        // If we are distributable, this will result in a dirtying of the
                        // session data, forcing replication. If we are not distributable,
                        // this is a no-op.
                        sessionMap.put(ACTIVE_VIEW_MAPS, viewMaps);
                    }
                }

                if (contextManager != null) {
                    contextManager.fireInitializedEvent(facesContext, viewRoot);
                }
            }
        }
    }

    /**
     * Process the PreDestroyViewMap system event.
     *
     * @param se the system event.
     */
    private void processPreDestroyViewMap(SystemEvent se) {
        LOGGER.log(FINEST, "Handling PreDestroyViewMapEvent");
        
        UIViewRoot viewRoot = (UIViewRoot) se.getSource();
        Map<String, Object> viewMap = viewRoot.getViewMap(false);
        String viewMapId = (String) viewRoot.getTransientStateHelper().getTransient(VIEW_MAP_ID);

        if (viewMap != null && viewMapId != null && !viewMap.isEmpty()) {
            FacesContext facesContext = FacesContext.getCurrentInstance();

            if (contextManager != null) {
                contextManager.clear(facesContext, viewMapId, viewMap);
                contextManager.fireDestroyedEvent(facesContext, viewRoot);
            }

            destroyBeans(facesContext, viewMap);

        }
    }

    /**
     * Create the associated data in the session (if any).
     *
     * @param se the HTTP session event.
     */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        LOGGER.log(FINEST, "Creating session for @ViewScoped beans");
    }

    /**
     * Destroy the associated data in the session.
     *
     * @param httpSessionEvent the HTTP session event.
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        LOGGER.log(FINEST, "Cleaning up session for @ViewScoped beans");

        if (contextManager != null) {
            contextManager.sessionDestroyed(httpSessionEvent);
        }

        HttpSession session = httpSessionEvent.getSession();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> activeViewMaps = (Map<String, Object>) session.getAttribute(ACTIVE_VIEW_MAPS);
        if (activeViewMaps != null) {
            Iterator<Object> activeViewMapsIterator = activeViewMaps.values().iterator();
            ApplicationAssociate applicationAssociate = ApplicationAssociate.getInstance(httpSessionEvent.getSession().getServletContext());
            while (activeViewMapsIterator.hasNext()) {
                @SuppressWarnings("unchecked")
                Map<String, Object> viewMap = (Map<String, Object>) activeViewMapsIterator.next();
                destroyBeans(applicationAssociate, viewMap);
            }

            activeViewMaps.clear();
            session.removeAttribute(ACTIVE_VIEW_MAPS);
            session.removeAttribute(ACTIVE_VIEW_MAPS_SIZE);
        }
    }

    /**
     * Remove the eldest view map from the active view maps.
     *
     * @param facesContext the context
     * @param viewMapId the view map id
     * @param eldestViewMap the eldest view map.
     */
    private void removeEldestViewMap(FacesContext facesContext, String viewMapId, Map<String, Object> eldestViewMap) {
        LOGGER.log(FINEST, "Removing eldest view map: {0}", eldestViewMap);

        if (contextManager != null) {
            contextManager.clear(facesContext, viewMapId, eldestViewMap);
        }

        destroyBeans(facesContext, eldestViewMap);
    }
}
