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

package com.sun.faces.application;

import java.util.List;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.FacesListener;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import javax.faces.event.SystemEventListenerHolder;
import javax.faces.event.PostConstructApplicationEvent;

import com.sun.faces.CustomSystemEvent;
import com.sun.faces.cactus.ServletFacesTestCase;

/**
 * @since 2.0.0
 */
public class TestApplicationEvents extends ServletFacesTestCase {


    // ------------------------------------------------------------ Constructors


    public TestApplicationEvents() {
        super("TestApplicationEvents");
    }


    public TestApplicationEvents(String name) {
        super(name);
    }


    // ------------------------------------------------------------ Test Methods

    ////////////////////////////////////////////////////////////////////////////
    // ensure NPEs are thrown per the docs
    public void testEventsNPEs() {

        Application application = getFacesContext().getApplication();
        SystemEventListener listener = new TestListener();

        // ----------------------------------------------------------- Subscribe

        try {
            application.subscribeToEvent(null, UIViewRoot.class, listener);
            assertTrue(false);
        } catch (NullPointerException npe) {
        } catch (Exception e) {
            assertTrue(false);
        }

        try {
            application.subscribeToEvent(TestApplicationEvents.TestSystemEvent.class, UIViewRoot.class, null);
            assert(false);
        } catch (NullPointerException ignored) {
        } catch (Exception e) {
            assertTrue(false);
        }

        // --------------------------------------------------------- Unsubscribe

        try {
            application.subscribeToEvent(null, UIViewRoot.class, listener);
            assertTrue(false);
        } catch (NullPointerException ignored) {
        } catch (Exception e) {
            assertTrue(false);
        }

        try {
            application.subscribeToEvent(TestApplicationEvents.TestSystemEvent.class, UIViewRoot.class, null);
            assertTrue(false);
        } catch (NullPointerException ignored) {
        } catch (Exception e) {
            assertTrue(false);
        }

        // ------------------------------------------------------------- Publish

        try {
            application.publishEvent(getFacesContext(), null, new UIViewRoot());
            assertTrue(false);
        } catch (NullPointerException ignored) {
        } catch (Exception e) {
            assertTrue(false);
        }

        try {
            application.publishEvent(getFacesContext(),
                                     TestApplicationEvents.TestSystemEvent.class,
                                     null);
            assertTrue(false);
        } catch (NullPointerException ignored) {
        } catch (Exception e) {
            assertTrue(false);
        }

        try {
            application.publishEvent(null,
                                     TestApplicationEvents.TestSystemEvent.class,
                                     new UIViewRoot());
            assertTrue(false);
        } catch (NullPointerException ignored) {
        } catch (Exception e) {
            assertTrue(false);
        }
        
    }

    ////////////////////////////////////////////////////////////////////////////
    // Ensure component level listeners are invoked when
    // Application.publishEvent() is called.
    public void testEvents1() {

        TestComponentListener listener = new TestComponentListener();
        Application application = getFacesContext().getApplication();
        UIInput input = new UIInput();
        input.subscribeToEvent(TestApplicationEvents.TestSystemEvent3.class,
                               listener);
        application.publishEvent(getFacesContext(),
                                 TestApplicationEvents.TestSystemEvent3.class,
                                 input);
        assertTrue(listener.getPassedEvent() instanceof TestApplicationEvents.TestSystemEvent3);

        // new UIInput without any subs should result in no invocation
        listener.reset();
        UIInput input2 = new UIInput();
        application.publishEvent(getFacesContext(),
                                 TestApplicationEvents.TestSystemEvent3.class,
                                 input2);
        assertTrue(!listener.wasProcessEventInvoked());

        // unsub'd event should result in no invocations
        input.unsubscribeFromEvent(TestApplicationEvents.TestSystemEvent3.class,
                                   listener);
        application.publishEvent(getFacesContext(),
                                 TestApplicationEvents.TestSystemEvent3.class,
                                 input);
        assertTrue(!listener.wasProcessEventInvoked());
        
    }

    ////////////////////////////////////////////////////////////////////////////
    // Test Application subscribeToEvent() and unsubscribeToEvent() with
    // a specific source.
    public void testEvents2() {

        TestListener listener = new TestListener(UIViewRoot.class);
        Application application = getFacesContext().getApplication();
        application.subscribeToEvent(TestApplicationEvents.TestSystemEvent.class,
                                     UIViewRoot.class,
                                     listener);
        application.publishEvent(getFacesContext(),
                                 TestApplicationEvents.TestSystemEvent.class,
                                 getFacesContext().getViewRoot());
        assertTrue(listener.getPassedSource() == getFacesContext().getViewRoot());
        assertTrue(listener.getPassedSystemEvent() instanceof TestSystemEvent);
        assertTrue(listener.getPassedSystemEvent().getSource() == getFacesContext().getViewRoot());

        // event is setup for UIViewRoot sources, so a UIInput source shouldn't
        // trigger the listener
        UIInput input = new UIInput();
        listener.reset();
        application.publishEvent(getFacesContext(),
                                 TestApplicationEvents.TestSystemEvent.class,
                                 input);
        assertTrue(listener.getPassedSource() == null);
        assertTrue(listener.getPassedSystemEvent() == null);

        // passing a custom UIViewRoot should result in the listener being
        // triggered and the event being created
        listener.reset();
        CustomViewRoot root = new CustomViewRoot();
         application.subscribeToEvent(TestApplicationEvents.TestSystemEvent.class,
                                     CustomViewRoot.class,
                                     listener);
        application.publishEvent(getFacesContext(),
                                 TestApplicationEvents.TestSystemEvent.class,
                                 root);
        assertTrue(listener.getPassedSource() == root);
        assertTrue(listener.getPassedSystemEvent() instanceof TestSystemEvent);
        assertTrue(listener.getPassedSystemEvent().getSource() == root);

        // unsubscript and verify a publish doesn't trigger the listener
        application.unsubscribeFromEvent(TestApplicationEvents.TestSystemEvent.class,
                                         CustomViewRoot.class,
                                         listener);
        listener.reset();
        application.publishEvent(getFacesContext(),
                                 TestApplicationEvents.TestSystemEvent.class,
                                 root);
        assertTrue(!listener.wasIsListenerForSourceInvoked());
        assertTrue(!listener.wasProcessEventInvoked());

        application.unsubscribeFromEvent(TestApplicationEvents.TestSystemEvent.class,
                                         UIViewRoot.class,
                                         listener);
        listener.reset();
        application.publishEvent(getFacesContext(),
                                 TestApplicationEvents.TestSystemEvent.class,
                                 root);
        assertTrue(!listener.wasIsListenerForSourceInvoked());
        assertTrue(!listener.wasProcessEventInvoked());

        // verify multiple events for a single source works
        listener = new TestListener(UIViewRoot.class);
        application.subscribeToEvent(TestApplicationEvents.TestSystemEvent2.class,
                                     UIViewRoot.class,
                                     listener);
        application.subscribeToEvent(TestApplicationEvents.TestSystemEvent3.class,
                                     UIViewRoot.class,
                                     listener);

        application.publishEvent(getFacesContext(),
                                 TestApplicationEvents.TestSystemEvent2.class,
                                 getFacesContext().getViewRoot());
        assertTrue(listener.getPassedSource() == getFacesContext().getViewRoot());
        assertTrue(listener.getPassedSystemEvent() instanceof TestSystemEvent2);
        assertTrue(listener.getPassedSystemEvent().getSource() == getFacesContext().getViewRoot());
        listener.reset();
        application.publishEvent(getFacesContext(),
                                 TestApplicationEvents.TestSystemEvent3.class,
                                 getFacesContext().getViewRoot());
        assertTrue(listener.getPassedSource() == getFacesContext().getViewRoot());
        assertTrue(listener.getPassedSystemEvent() instanceof TestSystemEvent3);
        assertTrue(listener.getPassedSystemEvent().getSource() == getFacesContext().getViewRoot());

        application.unsubscribeFromEvent(TestApplicationEvents.TestSystemEvent2.class,
                                         UIViewRoot.class,
                                         listener);
        application.unsubscribeFromEvent(TestApplicationEvents.TestSystemEvent3.class,
                                         UIViewRoot.class,
                                         listener);

        // verify subscription for source that is an Abstract type works or
        // doesn't work depending on how the event is published.
        TestListener abstractListener = new TestListener(Application.class);
        application.subscribeToEvent(PostConstructApplicationEvent.class,
                                     Application.class,
                                     abstractListener);
        abstractListener.reset();
        application.publishEvent(getFacesContext(),
                                 PostConstructApplicationEvent.class,
                                 Application.class,
                                 application);
        assertTrue(abstractListener.getPassedSource() == application);
        assertTrue(abstractListener.getPassedSystemEvent() instanceof PostConstructApplicationEvent);
        assertTrue(abstractListener.getPassedSystemEvent().getSource() == application);

        // verify that the event isn't published when the base type isn't
        // provided with publish
        abstractListener.reset();
        application.publishEvent(getFacesContext(),
                                 PostConstructApplicationEvent.class,
                                 application);
        assertTrue(abstractListener.getPassedSource() == null);
        assertTrue(abstractListener.getPassedSystemEvent() == null);

        // cleanup
        application.unsubscribeFromEvent(PostConstructApplicationEvent.class,
                                         Application.class,
                                         abstractListener);

    }

    ////////////////////////////////////////////////////////////////////////////
    // Test Application subscribeToEvent() and unsubscribeToEvent() without
    // a specific source.
    public void testEvents3() {

        TestListener listener = new TestListener(UIComponent.class);
        Application application = getFacesContext().getApplication();
        application.subscribeToEvent(TestApplicationEvents.TestSystemEvent2.class,
                                     listener);
        application.publishEvent(getFacesContext(),
                                 TestApplicationEvents.TestSystemEvent2.class,
                                 getFacesContext().getViewRoot());
        assertTrue(listener.getPassedSource() == getFacesContext().getViewRoot());
        assertTrue(listener.getPassedSystemEvent() instanceof TestSystemEvent2);
        assertTrue(listener.getPassedSystemEvent().getSource() == getFacesContext().getViewRoot());

        // any UIComponent source should work
        listener.reset();
        UIInput input = new UIInput();
        application.publishEvent(getFacesContext(),
                                 TestApplicationEvents.TestSystemEvent2.class, input);
        assertTrue(listener.getPassedSource() == input);
        assertTrue(listener.getPassedSystemEvent() instanceof TestSystemEvent2);
        assertTrue(listener.getPassedSystemEvent().getSource() == input);

        // non UIComponent SystemEventListenerHolder shouldn't result in the
        // listener being used
        listener.reset();
        TestSystemEventListenerHolder holder = new TestSystemEventListenerHolder();
        application.publishEvent(getFacesContext(),
                                 TestApplicationEvents.TestSystemEvent2.class,
                                 holder);
        assertTrue(listener.getPassedSource() == holder);
        assertTrue(!listener.wasProcessEventInvoked());
        assertTrue(listener.getPassedSystemEvent() == null);


        // unsubscribe
        listener.reset();
        application.unsubscribeFromEvent(TestApplicationEvents.TestSystemEvent2.class, listener);
        application.publishEvent(getFacesContext(),
                                 TestApplicationEvents.TestSystemEvent2.class,
                                 input);
        assertTrue(!listener.wasIsListenerForSourceInvoked());
        assertTrue(!listener.wasProcessEventInvoked());

    }


    public void testListenersFromConfig() {

        FacesContext ctx = getFacesContext();
        Application app = ctx.getApplication();
        // SystemEventListener1 is only interested in UIOutput sources, while
        // SystemEventListener2 is interested in any events.
        app.publishEvent(getFacesContext(),
                         CustomSystemEvent.class,
                         new UIInput());
        assertNull(ctx.getAttributes().remove("SystemEventListener1"));
        assertNotNull(ctx.getAttributes().remove("SystemEventListener2"));
        app.publishEvent(getFacesContext(),
                         CustomSystemEvent.class,
                         new UIOutput());
        assertNotNull(ctx.getAttributes().remove("SystemEventListener1"));
        assertNotNull(ctx.getAttributes().remove("SystemEventListener2"));

    }

    // ----------------------------------------------------------- Inner Classes


    private static final class TestComponentListener
          implements ComponentSystemEventListener {

        private SystemEvent passedEvent;
        boolean processEventInvoked;

        // -------------------------------------------------------- Constructors


        public TestComponentListener() { }


        // --------------------------- Methods from ComponentSystemEventListener


        public void processEvent(ComponentSystemEvent event)
        throws AbortProcessingException {
            passedEvent = event;
            processEventInvoked = true;
        }


        // ------------------------------------------------------ Public Methods


        public void reset() {
            passedEvent = null;
            processEventInvoked = false;
        }

        public boolean wasProcessEventInvoked() {
            return processEventInvoked;
        }

        public SystemEvent getPassedEvent() {
            return passedEvent;
        }

    } // END TestComponentListener


    private static final class TestListener implements SystemEventListener {

        private Class<?> sourceFor;

        private Object passedSource;
        private SystemEvent passedEvent;
        boolean processEventInvoked;
        private boolean forSourceInvoked;

        // -------------------------------------------------------- Constructors


        public TestListener() { }


        public TestListener(Class<?> sourceFor) {
            this.sourceFor = sourceFor;
        }


        // ------------------------------------- Methods for SystemEventListener


        public void processEvent(SystemEvent event)
        throws AbortProcessingException {
            processEventInvoked = true;
            passedEvent = event;
        }


        public boolean isListenerForSource(Object source) {
            forSourceInvoked = true;
            passedSource = source;
            if (sourceFor == null) {
                return (source != null);
            } else {
                return sourceFor.isInstance(source);
            }
        }

        // ------------------------------------------------------ Public Methods


        public Object getPassedSource() {
            return passedSource;
        }

        public SystemEvent getPassedSystemEvent() {
            return passedEvent;
        }

        public boolean wasProcessEventInvoked() {
            return processEventInvoked;
        }

        public boolean wasIsListenerForSourceInvoked() {
            return forSourceInvoked;
        }

        public void reset() {
            passedSource = null;
            passedEvent = null;
            processEventInvoked = false;
            forSourceInvoked = false;
        }

        public void setSourceFor(Class<?> sourceFor) {
            this.sourceFor = sourceFor;
        }

    } // END TestListener


    public class CustomViewRoot extends UIViewRoot {

    } // END CustomViewRoot


    public static final class TestSystemEvent extends SystemEvent {

        private static final long serialVersionUID = -1623739732540866805L;

        // -------------------------------------------------------- Constructors


        public TestSystemEvent(UIViewRoot root) {
            super(root);
        }


        // -------------------------------------------- Methods from SystemEvent


        @Override
        public boolean isAppropriateListener(FacesListener listener) {
            return (listener instanceof TestListener);
        }


        @Override
        public void processListener(FacesListener listener) {
            super.processListener(listener);
        }

    } // END TestSystemEvent

    public static final class TestSystemEvent2 extends SystemEvent {

        private static final long serialVersionUID = -5903799319964180305L;

        // -------------------------------------------------------- Constructors


        public TestSystemEvent2(UIComponent root) {
            super(root);
        }


    } // END TestSystemEvent2

    private static final class TestSystemEventListenerHolder
          implements SystemEventListenerHolder {

        public List<SystemEventListener> getListenersForEventClass(Class<? extends SystemEvent> facesEventClass) {
            return null;
        }

    } // END TestSystemEventListenerHolder


    public static final class TestSystemEvent3 extends ComponentSystemEvent {

        private static final long serialVersionUID = 6317143707337743522L;

        public TestSystemEvent3(UIComponent component) {
            super(component);
        }

    } // END TestSystemEvent3

}
