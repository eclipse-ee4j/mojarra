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

package javax.faces.webapp;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Iterator;

import javax.faces.context.ExceptionHandlerFactory;
import javax.faces.context.ExceptionHandler;
import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.faces.event.PhaseId;
import javax.el.ELException;
import javax.faces.application.FacesMessage;
import javax.faces.component.UpdateModelException;
import javax.faces.context.FacesContext;


/**
 * <p class="changed_added_2_0">This {@link ExceptionHandlerFactory} instance 
 * produces Jakarta Server Faces 1.2 compatible
 * {@link ExceptionHandler} instances.  The {@link ExceptionHandler#handle} 
 * method of the <code>ExceptionHandler</code> produced by this factory must 
 * meet the following requirements</p>
 * <div class="changed_added_2_0">
 * 
 * <ul>
 * 
 * <li><p>Any
 * exceptions thrown before or after phase execution will be logged and 
 * swallowed.</p></li>
 * 
 * <li><p>The implementation must examine
 * the <code>Exception</code> within each of the unhandled exception
 * events.  If the <code>Exception</code> is an instance of
 * {@link UpdateModelException}, extract the {@link FacesMessage} from
 * the <code>UpdateModelException</code>.  Log a <code>SEVERE</code>
 * message to the log and queue the <code>FacesMessage</code> 
 * on the {@link FacesContext}, using the <code>clientId</code> of
 * the source component in a call to 
 * {@link FacesContext#addMessage(java.lang.String, javax.faces.application.FacesMessage)}</p></li>
 * 
 * </ul>
 * 
 * </div>
 *
 * @since 2.0
 */
public class PreJsf2ExceptionHandlerFactory extends ExceptionHandlerFactory {

    public PreJsf2ExceptionHandlerFactory() {
    }


    // ------------------------------------ Methods from ExceptionHandlerFactory


    /**
     * @return a new {@link ExceptionHandler} that behaves in a fashion compatible
     *  with specifications prior to Jakarta Server Faces 1.2
     */
    @Override
    public ExceptionHandler getExceptionHandler() {

        return new PreJsf2ExceptionHandler();

    }


    // ---------------------------------------------------------- Nested Classes


    /**
     * Jakarta Server Faces 1.2-style <code>ExceptionHandler</code> implementation.
     */
    private static final class PreJsf2ExceptionHandler extends ExceptionHandler {


        private static final Logger LOGGER =
              Logger.getLogger("javax.faces.webapp", "javax.faces.LogStrings");

        private static final String LOG_BEFORE_KEY =
              "servere.webapp.prejsf2.exception.handler.log_before";
        private static final String LOG_AFTER_KEY =
              "servere.webapp.prejsf2.exception.handler.log_after";
        private static final String LOG_KEY =
              "servere.webapp.prejsf2.exception.handler.log";


        private LinkedList<ExceptionQueuedEvent> unhandledExceptions;
        private LinkedList<ExceptionQueuedEvent> handledExceptions;
        private ExceptionQueuedEvent handled;


        // ------------------------------------------- Methods from ExceptionHandler


        /**
         * @see ExceptionHandler@getHandledExceptionQueuedEvent()
         */
        @Override
        public ExceptionQueuedEvent getHandledExceptionQueuedEvent() {

            return handled;

        }


        /**
         * 
         * 
         * @since 2.0
         */
        @Override
        public void handle() throws FacesException {

            for (Iterator<ExceptionQueuedEvent> i = getUnhandledExceptionQueuedEvents().iterator(); i.hasNext();) {
                ExceptionQueuedEvent event = i.next();
                ExceptionQueuedEventContext context =
                      (ExceptionQueuedEventContext) event.getSource();
                try {
                    Throwable t = context.getException();
                    if (isRethrown(t, (context.inBeforePhase() || context.inAfterPhase()))) {
                        handled = event;
                        Throwable unwrapped = getRootCause(t);
                        if (unwrapped != null) {
                            throw new FacesException(unwrapped.getMessage(), unwrapped);
                        } else {
                            if (t instanceof FacesException) {
                                throw (FacesException) t;
                            } else {
                                throw new FacesException(t.getMessage(), t);
                            }
                        }
                    } else {
                        log(context);
                    }

                } finally {
                    if (handledExceptions == null) {
                        handledExceptions =
                              new LinkedList<>();
                    }
                    handledExceptions.add(event);
                    i.remove();
                }
            }

        }


        /**
         * @see javax.faces.context.ExceptionHandler#isListenerForSource(Object)
         */
        @Override
        public boolean isListenerForSource(Object source) {

            return (source instanceof ExceptionQueuedEventContext);

        }


        /**
         * @see javax.faces.context.ExceptionHandler#processEvent(javax.faces.event.SystemEvent)
         */
        @Override
        public void processEvent(SystemEvent event)
              throws AbortProcessingException {

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
         * @see javax.faces.context.ExceptionHandler#getUnhandledExceptionQueuedEvents()
         */
        @Override
        public Iterable<ExceptionQueuedEvent> getUnhandledExceptionQueuedEvents() {

            return ((unhandledExceptions != null)
                    ? unhandledExceptions
                    : Collections.<ExceptionQueuedEvent>emptyList());

        }


        /**
         * @return
         *
         * @see javax.faces.context.ExceptionHandler#getHandledExceptionQueuedEvents()
         */
        @Override
        public Iterable<ExceptionQueuedEvent> getHandledExceptionQueuedEvents() {

            return ((handledExceptions != null)
                    ? handledExceptions
                    : Collections.<ExceptionQueuedEvent>emptyList());

        }


        // --------------------------------------------------------- Private Methods


        /**
         * @param c <code>Throwable</code> implementation class
         *
         * @return <code>true</code> if <code>c</code> is FacesException.class or
         *         ELException.class
         */
        private boolean shouldUnwrap(Class<? extends Throwable> c) {

            return (FacesException.class.equals(c) || ELException.class.equals(c));

        }


        private boolean isRethrown(Throwable t, boolean isBeforeOrAfterPhase) {

            return (!isBeforeOrAfterPhase &&
                    !(t instanceof AbortProcessingException) &&
                    !(t instanceof UpdateModelException));

        }

        
        private void log(ExceptionQueuedEventContext exceptionContext) {

            Throwable t = exceptionContext.getException();
            UIComponent c = exceptionContext.getComponent();
            if (t instanceof UpdateModelException) {
                FacesContext context = FacesContext.getCurrentInstance();
                FacesMessage message = ((UpdateModelException)t).getFacesMessage();
                LOGGER.log(Level.SEVERE, message.getSummary(), t.getCause());
                context.addMessage(c.getClientId(context), message);
            } else {
                boolean beforePhase = exceptionContext.inBeforePhase();
                boolean afterPhase = exceptionContext.inAfterPhase();
                PhaseId phaseId = exceptionContext.getPhaseId();
                String key = getLoggingKey(beforePhase, afterPhase);
                if (LOGGER.isLoggable(Level.SEVERE)) {
                    LOGGER.log(Level.SEVERE,
                            key,
                            new Object[]{t.getClass().getName(),
                                        phaseId.toString(),
                                        ((c != null)
                                         ? c.getClientId(exceptionContext.getContext())
                                         : ""),
                                        t.getMessage()});
                    LOGGER.log(Level.SEVERE, t.getMessage(), t);
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

    } // END PreJsf2ExceptionHandler
    
}
