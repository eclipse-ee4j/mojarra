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

package com.sun.faces.application;

import static com.sun.faces.application.view.ViewScopeManager.ACTIVE_VIEW_MAPS;
import static com.sun.faces.application.view.ViewScopeManager.VIEW_SCOPE_MANAGER;
import static com.sun.faces.cdi.clientwindow.ClientWindowScopeManager.CLIENT_WINDOW_SCOPE_MANAGER;
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableDistributable;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.faces.config.InitFacesContext;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.flow.FlowCDIContext;
import com.sun.faces.renderkit.StateHelper;

import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * <p>
 * Central location for web application lifecycle events.
 * </p>
 * <p>
 * The main purpose of this class is detect when we should be invoking methods marked with the <code>@PreDestroy</code>
 * annotation.
 * </p>
 */
public class WebappLifecycleListener {

    private ServletContext servletContext;
    private ApplicationAssociate applicationAssociate;
    private final Set<HttpSession> activeSessions = ConcurrentHashMap.newKeySet();


    // ------------------------------------------------------------ Constructors

    public WebappLifecycleListener() {
    }

    public WebappLifecycleListener(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    // ---------------------------------------------------------- Public Methods


    /**
     * The request is about to come into scope of the web application.
     *
     * @param event the notification event
     */
    public void requestInitialized(ServletRequestEvent event) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext instanceof InitFacesContext) {
            // NOTE: this should never happen. We should probably log a WARN line for diagnostic?
            InitFacesContext initFacesContext = (InitFacesContext) facesContext;
            initFacesContext.release();
        }
        ApplicationAssociate.setCurrentInstance(getAssociate());
    }


    /**
     * The request is about to go out of scope of the web application.
     *
     * @param event the notification event
     */
    public void requestDestroyed(ServletRequestEvent event) {
        try {
            // If we are distributable and we have an active view map force the ACTIVE_VIEW_MAPS session entry to be replicated.
            if (isDistributable(event)) {
                HttpSession session = ((HttpServletRequest) event.getServletRequest()).getSession(false);
                if (session != null && session.getAttribute(ACTIVE_VIEW_MAPS) != null) {
                    session.setAttribute(ACTIVE_VIEW_MAPS, session.getAttribute(ACTIVE_VIEW_MAPS));
                }
            }
        } catch (Throwable t) {
            FacesContext context = new InitFacesContext(event.getServletContext());
            context.getApplication()
                   .publishEvent(context, ExceptionQueuedEvent.class, new ExceptionQueuedEventContext(context, t));
            context.getExceptionHandler().handle();
        } finally {
            ApplicationAssociate.setCurrentInstance(null);
        }
    }

    /**
     * Notification that a session has been created.
     *
     * @param event the notification event
     */
    public void sessionCreated(HttpSessionEvent event) {
        ApplicationAssociate associate = getAssociate();
        if (isDevModeEnabled(associate)) {
            activeSessions.add(event.getSession());
        }

        // Try to avoid creating the token unless we actually have protected views
        if (haveProtectedViews(associate)) {
            StateHelper.createAndStoreCryptographicallyStrongTokenInSession(event.getSession());
        }
    }

    /**
     * Notification that a session is about to be invalidated.
     *
     * @param event the notification event
     */
    public void sessionDestroyed(HttpSessionEvent event) {
        activeSessions.remove(event.getSession());
        FlowCDIContext.sessionDestroyed(event);

        for (HttpSessionListener listener :
                asList((HttpSessionListener)servletContext.getAttribute(VIEW_SCOPE_MANAGER),
                        (HttpSessionListener)servletContext.getAttribute(CLIENT_WINDOW_SCOPE_MANAGER))) {
            if (listener != null) {
                listener.sessionDestroyed(event);
            }
        }
    }

    /**
     * Notification that the web application initialization process is starting. All ServletContextListeners are notified of
     * context initialization before any filter or servlet in the web application is initialized.
     *
     * @param event the notification event
     */
    public void contextInitialized(ServletContextEvent event) {
        if (servletContext == null) {
            servletContext = event.getServletContext();
        }
    }

    /**
     * Notification that the servlet context is about to be shut down. All servlets and filters have been destroy()ed before
     * any ServletContextListeners are notified of context destruction.
     *
     * @param event the nofication event
     */
    public void contextDestroyed(ServletContextEvent event) {
        applicationAssociate = null;
    }

    public List<HttpSession> getActiveSessions() {
        return new ArrayList<>(activeSessions);
    }


    // --------------------------------------------------------- Private Methods

    private ApplicationAssociate getAssociate() {
        if (applicationAssociate == null) {
            applicationAssociate = ApplicationAssociate.getInstance(servletContext);
        }

        return applicationAssociate;
    }

    private boolean isDistributable(ServletRequestEvent event) {
        return WebConfiguration.getInstance(event.getServletContext()).isOptionEnabled(EnableDistributable);
    }

    private boolean isDevModeEnabled(ApplicationAssociate associate) {
        return associate != null && associate.isDevModeEnabled();
    }

    private boolean haveProtectedViews(ApplicationAssociate associate) {
        return !associate.getApplication().getViewHandler().getProtectedViewsUnmodifiable().isEmpty();
    }
}
