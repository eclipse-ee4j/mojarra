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

package com.sun.faces.context;

import java.util.List;
import java.util.ArrayList;

import javax.faces.context.ExceptionHandler;
import javax.faces.context.ExceptionHandlerFactory;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.webapp.PreJsf2ExceptionHandlerFactory;
import javax.faces.FacesException;
import javax.el.ELException;

import com.sun.faces.cactus.ServletFacesTestCase;

/**
 * <p>
 * Test case for both {@link ExceptionHandlerImpl} and the <code>ExceptionHandler</code>
 * created by {@link jakarta.faces.webapp.PreJsf2ExceptionHandlerFactory}.
 * </p>
 *
 * <p>
 * Testing for the API implementation occurs here to leverage common code.
 * </p>
 *
 */
public class TestExceptionHandler extends ServletFacesTestCase {

    private ExceptionHandlerFactory implFactory = new ExceptionHandlerFactoryImpl();
    private ExceptionHandlerFactory apiFactory = new PreJsf2ExceptionHandlerFactory();


    // ------------------------------------------------------------ Constructors


    public TestExceptionHandler() {
        super("TestExceptionHandler");
    }


    public TestExceptionHandler(String name) {
        super(name);
    }


    // ------------------------------------------------------------ Test Methods


    public void testIsListenerForSource() {
        testIsListenerForSource(implFactory.getExceptionHandler());
        testIsListenerForSource(apiFactory.getExceptionHandler());
    }


    public void testProcessEvent() {
        testProcessEvent(implFactory.getExceptionHandler());
        testProcessEvent(apiFactory.getExceptionHandler());
    }

    public void testHandle() {
        testHandleNoEventsQueued(implFactory.getExceptionHandler());
        testHandleNoEventsQueued(apiFactory.getExceptionHandler());
        testHandleAbortProcessingExceptionQueued(implFactory.getExceptionHandler());
        testHandleAbortProcessingExceptionQueued(apiFactory.getExceptionHandler());
        testHandleExceptionThrow(implFactory.getExceptionHandler());
        testHandleExceptionThrow(apiFactory.getExceptionHandler());
        testHandleBeforeAfterExceptions(implFactory.getExceptionHandler(), true);
        testHandleBeforeAfterExceptions(apiFactory.getExceptionHandler(), false);
        //test for issue 1263
        boolean isProcessingEvents = getFacesContext().isProcessingEvents(); 
        try {
            getFacesContext().setProcessingEvents(false);
            testHandleExceptionThrow(implFactory.getExceptionHandler());
            testHandleExceptionThrow(apiFactory.getExceptionHandler());
        } finally {
        	getFacesContext().setProcessingEvents(isProcessingEvents);
        }
    }

    public void testGetRootCause() {
        testGetRootCauseNull(implFactory.getExceptionHandler());
        testGetRootCauseNull(apiFactory.getExceptionHandler());
    }

    // --------------------------------------------------------- Private Methods


    private void testIsListenerForSource(ExceptionHandler handler) {

        assertFalse(handler.isListenerForSource(null));
        ExceptionQueuedEventContext ectx =
              new ExceptionQueuedEventContext(getFacesContext(), new RuntimeException());
        assertFalse(handler.isListenerForSource(new ExceptionQueuedEvent(ectx)));
        assertTrue(handler.isListenerForSource(ectx));

    }


    private void testProcessEvent(ExceptionHandler handler) {

        // if event is null, no action is taken, which means an empty Iterator
        // for the getUnhandledExceptionQueuedEvents
        handler.processEvent(null);
        List<ExceptionQueuedEvent> events = copyToList(handler.getUnhandledExceptionQueuedEvents());
        assertTrue(events.isEmpty());

        // queue an exception event...
        ExceptionQueuedEventContext ectx =
              new ExceptionQueuedEventContext(getFacesContext(), new RuntimeException());
        handler.processEvent(new ExceptionQueuedEvent(ectx));
        events = copyToList(handler.getUnhandledExceptionQueuedEvents());
        assertTrue(events.size() == 1);
        assertTrue(events.get(0).getSource() == ectx);
        ExceptionQueuedEventContext ectx2 =
              new ExceptionQueuedEventContext(getFacesContext(), new RuntimeException());

        // queue an additionl event to ensure order is maintained
        handler.processEvent(new ExceptionQueuedEvent(ectx2));
        events = copyToList(handler.getUnhandledExceptionQueuedEvents());
        assertTrue(events.size() == 2);
        assertTrue(events.get(0).getSource() == ectx);
        assertTrue(events.get(1).getSource() == ectx2);

    }


    private void testHandleNoEventsQueued(ExceptionHandler handler) {

        // no events have been queued
        try {
            handler.handle();
        } catch (Throwable t) {
            assertTrue("Unexpected exception thrown with no ExceptionQueuedEvents queued", false);
        }

    }

    private void testHandleAbortProcessingExceptionQueued(ExceptionHandler handler) {

        getFacesContext().setExceptionHandler(handler);
        ExceptionQueuedEventContext ctx =
              new ExceptionQueuedEventContext(getFacesContext(), new AbortProcessingException());

        // queue the abort processing exception.  When calling handle(), no
        // exception should be thrown, but the ExceptionQueuedEvent should be returned
        // by getHandledExceptionQueuedEvents() while getUnhandledExceptionQueuedEvents()
        // should be null, and getHandledExceptionQueuedEvent() should return null
        // as nothing was thrown by the handle() method.
        // Side note, validate the exception is properly queued by publishing
        // and event.
        queueException(ctx);
        List<ExceptionQueuedEvent> unhandled = copyToList(handler.getUnhandledExceptionQueuedEvents());
        List<ExceptionQueuedEvent> handled = copyToList(handler.getHandledExceptionQueuedEvents());
        assertTrue(unhandled.size() == 1);
        assertTrue(handled.isEmpty());

        try {
            handler.handle();
        } catch (Throwable t) {
            assertTrue("Exception thrown by handle() when only an AbortProcessingException was queued.  These should be ignored.", false);
        }

        unhandled = copyToList(handler.getUnhandledExceptionQueuedEvents());
        handled = copyToList(handler.getHandledExceptionQueuedEvents());
        assertNull(handler.getHandledExceptionQueuedEvent());
        assertTrue(unhandled.isEmpty());
        assertTrue(handled.size() == 1);

        // queue another and call handled() again
        queueException(ctx);
        unhandled = copyToList(handler.getUnhandledExceptionQueuedEvents());
        handled = copyToList(handler.getHandledExceptionQueuedEvents());
        assertTrue(unhandled.size() == 1);
        assertTrue(handled.size() == 1);

        try {
            handler.handle();
        } catch (Throwable t) {
            assertTrue("Exception thrown by handle() when only an AbortProcessingException was queued.  These should be ignored.", false);
        }

        unhandled = copyToList(handler.getUnhandledExceptionQueuedEvents());
        handled = copyToList(handler.getHandledExceptionQueuedEvents());
        assertTrue(unhandled.size() == 0);
        assertTrue(handled.size() == 2);


    }

    public void testHandleExceptionThrow(ExceptionHandler handler) {

        getFacesContext().setExceptionHandler(handler);
        ExceptionQueuedEventContext abortProcessing =
              new ExceptionQueuedEventContext(getFacesContext(), new AbortProcessingException());
        ExceptionQueuedEventContext abortProcessing2 =
              new ExceptionQueuedEventContext(getFacesContext(), new AbortProcessingException());

        // wrap this up in a chain of exceptions to unsure that when
        // getRootCause() we get what we expect
        Exception e = new FacesException(
                          new ELException(
                              new FacesException(
                                  new IllegalStateException(
                                      new FacesException(
                                          new IllegalArgumentException())))));
        ExceptionQueuedEventContext runtimeException =
              new ExceptionQueuedEventContext(getFacesContext(), e);
        ExceptionQueuedEventContext runtimeException2 =
              new ExceptionQueuedEventContext(getFacesContext(), e);
        ExceptionQueuedEventContext abortProcessing3 =
              new ExceptionQueuedEventContext(getFacesContext(), new AbortProcessingException());

        queueException(abortProcessing);
        queueException(abortProcessing2);
        queueException(runtimeException);
        queueException(runtimeException2);
        queueException(abortProcessing3);

        List<ExceptionQueuedEvent> unhandled = copyToList(handler.getUnhandledExceptionQueuedEvents());
        List<ExceptionQueuedEvent> handled = copyToList(handler.getHandledExceptionQueuedEvents());
        assertTrue(unhandled.size() == 5);
        assertTrue(handled.isEmpty());

        try {
            handler.handle();
            assertTrue("No exception thrown by the handle() method, but there were RuntimeExceptions present that should have been thrown.", false);
        } catch (Throwable t) {
            assertTrue(t instanceof FacesException);
            Throwable root = t.getCause();
            assertTrue(root instanceof IllegalStateException);
            root = root.getCause();
            assertTrue(root instanceof FacesException);
            root = root.getCause();
            assertTrue(root instanceof IllegalArgumentException);
            root = root.getCause();
            assertNull(root);
        }

        assertTrue(handler.getHandledExceptionQueuedEvent().getSource() == runtimeException);
        unhandled = copyToList(handler.getUnhandledExceptionQueuedEvents());
        handled = copyToList(handler.getHandledExceptionQueuedEvents());

        assertTrue(handled.size() == 3);
        assertTrue(unhandled.size() == 2);
        assertTrue(handled.get(0).getSource() == abortProcessing);
        assertTrue(handled.get(1).getSource() == abortProcessing2);
        assertTrue(handled.get(2).getSource() == runtimeException);
        assertTrue(unhandled.get(0).getSource() == runtimeException2);
        assertTrue(unhandled.get(1).getSource() == abortProcessing3);

        // call handle() again and make sure the results are sane
        try {
            handler.handle();
            assertTrue("No exception thrown by the handle() method, but there were RuntimeExceptions present that should have been thrown.", false);
        } catch (Throwable t) {
            // expected
        }

        assertTrue(handler.getHandledExceptionQueuedEvent().getSource() == runtimeException2);
        unhandled = copyToList(handler.getUnhandledExceptionQueuedEvents());
        handled = copyToList(handler.getHandledExceptionQueuedEvents());

        assertTrue(handled.size() == 4);
        assertTrue(unhandled.size() == 1);
        assertTrue(handled.get(0).getSource() == abortProcessing);
        assertTrue(handled.get(1).getSource() == abortProcessing2);
        assertTrue(handled.get(2).getSource() == runtimeException);
        assertTrue(handled.get(3).getSource() == runtimeException2);
        assertTrue(unhandled.get(0).getSource() == abortProcessing3);

        // call handle() again and make sure the results are sane - no exception thrown
        // this time
        try {
            handler.handle();
        } catch (Throwable t) {
            assertTrue("Exception thrown by handle() when only an AbortProcessingException was queued.  These should be ignored.", false);
        }

        assertTrue(handler.getHandledExceptionQueuedEvent().getSource() == runtimeException2);
        unhandled = copyToList(handler.getUnhandledExceptionQueuedEvents());
        handled = copyToList(handler.getHandledExceptionQueuedEvents());

        assertTrue(handled.size() == 5);
        assertTrue(unhandled.isEmpty());
        assertTrue(handled.get(0).getSource() == abortProcessing);
        assertTrue(handled.get(1).getSource() == abortProcessing2);
        assertTrue(handled.get(2).getSource() == runtimeException);
        assertTrue(handled.get(3).getSource() == runtimeException2);
        assertTrue(handled.get(4).getSource() == abortProcessing3);

    }


    private void testHandleBeforeAfterExceptions(ExceptionHandler handler, boolean shouldThrow) {

        // In 2.0, exceptions thrown by before or after phases of a single
        // phase will be rethrown.  In 1.2, they were logged and swallowed.
        // Make sure this is the case.
        getFacesContext().setExceptionHandler(handler);

        ExceptionQueuedEventContext ctx = new ExceptionQueuedEventContext(getFacesContext(), new RuntimeException());
        ctx.getAttributes().put(ExceptionQueuedEventContext.IN_BEFORE_PHASE_KEY, Boolean.TRUE);
        queueException(ctx);

        try {
            handler.handle();
            if (shouldThrow) {
                assertTrue("[BEFORE] Exception expected to be thrown", false);
            }
        } catch (Throwable t) {
            if (!shouldThrow) {
                assertTrue("[BEFORE] Exception should not have been thrown", false);
            }
        }

        ctx.getAttributes().remove(ExceptionQueuedEventContext.IN_BEFORE_PHASE_KEY);
        ctx.getAttributes().put(ExceptionQueuedEventContext.IN_AFTER_PHASE_KEY, Boolean.TRUE);

        try {
            handler.handle();
            if (shouldThrow) {
                assertTrue("[AFTER] Exception expected to be thrown", false);
            }
        } catch (Throwable t) {
            if (!shouldThrow) {
                assertTrue("[AFTER] Exception should not have been thrown", false);
            }
        }

    }

    private void testGetRootCauseNull(ExceptionHandler handler) {

        assertNull(handler.getRootCause(null));

    }


    private List<ExceptionQueuedEvent> copyToList(Iterable<ExceptionQueuedEvent> events) {

        List<ExceptionQueuedEvent> list = new ArrayList<ExceptionQueuedEvent>();
        for (ExceptionQueuedEvent event : events) {
            list.add(event);
        }
        return list;

    }


    private void queueException(ExceptionQueuedEventContext source) {

        getFacesContext().getApplication().publishEvent(getFacesContext(),
                                                        ExceptionQueuedEvent.class,
                                                        source);

    }
}
