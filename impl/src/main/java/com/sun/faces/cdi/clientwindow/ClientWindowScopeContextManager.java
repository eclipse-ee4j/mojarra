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

package com.sun.faces.cdi.clientwindow;

import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableDistributable;
import static java.util.logging.Level.FINEST;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import com.sun.faces.config.WebConfiguration;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.LRUMap;

import jakarta.enterprise.context.spi.Contextual;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.PassivationCapable;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.lifecycle.ClientWindow;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;

/**
 * The manager that deals with CDI ClientWindowScoped beans.
 */
public class ClientWindowScopeContextManager {

    private static final Logger LOGGER = FacesLogger.CLIENTWINDOW.getLogger();

    private static final String CLIENT_WINDOW_CONTEXTS = "com.sun.faces.cdi.clientwindow.clientWindowContexts";

    private final boolean distributable;

    public ClientWindowScopeContextManager() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        distributable = WebConfiguration.getInstance(facesContext.getExternalContext())
                                        .isOptionEnabled(EnableDistributable);
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

            getContextMap(facesContext).put(passivationCapableId, new ClientWindowScopeContextObject(passivationCapableId, contextualInstance));
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
        Map<String, ClientWindowScopeContextObject> contextMap = getContextMap(facesContext);

        if (contextMap != null) {
            if (!(contextual instanceof PassivationCapable)) {
                throw new IllegalArgumentException("ClientWindowScoped bean " + contextual.toString() + " must be PassivationCapable, but is not.");
            }

            ClientWindowScopeContextObject<T> contextObject = contextMap.get(((PassivationCapable) contextual).getId());

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
    private Map<String, ClientWindowScopeContextObject> getContextMap(FacesContext facesContext) {
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
    private Map<String, ClientWindowScopeContextObject> getContextMap(FacesContext facesContext, boolean create) {
        Map<String, ClientWindowScopeContextObject> result = null;

        ExternalContext externalContext = facesContext.getExternalContext();
        if (externalContext != null) {
            Map<String, Object> sessionMap = externalContext.getSessionMap();
            Object session = externalContext.getSession(create);

            if (session != null) {
                Map<Object, Map<String, ClientWindowScopeContextObject>> clientWindowScopeContexts = (Map<Object, Map<String, ClientWindowScopeContextObject>>)
                    sessionMap.get(CLIENT_WINDOW_CONTEXTS);
                String clientWindowId = getCurrentClientWindowId(facesContext);

                if (clientWindowScopeContexts == null && create) {
                    synchronized (session) {
                        Integer size = (Integer) sessionMap.get(ClientWindow.NUMBER_OF_CLIENT_WINDOWS_PARAM_NAME);
                        if (size == null) {
                            size = 10;
                        }
                        sessionMap.put(CLIENT_WINDOW_CONTEXTS, Collections.synchronizedMap(new LRUMap<String, Object>(size)));
                    }
                }

                if (clientWindowScopeContexts != null && clientWindowId != null && create) {
                    synchronized (clientWindowScopeContexts) {
                        if (!clientWindowScopeContexts.containsKey(clientWindowId)) {
                            clientWindowScopeContexts.put(clientWindowId,
                                    new ConcurrentHashMap<String, ClientWindowScopeContextObject>());
                            if (distributable) {
                                // If we are distributable, this will result in a dirtying of the
                                // session data, forcing replication. If we are not distributable,
                                // this is a no-op.
                                sessionMap.put(CLIENT_WINDOW_CONTEXTS, clientWindowScopeContexts);
                            }
                        }
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

        Map<Object, Map<String, ClientWindowScopeContextObject>> clientWindowScopeContexts = (Map<Object, Map<String, ClientWindowScopeContextObject>>)
                session.getAttribute(CLIENT_WINDOW_CONTEXTS);
        if (clientWindowScopeContexts != null) {
            clientWindowScopeContexts.clear();
            session.removeAttribute(CLIENT_WINDOW_CONTEXTS);
        }
    }

    protected String getCurrentClientWindowId(FacesContext facesContext)
    {
        return facesContext.getExternalContext().getClientWindow().getId();
    }
}
