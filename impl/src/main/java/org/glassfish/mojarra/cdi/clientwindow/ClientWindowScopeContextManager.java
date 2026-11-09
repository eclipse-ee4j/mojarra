/*
 * Copyright (c) 2021, 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.mojarra.cdi.clientwindow;

import static java.util.logging.Level.FINEST;
import static org.glassfish.mojarra.context.SessionMap.getMutex;
import static org.glassfish.mojarra.util.Util.getCdiBeanManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;

import org.glassfish.mojarra.config.FacesContextParam;
import org.glassfish.mojarra.config.MojarraContextParam;
import org.glassfish.mojarra.util.FacesLogger;
import org.glassfish.mojarra.util.LRUMap;

/**
 * The manager that deals with CDI ClientWindowScoped beans.
 */
public class ClientWindowScopeContextManager {

    private static final Logger LOGGER = FacesLogger.CLIENTWINDOW.getLogger();

    private static final String CLIENT_WINDOW_CONTEXTS = "org.glassfish.mojarra.cdi.clientwindow.clientWindowContexts";

    private final BeanManager beanManager;
    private final boolean distributable;
    private final int numberOfClientWindows;

    public ClientWindowScopeContextManager() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        beanManager = getCdiBeanManager(facesContext);
        distributable = MojarraContextParam.ENABLE_DISTRIBUTABLE.isEnabled(facesContext);
        numberOfClientWindows = FacesContextParam.NUMBER_OF_CLIENT_WINDOWS.getInt(facesContext);
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
        LOGGER.log(FINEST, "Creating @ClientWindowScoped CDI bean using contextual: {0}", contextual);

        if (!(contextual instanceof PassivationCapable)) {
            throw new IllegalArgumentException("ClientWindowScoped bean " + contextual.toString() + " must be PassivationCapable, but is not.");
        }

        T contextualInstance = contextual.create(creational);

        if (contextualInstance != null) {
            String passivationCapableId = ((PassivationCapable) contextual).getId();

            getContextMap(facesContext).put(passivationCapableId, new ClientWindowScopeContextObject<>(passivationCapableId, contextualInstance, creational));
        }

        return contextualInstance;
    }

    /**
     * Get the value from the ClientWindow-map (or null if not found).
     *
     * @param <T> the type.
     * @param facesContext the faces context.
     * @param contextual the contextual.
     * @return the value or null if not found.
     */
    @SuppressWarnings("unchecked")
    public <T> T getBean(FacesContext facesContext, Contextual<T> contextual) {
        T result = null;
        Map<String, ClientWindowScopeContextObject<?>> contextMap = getContextMap(facesContext);

        if (contextMap != null) {
            if (!(contextual instanceof PassivationCapable)) {
                throw new IllegalArgumentException("ClientWindowScoped bean " + contextual.toString() + " must be PassivationCapable, but is not.");
            }

            ClientWindowScopeContextObject<T> contextObject = (ClientWindowScopeContextObject<T>) contextMap.get(((PassivationCapable) contextual).getId());

            if (contextObject != null) {
                return contextObject.getContextualInstance();
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
    private Map<String, ClientWindowScopeContextObject<?>> getContextMap(FacesContext facesContext) {
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
    private Map<String, ClientWindowScopeContextObject<?>> getContextMap(FacesContext facesContext, boolean create) {
        Map<String, ClientWindowScopeContextObject<?>> result = null;

        ExternalContext externalContext = facesContext.getExternalContext();
        if (externalContext != null) {
            Map<String, Object> sessionMap = externalContext.getSessionMap();
            Object session = externalContext.getSession(create);

            if (session != null) {
                Map<Object, Map<String, ClientWindowScopeContextObject<?>>> clientWindowScopeContexts = (Map<Object, Map<String, ClientWindowScopeContextObject<?>>>) sessionMap
                    .get(CLIENT_WINDOW_CONTEXTS);
                String clientWindowId = getCurrentClientWindowId(facesContext);

                if (clientWindowScopeContexts == null && create) {
                    synchronized (getMutex(session)) {
                        sessionMap.put(CLIENT_WINDOW_CONTEXTS, Collections.synchronizedMap(new LRUMap<>(numberOfClientWindows)));
                    }
                }

                if (clientWindowScopeContexts != null && clientWindowId != null && create) {
                    Map<String, ClientWindowScopeContextObject<?>> evictedContextMap = null;

                    synchronized (clientWindowScopeContexts) {
                        if (!clientWindowScopeContexts.containsKey(clientWindowId)) {
                            evictedContextMap = evictEldestContextMap(clientWindowScopeContexts);
                            clientWindowScopeContexts.put(clientWindowId, new ConcurrentHashMap<>());
                            if (distributable) {
                                // If we are distributable, this will result in a dirtying of the
                                // session data, forcing replication. If we are not distributable,
                                // this is a no-op.
                                sessionMap.put(CLIENT_WINDOW_CONTEXTS, clientWindowScopeContexts);
                            }
                        }
                    }

                    if (evictedContextMap != null) {
                        destroyBeans(evictedContextMap);
                    }
                }

                if (clientWindowScopeContexts != null && clientWindowId != null) {
                    result = clientWindowScopeContexts.get(clientWindowId);
                }
            }
        }

        return result;
    }

    /**
     * Remove the context map of the least recently used client window when there is no room for another one. The caller must hold the lock on the given map.
     *
     * @param clientWindowScopeContexts the access ordered context maps of all client windows of the session.
     * @return the removed context map, or null when there is still room.
     */
    private Map<String, ClientWindowScopeContextObject<?>> evictEldestContextMap(
        Map<Object, Map<String, ClientWindowScopeContextObject<?>>> clientWindowScopeContexts
    )
    {
        if (clientWindowScopeContexts.size() < numberOfClientWindows) {
            return null;
        }

        Iterator<Map<String, ClientWindowScopeContextObject<?>>> contextMaps = clientWindowScopeContexts.values().iterator();
        Map<String, ClientWindowScopeContextObject<?>> eldestContextMap = contextMaps.next();
        contextMaps.remove();
        return eldestContextMap;
    }

    /**
     * Destroy the client window scoped beans of the given context map. A bean which cannot be destroyed is logged and skipped.
     */
    private void destroyBeans(Map<String, ClientWindowScopeContextObject<?>> contextMap) {
        for (ClientWindowScopeContextObject<?> contextObject : contextMap.values()) {
            try {
                destroyBean(contextObject);
            }
            catch (RuntimeException e) {
                LOGGER.log(Level.WARNING, "Cannot destroy the @ClientWindowScoped bean: " + contextObject.getPassivationCapableId(), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> void destroyBean(ClientWindowScopeContextObject<T> contextObject) {
        Contextual<T> contextual = (Contextual<T>) beanManager.getPassivationCapableBean(contextObject.getPassivationCapableId());
        CreationalContext<T> creationalContext = contextObject.getCreationalContext();
        // Absent in a session which was serialized by a version which did not store it yet.
        contextual
            .destroy(contextObject.getContextualInstance(), creationalContext != null ? creationalContext : beanManager.createCreationalContext(contextual));
    }

    /**
     * Called when a session destroyed.
     *
     * @param httpSessionEvent the HTTP session event.
     */
    @SuppressWarnings("unchecked")
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        if (LOGGER.isLoggable(FINEST)) {
            LOGGER.log(FINEST, "Cleaning up session for CDI @ClientWindowScoped beans");
        }

        HttpSession session = httpSessionEvent.getSession();

        Map<Object, Map<String, ClientWindowScopeContextObject<?>>> clientWindowScopeContexts = (Map<Object, Map<String, ClientWindowScopeContextObject<?>>>) session
            .getAttribute(CLIENT_WINDOW_CONTEXTS);
        if (clientWindowScopeContexts != null) {
            List<Map<String, ClientWindowScopeContextObject<?>>> contextMaps;

            synchronized (clientWindowScopeContexts) {
                contextMaps = new ArrayList<>(clientWindowScopeContexts.values());
                clientWindowScopeContexts.clear();
            }

            contextMaps.forEach(this::destroyBeans);
            session.removeAttribute(CLIENT_WINDOW_CONTEXTS);
        }
    }

    protected String getCurrentClientWindowId(FacesContext facesContext) {
        return facesContext.getExternalContext().getClientWindow().getId();
    }

}
