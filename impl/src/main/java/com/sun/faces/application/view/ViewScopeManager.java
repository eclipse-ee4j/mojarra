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

package com.sun.faces.application.view;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.config.WebConfiguration;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableDistributable;
import com.sun.faces.mgbean.BeanManager;
import com.sun.faces.util.LRUMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.application.ProjectStage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PostConstructViewMapEvent;
import javax.faces.event.PreDestroyViewMapEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.ViewMapListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

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
    private ViewScopeContextManager contextManager;
    
    private boolean distributable;

    /**
     * Constructor.
     */
    public ViewScopeManager() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            contextManager = new ViewScopeContextManager();
        } catch (Throwable throwable) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, "CDI @ViewScoped bean functionality unavailable");
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "CDI @ViewScoped manager unavailable", throwable);
            }
        }
        WebConfiguration config = WebConfiguration.getInstance(context.getExternalContext());
        distributable = config.isOptionEnabled(EnableDistributable);
        
    }

    /**
     * Static method that locates the ID for a view map in the active view maps
     * stored in the session. It just performs a == over the view map because
     * it should be the same object.
     *
     * @param facesContext The faces context
     * @param viewMap The view to locate
     * @return
     */
    protected static String locateViewMapId(FacesContext facesContext, Map<String, Object> viewMap) {
        Object session = facesContext.getExternalContext().getSession(true);
        if (session != null) {
            Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
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
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Clearing @ViewScoped beans from current view map");
        }

        if (contextManager != null) {
            contextManager.clear(facesContext);
        }

        destroyBeans(facesContext, facesContext.getViewRoot().getViewMap(false));
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
            LOGGER.log(Level.WARNING, "Cannot locate the view map to clear in the active maps: {0}", viewMap);
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
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Clearing @ViewScoped beans from view map: {0}", viewMap);
        }

        if (contextManager != null) {
            contextManager.clear(facesContext, viewMapId, viewMap);
        }

        destroyBeans(facesContext, viewMap);
    }

    /**
     * Destroy the managed beans from the given view map.
     *
     * @param applicationAssociate the application associate.
     * @param viewMap the view map.
     */
    private void destroyBeans(ApplicationAssociate applicationAssociate, Map<String, Object> viewMap) {
        for (Map.Entry<String, Object> entry : viewMap.entrySet()) {
            String name = entry.getKey();
            Object bean = entry.getValue();

            try {
                if (applicationAssociate != null) {
                    BeanManager beanManager = applicationAssociate.getBeanManager();
                    if (beanManager != null && beanManager.isManaged(name)) {
                        beanManager.destroy(name, bean);
                    }
                }
            } catch (Exception exception) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING, "Error calling @PreDestroy on bean with name: " + name, exception);
                }
            }
        }
    }

    /**
     * Destroy the managed beans from the given view map.
     *
     * @param facesContext the Faces Context.
     * @param viewMap the view map.
     */
    public void destroyBeans(FacesContext facesContext, Map<String, Object> viewMap) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Destroying @ViewScoped beans from view map: {0}", viewMap);
        }
        ApplicationAssociate applicationAssociate = ApplicationAssociate.getInstance(facesContext.getExternalContext());
        if (applicationAssociate != null) {
            destroyBeans(applicationAssociate, viewMap);
        }
    }

    /**
     * Get the CDI context manager.
     *
     * @return the CDI context manager.
     */
    ViewScopeContextManager getContextManager() {
        return this.contextManager;
    }

    /**
     * Get our instance.
     *
     * @param facesContext the FacesContext.
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
        return (source instanceof UIViewRoot);
    }

    /**
     * Process the system event.
     *
     * @param se the system event.
     * @throws AbortProcessingException when processing needs to be aborter.
     */
    @Override
    public void processEvent(SystemEvent se) throws AbortProcessingException {
        if (se instanceof PreDestroyViewMapEvent) {
            processPreDestroyViewMap(se);
        }

        if (se instanceof PostConstructViewMapEvent) {
            processPostConstructViewMap(se);
        }
    }

    /**
     * Process the PostConstructViewMap system event.
     *
     * @param se the system event.
     */
    private void processPostConstructViewMap(SystemEvent se) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Handling PostConstructViewMapEvent");
        }
        
        UIViewRoot viewRoot = (UIViewRoot) se.getSource();
        Map<String, Object> viewMap = viewRoot.getViewMap(false);

        if (viewMap != null) {            
            FacesContext facesContext = FacesContext.getCurrentInstance();
            
            if (viewRoot.isTransient() && facesContext.isProjectStage(ProjectStage.Development)) {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "@ViewScoped beans are not supported on stateless views",
                    "@ViewScoped beans are not supported on stateless views");
                facesContext.addMessage(viewRoot.getClientId(facesContext), message);                

                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING, "@ViewScoped beans are not supported on stateless views");
                }
            }
            
            Object session = facesContext.getExternalContext().getSession(true);

            if (session != null) {
                Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
                Integer size = (Integer) sessionMap.get(ACTIVE_VIEW_MAPS_SIZE);
                if (size == null) {
                    size = 25;
                }

                if (sessionMap.get(ACTIVE_VIEW_MAPS) == null) {
                    sessionMap.put(ACTIVE_VIEW_MAPS, (Map<String, Object>) Collections.synchronizedMap(new LRUMap<String, Object>(size)));
                }

                Map<String, Object> viewMaps = (Map<String, Object>) sessionMap.get(ACTIVE_VIEW_MAPS);
                synchronized (viewMaps) {
                    String viewMapId = UUID.randomUUID().toString();
                    while (viewMaps.containsKey(viewMapId)) {
                        viewMapId = UUID.randomUUID().toString();
                    }

                    if (viewMaps.size() == size) {
                        String eldestViewMapId = viewMaps.keySet().iterator().next();
                        Map<String, Object> eldestViewMap = (Map<String, Object>) viewMaps.remove(eldestViewMapId);
                        removeEldestViewMap(facesContext, eldestViewMapId, eldestViewMap);
                    }

                    viewMaps.put(viewMapId, viewMap);
                    viewRoot.getTransientStateHelper().putTransient(VIEW_MAP_ID, viewMapId);
                    viewRoot.getTransientStateHelper().putTransient(VIEW_MAP, viewMap);
                    if (distributable) {
                        // If we are distributable, this will result in a dirtying of the
                        // session data, forcing replication.  If we are not distributable,
                        // this is a no-op.
                        sessionMap.put(ACTIVE_VIEW_MAPS, viewMaps);
                    }
                }
                if (null != contextManager) {
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
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Handling PreDestroyViewMapEvent");
        }
        
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
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Creating session for @ViewScoped beans");
        }
    }
    
    /**
     * Destroy the associated data in the session.
     *
     * @param hse the HTTP session event.
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent hse) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Cleaning up session for @ViewScoped beans");
        }

        if (contextManager != null) {
            contextManager.sessionDestroyed(hse);
        }

        HttpSession session = hse.getSession();
        Map<String, Object> activeViewMaps = (Map<String, Object>) session.getAttribute(ACTIVE_VIEW_MAPS);
        if (activeViewMaps != null) {
            Iterator<Object> activeViewMapsIterator = activeViewMaps.values().iterator();
            ApplicationAssociate applicationAssociate = ApplicationAssociate.getInstance(hse.getSession().getServletContext());
            while (activeViewMapsIterator.hasNext()) {
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
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "Removing eldest view map: {0}", eldestViewMap);
        }

        if (contextManager != null) {
            contextManager.clear(facesContext, viewMapId, eldestViewMap);
        }

        destroyBeans(facesContext, eldestViewMap);
    }
}
