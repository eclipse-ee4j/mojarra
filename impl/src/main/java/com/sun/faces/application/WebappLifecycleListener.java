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
import static com.sun.faces.config.WebConfiguration.BooleanWebContextInitParameter.EnableDistributable;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.application.view.ViewScopeManager;
import com.sun.faces.config.InitFacesContext;
import com.sun.faces.config.WebConfiguration;
import com.sun.faces.el.ELUtils;
import com.sun.faces.flow.FlowCDIContext;
import com.sun.faces.io.FastStringWriter;
import com.sun.faces.mgbean.BeanManager;
import com.sun.faces.renderkit.StateHelper;
import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import jakarta.faces.application.Application;
import jakarta.faces.application.ViewHandler;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextAttributeEvent;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestAttributeEvent;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionBindingEvent;
import jakarta.servlet.http.HttpSessionEvent;

/**
 * <p>
 * Central location for web application lifecycle events.
 * <p>
 * <p>
 * The main purpose of this class is detect when we should be invoking methods marked with the <code>@PreDestroy</code>
 * annotation.
 * </p>
 */
public class WebappLifecycleListener {

    // Log instance for this class
    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    private ServletContext servletContext;
    private ApplicationAssociate applicationAssociate;
    private ActiveSessions activeSessions;

    /*
     * An inner class to provide synchronized access to activeSessions
     */
    class ActiveSessions {

        private List<HttpSession> activeSessions;

        public ActiveSessions() {
            activeSessions = new ArrayList<>();
        }

        public synchronized void add(HttpSession hs) {
            if (activeSessions == null) {
                activeSessions = new ArrayList<>();
            }
            activeSessions.add(hs);
        }

        public synchronized void remove(HttpSession hs) {
            if (activeSessions != null) {
                activeSessions.remove(hs);
            }
        }

        public synchronized List<HttpSession> get() {
            return new ArrayList<>(activeSessions);
        }

    }

    // ------------------------------------------------------------ Constructors

    public WebappLifecycleListener() {
        activeSessions = new ActiveSessions();
    }

    public WebappLifecycleListener(ServletContext servletContext) {

        this.servletContext = servletContext;
        activeSessions = new ActiveSessions();

    }

    // ---------------------------------------------------------- Public Methods

    /**
     * The request is about to go out of scope of the web application.
     *
     * @param event the notification event
     */
    public void requestDestroyed(ServletRequestEvent event) {

        try {
            ServletRequest request = event.getServletRequest();
            for (Enumeration e = request.getAttributeNames(); e.hasMoreElements();) {
                String beanName = (String) e.nextElement();
                handleAttributeEvent(beanName, request.getAttribute(beanName), ELUtils.Scope.REQUEST);
            }
            WebConfiguration config = WebConfiguration.getInstance(event.getServletContext());
            if (config.isOptionEnabled(WebConfiguration.BooleanWebContextInitParameter.EnableAgressiveSessionDirtying)) {
                syncSessionScopedBeans(request);
            }

            /*
             * If we are distributable and we have an active view map force the ACTIVE_VIEW_MAPS session entry to be replicated.
             */
            boolean distributable = config.isOptionEnabled(EnableDistributable);

            if (distributable) {
                HttpSession session = ((HttpServletRequest) request).getSession(false);
                if (session != null && session.getAttribute(ACTIVE_VIEW_MAPS) != null) {
                    session.setAttribute(ACTIVE_VIEW_MAPS, session.getAttribute(ACTIVE_VIEW_MAPS));
                }
            }
        } catch (Throwable t) {
            FacesContext context = new InitFacesContext(event.getServletContext());
            ExceptionQueuedEventContext eventContext = new ExceptionQueuedEventContext(context, t);
            context.getApplication().publishEvent(context, ExceptionQueuedEvent.class, eventContext);
            context.getExceptionHandler().handle();
        } finally {
            ApplicationAssociate.setCurrentInstance(null);
        }
    }

    /**
     * The request is about to come into scope of the web application.
     *
     * @param event the notification event
     */
    public void requestInitialized(ServletRequestEvent event) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (facesContext instanceof InitFacesContext) {
            InitFacesContext initFacesContext = (InitFacesContext) facesContext;
            initFacesContext.releaseCurrentInstance();
            // Bug 20458755 Clean up the entry for the InitContext corresponding
            // to this ServletContext
            initFacesContext.removeServletContextEntryForInitContext();
        }
        ApplicationAssociate.setCurrentInstance(getAssociate());
    }

    /**
     * Notfication that a session has been created.
     *
     * @param event the notification event
     */
    public void sessionCreated(HttpSessionEvent event) {
        ApplicationAssociate associate = getAssociate();
        // PENDING this should only create a new list if in dev mode
        if (associate != null && associate.isDevModeEnabled()) {
            activeSessions.add(event.getSession());
        }
        boolean doCreateToken = true;

        // Try to avoid creating the token unless we actually have protected views
        if (null != associate) {
            Application application = associate.getApplication();
            ViewHandler viewHandler = application.getViewHandler();
            doCreateToken = !viewHandler.getProtectedViewsUnmodifiable().isEmpty();
        }

        if (doCreateToken) {
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

        if (Util.isCdiAvailable(servletContext)) {
            FlowCDIContext.sessionDestroyed(event);
        }

        ViewScopeManager manager = (ViewScopeManager) servletContext.getAttribute(ViewScopeManager.VIEW_SCOPE_MANAGER);
        if (manager != null) {
            manager.sessionDestroyed(event);
        }
    }

    /**
     * Notification that an existing attribute has been removed from the servlet request. Called after the attribute is
     * removed.
     *
     * @param event the notification event
     */
    public void attributeRemoved(ServletRequestAttributeEvent event) {
        handleAttributeEvent(event.getName(), event.getValue(), ELUtils.Scope.REQUEST);
    }

    /**
     * Notification that an attribute was replaced on the servlet request. Called after the attribute is replaced.
     *
     * @param event the notification event
     */
    public void attributeReplaced(ServletRequestAttributeEvent event) {
        String attrName = event.getName();
        Object newValue = event.getServletRequest().getAttribute(attrName);

        // perhaps a bit paranoid, but since the javadocs are a bit vague,
        // only handle the event if oldValue and newValue are not the
        // exact same object
        // noinspection ObjectEquality
        if (event.getValue() != newValue) {
            handleAttributeEvent(attrName, event.getValue(), ELUtils.Scope.REQUEST);
        }
    }

    /**
     * Notification that an attribute has been removed from a session. Called after the attribute is removed.
     *
     * @param event the nofication event
     */
    public void attributeRemoved(HttpSessionBindingEvent event) {
        handleAttributeEvent(event.getName(), event.getValue(), ELUtils.Scope.SESSION);
    }

    /**
     * Notification that an attribute has been replaced in a session. Called after the attribute is replaced.
     *
     * @param event the notification event
     */
    public void attributeReplaced(HttpSessionBindingEvent event) {
        HttpSession session = event.getSession();
        String attrName = event.getName();
        Object newValue = session.getAttribute(attrName);

        // perhaps a bit paranoid, but since the javadocs are a bit vague,
        // only handle the event if oldValue and newValue are not the
        // exact same object
        // noinspection ObjectEquality
        if (event.getValue() != newValue) {
            handleAttributeEvent(attrName, event.getValue(), ELUtils.Scope.SESSION);
        }

    }

    /**
     * Notification that an existing attribute has been removed from the servlet context. Called after the attribute is
     * removed.
     *
     * @param event the notification event
     */
    public void attributeRemoved(ServletContextAttributeEvent event) {
        handleAttributeEvent(event.getName(), event.getValue(), ELUtils.Scope.APPLICATION);
    }

    /**
     * Notification that an attribute on the servlet context has been replaced. Called after the attribute is replaced.
     *
     * @param event the notification event
     */
    public void attributeReplaced(ServletContextAttributeEvent event) {
        ServletContext context = event.getServletContext();
        String attrName = event.getName();
        Object newValue = context.getAttribute(attrName);

        // perhaps a bit paranoid, but since the javadocs are a bit vague,
        // only handle the event if oldValue and newValue are not the
        // exact same object
        // noinspection ObjectEquality
        if (event.getValue() != newValue) {
            handleAttributeEvent(attrName, event.getValue(), ELUtils.Scope.APPLICATION);
        }
    }

    private void handleAttributeEvent(String beanName, Object bean, ELUtils.Scope scope) {

        ApplicationAssociate associate = getAssociate();
        try {
            if (associate != null) {
                BeanManager beanManager = associate.getBeanManager();
                if (beanManager != null && beanManager.isManaged(beanName)) {
                    beanManager.destroy(beanName, bean);
                }
            }
        } catch (Exception e) {
            String className = e.getClass().getName();
            String message = e.getMessage();
            if (message == null) {
                message = "";
            }
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, "jsf.config.listener.predestroy.error", new Object[] { className, beanName, scope, message });
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                FastStringWriter writer = new FastStringWriter(128);
                e.printStackTrace(new PrintWriter(writer));
                LOGGER.fine(writer.toString());
            }
        }

    } // END handleAttributeEvent

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

        for (Enumeration e = servletContext.getAttributeNames(); e.hasMoreElements();) {
            String beanName = (String) e.nextElement();
            handleAttributeEvent(beanName, servletContext.getAttribute(beanName), ELUtils.Scope.APPLICATION);
        }
        applicationAssociate = null;

    }

    public List<HttpSession> getActiveSessions() {
        return activeSessions.get();
    }

    // --------------------------------------------------------- Private Methods

    private ApplicationAssociate getAssociate() {
        if (applicationAssociate == null) {
            applicationAssociate = ApplicationAssociate.getInstance(servletContext);
        }

        return applicationAssociate;
    }

    /**
     * This method ensures that session scoped managed beans will be synchronized properly in a clustered environment.
     *
     * @param request the current <code>ServletRequest</code>
     */
    private void syncSessionScopedBeans(ServletRequest request) {

        if (request instanceof HttpServletRequest) {
            HttpSession session = ((HttpServletRequest) request).getSession(false);
            if (session != null) {
                ApplicationAssociate associate = getAssociate();
                if (associate == null) {
                    return;
                }
                BeanManager manager = associate.getBeanManager();
                if (manager != null) {
                    for (Enumeration e = session.getAttributeNames(); e.hasMoreElements();) {
                        String name = (String) e.nextElement();
                        if (manager.isManaged(name)) {
                            session.setAttribute(name, session.getAttribute(name));
                        }
                    }
                }
            }
        }

    }

} // END WebappLifecycleListener
