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

package jakarta.faces.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.faces.mock.MockExternalContext;
import com.sun.faces.mock.MockFacesContext;
import com.sun.faces.mock.MockHttpServletRequest;
import com.sun.faces.mock.MockHttpServletResponse;
import com.sun.faces.mock.MockLifecycle;
import com.sun.faces.mock.MockServletContext;

import jakarta.faces.FactoryFinder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.FacesListener;
import jakarta.faces.event.ValueChangeListener;

public class UIComponentBaseAttachedStateTestCase {

    private MockFacesContext facesContext = null;
    private MockServletContext servletContext;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    public void setUp() throws Exception {
        facesContext = new MockFacesContext();

        servletContext = new MockServletContext();
        servletContext.addInitParameter("appParamName", "appParamValue");
        servletContext.setAttribute("appScopeName", "appScopeValue");
        request = new MockHttpServletRequest(null);
        request.setAttribute("reqScopeName", "reqScopeValue");
        response = new MockHttpServletResponse();

        // Create something to stand-in as the InitFacesContext
        new MockFacesContext(new MockExternalContext(servletContext, request, response), new MockLifecycle());

    }

    @AfterEach
    public void tearDown() throws Exception {
        FactoryFinder.releaseFactories();
        Method reInitializeFactoryManager = FactoryFinder.class.getDeclaredMethod("reInitializeFactoryManager", (Class<?>[]) null);
        reInitializeFactoryManager.setAccessible(true);
        reInitializeFactoryManager.invoke(null, (Object[]) null);
    }


    @Test
    public void testAttachedObjectsSet() throws Exception {
        Set<ValueChangeListener> attachedObjects = new HashSet<>();
        ValueChangeListener toAdd = new ValueChangeListenerTestImpl();
        attachedObjects.add(toAdd);
        toAdd = new ValueChangeListenerTestImpl();
        attachedObjects.add(toAdd);
        toAdd = new ValueChangeListenerTestImpl();
        attachedObjects.add(toAdd);
        Object result = UIComponentBase.saveAttachedState(facesContext, attachedObjects);
        @SuppressWarnings("unchecked")
        Set<ValueChangeListener> returnedAttachedObjects = (Set<ValueChangeListener>) UIComponentBase
                .restoreAttachedState(facesContext, result);
        assertNotNull(returnedAttachedObjects);
    }

    @Test
    public void testAttachedObjectsStack() throws Exception {
        Stack<ValueChangeListener> attachedObjects = new Stack<>();
        ValueChangeListener toAdd = new ValueChangeListenerTestImpl();
        attachedObjects.add(toAdd);
        toAdd = new ValueChangeListenerTestImpl();
        attachedObjects.add(toAdd);
        toAdd = new ValueChangeListenerTestImpl();
        attachedObjects.add(toAdd);
        Object result = UIComponentBase.saveAttachedState(facesContext, attachedObjects);
        @SuppressWarnings("unchecked")
        Stack<ValueChangeListener> returnedAttachedObjects = (Stack<ValueChangeListener>) UIComponentBase
                .restoreAttachedState(facesContext, result);
        assertNotNull(returnedAttachedObjects);
    }

    @Test
    public void testAttachedObjectsMap() throws Exception {
        Map<String, ValueChangeListener> attachedObjects = new HashMap<>();
        ValueChangeListener toAdd = new ValueChangeListenerTestImpl();
        attachedObjects.put("one", toAdd);
        toAdd = new ValueChangeListenerTestImpl();
        attachedObjects.put("two", toAdd);
        toAdd = new ValueChangeListenerTestImpl();
        attachedObjects.put("three", toAdd);
        Object result = UIComponentBase.saveAttachedState(facesContext, attachedObjects);
        @SuppressWarnings("unchecked")
        Map<String, ValueChangeListener> returnedAttachedObjects = (Map<String, ValueChangeListener>) UIComponentBase
                .restoreAttachedState(facesContext, result);
        assertNotNull(returnedAttachedObjects);
    }

    // Regression test for bug #907
    @SuppressWarnings("unchecked")
    public void testAttachedObjectsCount() throws Exception {
        Set<ValueChangeListener> returnedAttachedObjects = null, attachedObjects = new HashSet<>();
        ValueChangeListener toAdd = new ValueChangeListenerTestImpl();
        attachedObjects.add(toAdd);
        toAdd = new ValueChangeListenerTestImpl();
        attachedObjects.add(toAdd);
        toAdd = new ValueChangeListenerTestImpl();
        attachedObjects.add(toAdd);
        Object result = UIComponentBase.saveAttachedState(facesContext, attachedObjects);
        returnedAttachedObjects = (Set<ValueChangeListener>) UIComponentBase.restoreAttachedState(facesContext, result);
        int firstSize = returnedAttachedObjects.size();
        returnedAttachedObjects = (Set<ValueChangeListener>) UIComponentBase.restoreAttachedState(facesContext, result);
        int secondSize = returnedAttachedObjects.size();
        assertEquals(firstSize, secondSize);
    }

    @Test
    public void testFacesListenerState() {
        UIComponent component = new UIOutput();
        TestFacesListener listener = new TestFacesListener();
        listener.setValue("initial");
        component.addFacesListener(listener);
        component.markInitialState();
        assertTrue(component.initialStateMarked());
        assertTrue(listener.initialStateMarked());

        Object state = component.saveState(facesContext);
        assertNull(state);

        component = new UIOutput();
        listener = new TestFacesListener();
        component.addFacesListener(listener);
        listener.setValue("initial");
        component.markInitialState();
        listener.setValue("newvalue");
        state = component.saveState(facesContext);
        assertNotNull(state);

        // verify that state is applied to existing Listener instances.
        component = new UIOutput();
        listener = new TestFacesListener();
        component.addFacesListener(listener);
        listener.setValue("newinitial");
        component.restoreState(facesContext, state);
        assertEquals("newvalue", listener.getValue());

        // verify listeners are overwritten when using full state saving
        component = new UIOutput();
        listener = new TestFacesListener();
        component.addFacesListener(listener);
        listener.setValue("initial");
        state = component.saveState(facesContext);
        assertNotNull(state);
        listener.setValue("postsave");

        component.restoreState(facesContext, state);
        TestFacesListener l = (TestFacesListener) component.getFacesListeners(TestFacesListener.class)[0];
        assertTrue(l != listener);
        assertEquals("initial", l.getValue());
    }

    @Test
    public void testTransientListenersState() {
        UIComponent output = new UIOutput();
        output.markInitialState();
        TestFacesListener l1 = new TestFacesListener();
        TestFacesListener l2 = new TestFacesListener();
        TestFacesListener l3 = new TestFacesListener();
        TestFacesListener l4 = new TestFacesListener();
        l1.setValue("l1");
        l2.setValue("l2");
        l3.setValue("l3");
        l4.setValue("l4");
        l2.setTransient(true);
        l4.setTransient(true);

        output.addFacesListener(l1);
        output.addFacesListener(l2);
        output.addFacesListener(l3);
        output.addFacesListener(l4);

        Object state = output.saveState(facesContext);
        assertNotNull(state);
        output = new UIOutput();
        output.restoreState(facesContext, state);
        FacesListener[] listeners = output.getFacesListeners(TestFacesListener.class);
        assertTrue(listeners.length == 2);
        assertEquals("l1", ((TestFacesListener) listeners[0]).getValue());
        assertEquals("l3", ((TestFacesListener) listeners[1]).getValue());

        output = new UIOutput();
        output.markInitialState();
        output.addFacesListener(l2);
        state = output.saveState(facesContext);
        assertNotNull(state);
        output = new UIOutput();
        output.restoreState(facesContext, state);
        listeners = output.getFacesListeners(TestFacesListener.class);
        assertTrue(listeners.length == 0);
    }

    @Test
    public void testTransientListenersState2() {
        UIComponent output = new UIOutput();
        TestFacesListener l1 = new TestFacesListener();
        TestFacesListener l2 = new TestFacesListener();
        TestFacesListener l3 = new TestFacesListener();
        TestFacesListener l4 = new TestFacesListener();
        l1.setValue("l1");
        l2.setValue("l2");
        l3.setValue("l3");
        l4.setValue("l4");
        l2.setTransient(true);
        l4.setTransient(true);

        output.addFacesListener(l1);
        output.addFacesListener(l2);
        output.addFacesListener(l3);
        output.addFacesListener(l4);

        Object state = output.saveState(facesContext);
        assertNotNull(state);
        output = new UIOutput();
        output.restoreState(facesContext, state);
        FacesListener[] listeners = output.getFacesListeners(TestFacesListener.class);
        assertTrue(listeners.length == 2);
        assertEquals("l1", ((TestFacesListener) listeners[0]).getValue());
        assertEquals("l3", ((TestFacesListener) listeners[1]).getValue());

        output = new UIOutput();
        output.addFacesListener(l2);
        state = output.saveState(facesContext);
        assertNotNull(state);
        output = new UIOutput();
        output.restoreState(facesContext, state);
        listeners = output.getFacesListeners(TestFacesListener.class);
        assertTrue(listeners.length == 0);
    }

    // ---------------------------------------------------------- Nested Classes
    public static final class TestFacesListener implements FacesListener, PartialStateHolder {

        private boolean initialState;
        private String value;
        private boolean trans;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            clearInitialState();
            this.value = value;
        }

        @Override
        public void markInitialState() {
            initialState = true;
        }

        @Override
        public boolean initialStateMarked() {
            return initialState;
        }

        @Override
        public void clearInitialState() {
            initialState = false;
        }

        @Override
        public Object saveState(FacesContext context) {
            return !initialState ? new Object[] { value } : null;
        }

        @Override
        public void restoreState(FacesContext context, Object state) {
            if (state != null) {
                Object[] values = (Object[]) state;
                value = (String) values[0];
            }
        }

        @Override
        public boolean isTransient() {
            return trans;
        }

        @Override
        public void setTransient(boolean trans) {
            this.trans = trans;
        }
    }
}
