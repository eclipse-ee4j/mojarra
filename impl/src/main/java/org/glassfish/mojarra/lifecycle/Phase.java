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

package org.glassfish.mojarra.lifecycle;

import static org.glassfish.mojarra.util.Util.getCdiBeanManager;

import java.lang.annotation.Annotation;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import jakarta.faces.event.AfterPhase;
import jakarta.faces.event.BeforePhase;
import jakarta.faces.event.ExceptionQueuedEvent;
import jakarta.faces.event.ExceptionQueuedEventContext;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.lifecycle.Lifecycle;

import org.glassfish.mojarra.cdi.CdiExtension;
import org.glassfish.mojarra.util.FacesLogger;
import org.glassfish.mojarra.util.Timer;

/**
 * <p>
 * A <strong>Phase</strong> is a single step in the processing of a Jakarta Faces request throughout its entire
 * {@link jakarta.faces.lifecycle.Lifecycle}. Each <code>Phase</code> performs the required transitions on the state
 * information in the {@link FacesContext} associated with this request.
 */

public abstract class Phase {

    private static final Logger LOGGER = FacesLogger.LIFECYCLE.getLogger();

    // ---------------------------------------------------------- Public Methods

    /**
     * Performs PhaseListener processing and invokes the execute method of the Phase.
     *
     * @param context the FacesContext for the current request
     * @param lifecycle the lifecycle for this request
     */
    public void doPhase(FacesContext context, Lifecycle lifecycle, ListIterator<PhaseListener> listeners) {

        context.setCurrentPhaseId(getId());
        PhaseEvent event = new PhaseEvent(context, getId(), lifecycle);

        // start timing - include before and after phase processing
        Timer timer = Timer.getInstance();
        if (timer != null) {
            timer.startTiming();
        }

        try {
            handleBeforePhase(context, listeners, event);
            if (!shouldSkip(context)) {
                execute(context);
            }
        } catch (Throwable e) {
            queueException(context, e);
        } finally {
            try {
                handleAfterPhase(context, listeners, event);
            } catch (Throwable e) {
                queueException(context, e);
            }
            // stop timing
            if (timer != null) {
                timer.stopTiming();
                timer.logResult("Execution time for phase (including any PhaseListeners) -> " + getId().toString());
            }

            context.getExceptionHandler().handle();
        }

    }

    /**
     * <p>
     * Perform all state transitions required by the current phase of the request processing
     * {@link jakarta.faces.lifecycle.Lifecycle} for a particular request.
     * </p>
     *
     * @param context FacesContext for the current request being processed
     * @throws FacesException if a processing error occurred while executing this phase
     */
    public abstract void execute(FacesContext context) throws FacesException;

    /**
     * @return the current {@link jakarta.faces.lifecycle.Lifecycle} <strong>Phase</strong> identifier.
     */
    public abstract PhaseId getId();

    // ------------------------------------------------------- Protected Methods

    protected void queueException(FacesContext ctx, Throwable t) {

        queueException(ctx, t, null);

    }

    protected void queueException(FacesContext ctx, Throwable t, String booleanKey) {

        ExceptionQueuedEventContext extx = new ExceptionQueuedEventContext(ctx, t);
        if (booleanKey != null) {
            extx.getAttributes().put(booleanKey, Boolean.TRUE);
        }
        ctx.getApplication().publishEvent(ctx, ExceptionQueuedEvent.class, extx);

    }

    /**
     * Handle <code>afterPhase</code> <code>PhaseListener</code> events.
     *
     * @param context the FacesContext for the current request
     * @param listenersIterator a ListIterator for the PhaseListeners that need to be invoked
     * @param event the event to pass to each of the invoked listeners
     */
    protected void handleAfterPhase(FacesContext context, ListIterator<PhaseListener> listenersIterator, PhaseEvent event) {

        try {
            Flash flash = context.getExternalContext().getFlash();
            flash.doPostPhaseActions(context);
        } catch (UnsupportedOperationException uoe) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("ExternalContext.getFlash() throw UnsupportedOperationException -> Flash unavailable");
            }
        }

        while (listenersIterator.hasPrevious()) {
            PhaseListener listener = listenersIterator.previous();
            if (getId().equals(listener.getPhaseId()) || PhaseId.ANY_PHASE.equals(listener.getPhaseId())) {
                try {
                    listener.afterPhase(event);
                } catch (Exception e) {
                    queueException(context, e, ExceptionQueuedEventContext.IN_AFTER_PHASE_KEY);
                    break;
                }
            }
        }

        if (hasCdiPhaseObserver(context)) {
            fireCdiPhaseEvent(context, event, AfterPhase.Literal.of(event.getPhaseId()));

            if (event.getPhaseId() != PhaseId.ANY_PHASE) {
                fireCdiPhaseEvent(context, event, AfterPhase.Literal.INSTANCE);
            }
        }
    }

    /**
     * Handle <code>beforePhase</code> <code>PhaseListener</code> events.
     *
     * @param context the FacesContext for the current request
     * @param listenersIterator a ListIterator for the PhaseListeners that need to be invoked
     * @param event the event to pass to each of the invoked listeners
     */
    protected void handleBeforePhase(FacesContext context, ListIterator<PhaseListener> listenersIterator, PhaseEvent event) {

        try {
            Flash flash = context.getExternalContext().getFlash();
            flash.doPrePhaseActions(context);
        } catch (UnsupportedOperationException uoe) {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("ExternalContext.getFlash() throw UnsupportedOperationException -> Flash unavailable");
            }
        }

        if (hasCdiPhaseObserver(context)) {
            if (event.getPhaseId() != PhaseId.ANY_PHASE) {
                fireCdiPhaseEvent(context, event, BeforePhase.Literal.INSTANCE);
            }

            fireCdiPhaseEvent(context, event, BeforePhase.Literal.of(event.getPhaseId()));
        }

        while (listenersIterator.hasNext()) {
            PhaseListener listener = listenersIterator.next();
            if (getId().equals(listener.getPhaseId()) || PhaseId.ANY_PHASE.equals(listener.getPhaseId())) {
                try {
                    listener.beforePhase(event);
                } catch (Exception e) {
                    queueException(context, e, ExceptionQueuedEventContext.IN_BEFORE_PHASE_KEY);
                    // move the iterator pointer back one
                    if (listenersIterator.hasPrevious()) {
                        listenersIterator.previous();
                    }
                    return;
                }
            }
        }

    }
    
    private static void fireCdiPhaseEvent(FacesContext context, PhaseEvent event, Annotation qualifier) {
        getCdiBeanManager(context).getEvent().select(PhaseEvent.class, qualifier).fire(event);
    }

    /**
     * @return whether the application defines any CDI observer of a {@link PhaseEvent}, so the before/after
     * phase dispatch can be skipped entirely in the common case where it does not. Decided once at CDI bootstrap
     * (see {@link CdiExtension#isPhaseEventObserved()}); returns {@code false} when the extension is no longer
     * registered (deployment shutting down, WELD-001325), where there are no observers to dispatch to.
     */
    private static boolean hasCdiPhaseObserver(FacesContext context) {
        try {
            return getCdiBeanManager(context).getExtension(CdiExtension.class).isPhaseEventObserved();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // --------------------------------------------------------- Private Methods

    /**
     * @param context the FacesContext for the current request
     * @return <code>true</code> if <code>FacesContext.responseComplete()</code> or
     * <code>FacesContext.renderResponse()</code> and the phase is not RENDER_RESPONSE, otherwise return <code>false</code>
     */
    private boolean shouldSkip(FacesContext context) {

        if (context.getResponseComplete()) {
            return true;
        } else if (context.getRenderResponse() && !PhaseId.RENDER_RESPONSE.equals(getId())) {
            return true;
        } else {
            return false;
        }

    }

}
