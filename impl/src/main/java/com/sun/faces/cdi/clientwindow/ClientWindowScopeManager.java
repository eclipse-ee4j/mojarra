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

package com.sun.faces.cdi.clientwindow;

import static java.util.logging.Level.FINEST;

import java.util.logging.Logger;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * The manager that deals with non-CDI and CDI ClientWindowScoped beans.
 */
public class ClientWindowScopeManager implements HttpSessionListener {

    /**
     * Stores the logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ClientWindowScopeManager.class.getName());
    /**
     * Stores the constant to keep track of the ClientWindowScopeManager.
     */
    public static final String CLIENT_WINDOW_SCOPE_MANAGER = "com.sun.faces.cdi.clientwindow.clientWindowScopeManager";
    /**
     * Stores the CDI context manager.
     */
    private ClientWindowScopeContextManager contextManager;

    /**
     * Constructor.
     */
    public ClientWindowScopeManager() {
        contextManager = new ClientWindowScopeContextManager();
    }

    /**
     * Get the CDI context manager.
     *
     * @return the CDI context manager.
     */
    ClientWindowScopeContextManager getContextManager() {
        return contextManager;
    }

    /**
     * Get our instance.
     *
     * @param facesContext the FacesContext.
     * @return our instance
     */
    public static ClientWindowScopeManager getInstance(FacesContext facesContext) {
        if (!facesContext.getExternalContext().getApplicationMap().containsKey(CLIENT_WINDOW_SCOPE_MANAGER)) {
            facesContext.getExternalContext().getApplicationMap().put(CLIENT_WINDOW_SCOPE_MANAGER, new ClientWindowScopeManager());
        }
        return (ClientWindowScopeManager) facesContext.getExternalContext().getApplicationMap().get(CLIENT_WINDOW_SCOPE_MANAGER);
    }

    /**
     * Create the associated data in the session (if any).
     *
     * @param se the HTTP session event.
     */
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        LOGGER.log(FINEST, "Creating session for @ClientWindowScoped beans");
    }

    /**
     * Destroy the associated data in the session.
     *
     * @param httpSessionEvent the HTTP session event.
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        LOGGER.log(FINEST, "Cleaning up session for @ClientWindowScoped beans");

        if (contextManager != null) {
            contextManager.sessionDestroyed(httpSessionEvent);
        }
    }
}
