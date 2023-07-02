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

package com.sun.faces.context;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.util.FacesLogger;

import jakarta.el.ELException;
import jakarta.faces.FacesException;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.SystemEvent;

/**
 * <p>
 * The default implementation of {@link ExceptionHandler} for Faces.
 * </p>
 *
 */
public class ExceptionHandlerImpl extends ExceptionHandler {

    private static final Logger LOGGER = FacesLogger.CONTEXT.getLogger();
    private static final String LOG_BEFORE_KEY = "faces.context.exception.handler.log_before";
    private static final String LOG_AFTER_KEY = "faces.context.exception.handler.log_after";
    private static final String LOG_KEY = "faces.context.exception.handler.log";

    public static final java.util.logging.Level INCIDENT_ERROR = Level.parse(Integer.toString(Level.SEVERE.intValue() + 100));

    private LinkedList<ExceptionQueuedEvent> unhandledExceptions;
    private LinkedList<ExceptionQueuedEvent> handledExceptions;
    private ExceptionQueuedEvent handled;
    private final boolean errorPagePresent;

    // ------------------------------------------------------------ Constructors

    public ExceptionHandlerImpl() {

        errorPagePresent = true;

    }

    public ExceptionHandlerImpl(boolean errorPagePresent) {

        this.errorPagePresent = errorPagePresent;

    }

    // ------------------------------------------- Methods from ExceptionHandler

    @Override
    public ExceptionQueuedEvent getHandledExceptionQueuedEvent() {

        return handled;

    }

    /**
     * @see jakarta.faces.context.ExceptionHandler#handle()
     */
    @SuppressWarnings({ "ThrowableInstanceNeverThrown" })
    @Override
    public void handle() throws FacesException {

        for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator(); i.hasNext();) {
            ExceptionQueuedEvent event = i.next();
            ExceptionQueuedEventContext context = (ExceptionQueuedEventContext) event.getSource();
            try {
                Throwable t = context.getException();
                if (isRethrown(t)) {
                    handled = event;
                    Throwable unwrapped = getRootCause(t);
                    if (unwrapped != null) {
                        throwIt(context.getContext(), new FacesException(unwrapped.getMessage(), unwrapped));
                    } else {
                        if (t instanceof FacesException) {
                            throwIt(context.getContext(), (FacesException) t);
                        } else {
                            throwIt(context.getContext(), new FacesException(t.getMessage(), t));
                        }
                    }
                    if (LOGGER.isLoggable(INCIDENT_ERROR)) {
                        log(context);
                    }

                } else {
                    log(context);
                }

            } finally {
                if (handledExceptions == null) {
                    handledExceptions = new LinkedList<>();
                }
                handledExceptions.add(event);
                i.remove();
            }
        }

    }

    /**
     * @see jakarta.faces.context.ExceptionHandler#isListenerForSource(Object)
     */
    @Override
    public boolean isListenerForSource(Object source) {

        return source instanceof ExceptionQueuedEventContext;

    }

    /**
     * @see jakarta.faces.context.ExceptionHandler#processEvent(jakarta.faces.event.SystemEvent)
     */
    @Override
    public void processEvent(SystemEvent event) throws AbortProcessingException {

        if (event != null) {
            if (unhandledExceptions == null) {
                unhandledExceptions = new LinkedList<>();
            }
            unhandledExceptions.add((ExceptionQueuedEvent) event);
        }

    }

    /**
     * @see ExceptionHandler#getRootCause(Throwable)
     */
    @Override
    public Throwable getRootCause(Throwable t) {

        if (t == null) {
            return null;
        }
        if (shouldUnwrap(t.getClass())) {
            Throwable root = t.getCause();
            if (root != null) {
                Throwable tmp = getRootCause(root);
                if (tmp == null) {
                    return root;
                } else {
                    return tmp;
                }
            } else {
                return t;
            }
        }
        return t;

    }

    /**
     * @see jakarta.faces.context.ExceptionHandler#getUnhandledExceptionQueuedEvents()
     */
    @Override
    public Iterable<ExceptionQueuedEvent> getUnhandledExceptionQueuedEvents() {

        return unhandledExceptions != null ? unhandledExceptions : Collections.<ExceptionQueuedEvent>emptyList();

    }

    /**
     * @see jakarta.faces.context.ExceptionHandler#getHandledExceptionQueuedEvents()
     */
    @Override
    public Iterable<ExceptionQueuedEvent> getHandledExceptionQueuedEvents() {

        return handledExceptions != null ? handledExceptions : Collections.<ExceptionQueuedEvent>emptyList();

    }

    // --------------------------------------------------------- Private Methods

    private void throwIt(FacesContext ctx, FacesException fe) {

        boolean isDevelopment = ctx.isProjectStage(ProjectStage.Development);
        ExternalContext extContext = ctx.getExternalContext();
        Throwable wrapped = fe.getCause();
        try {
            extContext.responseReset();
        } catch (Exception e) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, "Exception when handling error trying to reset the response.", wrapped);
            }
        }
        if (wrapped instanceof FacesFileNotFoundException) {
            extContext.setResponseStatus(404);
        } else {
            extContext.setResponseStatus(500);
        }

        if (isDevelopment && !errorPagePresent) {
            // RELEASE_PENDING_2_1
            // thThe error page here will be text/html which means not all device
            // types are going to render this properly. This should be addressed
            // in 2.1
            RenderKitUtils.renderHtmlErrorPage(ctx, fe);
        } else {
            if (isDevelopment) {
                // store the view root where the exception occurred into the
                // request scope so that the error page can display that component
                // tree and not the one rendering the error page
                ctx.getExternalContext().getRequestMap().put("com.sun.faces.error.view", ctx.getViewRoot());
            }
            throw fe;
        }
    }

    /**
     * @param c <code>Throwable</code> implementation class
     * @return <code>true</code> if <code>c</code> is FacesException.class or ELException.class
     */
    private boolean shouldUnwrap(Class<? extends Throwable> c) {

        return FacesException.class.equals(c) || ELException.class.equals(c);

    }

    private boolean isRethrown(Throwable t) {

        return !(t instanceof AbortProcessingException);

    }

    private void log(ExceptionQueuedEventContext exceptionContext) {

        UIComponent c = exceptionContext.getComponent();
        boolean beforePhase = exceptionContext.inBeforePhase();
        boolean afterPhase = exceptionContext.inAfterPhase();
        PhaseId phaseId = exceptionContext.getPhaseId();
        Throwable t = exceptionContext.getException();
        String key = getLoggingKey(beforePhase, afterPhase);
        // If both SEVERE and INCIDENT_ERROR are loggable, just use
        // INCIDENT ERROR, otherwise just use SEVERE.
        Level level = LOGGER.isLoggable(INCIDENT_ERROR) && LOGGER.isLoggable(Level.SEVERE) ? INCIDENT_ERROR : Level.SEVERE;

        if (LOGGER.isLoggable(level)) {
            LOGGER.log(level, key, new Object[] { t.getClass().getName(), phaseId.toString(), c != null ? c.getClientId(exceptionContext.getContext()) : "",
                    t.getMessage() });
            if (t.getMessage() != null) {
                LOGGER.log(level, t.getMessage(), t);
            } else {
                LOGGER.log(level, "No associated message", t);
            }
        }

    }

    private String getLoggingKey(boolean beforePhase, boolean afterPhase) {
        if (beforePhase) {
            return LOG_BEFORE_KEY;
        } else if (afterPhase) {
            return LOG_AFTER_KEY;
        } else {
            return LOG_KEY;
        }
    }

}
