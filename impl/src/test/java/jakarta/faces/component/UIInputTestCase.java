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

import java.util.Iterator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.event.ValueChangeListener;
import jakarta.faces.validator.Validator;

/**
 * <p>
 * Unit tests for {@link UIInput}.
 * </p>
 */
public class UIInputTestCase extends UIOutputTestCase {

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        component = new UIInput();
        expectedFamily = UIInput.COMPONENT_FAMILY;
        expectedRendererType = "jakarta.faces.Text";
    }

    // ------------------------------------------------- Individual Test Methods
    // Test attribute-property transparency
    @Override
    @Test
    public void testAttributesTransparency() {
        super.testAttributesTransparency();
        UIInput input = (UIInput) component;

        assertEquals(input.getSubmittedValue(), input.getAttributes().get("submittedValue"));
        input.setSubmittedValue("foo");
        assertEquals("foo", input.getAttributes().get("submittedValue"));
        input.setSubmittedValue(null);
        assertNull(input.getAttributes().get("submittedValue"));
        input.getAttributes().put("submittedValue", "bar");
        assertEquals("bar", input.getSubmittedValue());
        input.getAttributes().put("submittedValue", null);
        assertNull(input.getSubmittedValue());

        input.setRequired(true);
        assertEquals(Boolean.TRUE, input.getAttributes().get("required"));
        input.setRequired(false);
        assertEquals(Boolean.FALSE, input.getAttributes().get("required"));
        input.getAttributes().put("required", Boolean.TRUE);
        assertTrue(input.isRequired());
        input.getAttributes().put("required", Boolean.FALSE);
        assertTrue(!input.isRequired());

        assertEquals(input.isValid(), true);
        assertEquals(input.isValid(), ((Boolean) component.getAttributes().get("valid")).booleanValue());
        input.setValid(false);
        assertEquals(input.isValid(), ((Boolean) component.getAttributes().get("valid")).booleanValue());
        component.getAttributes().put("valid", Boolean.TRUE);
        assertEquals(input.isValid(), ((Boolean) component.getAttributes().get("valid")).booleanValue());

    }

    // Test the compareValues() method
    @Test
    public void testCompareValues() {
        InputTestImpl input = new InputTestImpl();
        Object value1a = "foo";
        Object value1b = "foo";
        Object value2 = "bar";
        Object value3 = null;

        assertTrue(!input.compareValues(value1a, value1a));
        assertTrue(!input.compareValues(value1a, value1b));
        assertTrue(!input.compareValues(value1b, value1b));
        assertTrue(!input.compareValues(value2, value2));
        assertTrue(!input.compareValues(value3, value3));

        assertTrue(input.compareValues(value1a, value2));
        assertTrue(input.compareValues(value1a, value3));
        assertTrue(input.compareValues(value2, value3));
        assertTrue(input.compareValues(value3, value2));
    }

    // Test event queuing and broadcasting (any phase listeners)
    @Test
    public void testEventsGeneric() {
        UIInput input = (UIInput) component;
        ValueChangeEvent event = new ValueChangeEvent(input, null, null);

        // Register three listeners
        input.addValueChangeListener(new ValueChangeListenerTestImpl("AP0"));
        input.addValueChangeListener(new ValueChangeListenerTestImpl("AP1"));
        input.addValueChangeListener(new ValueChangeListenerTestImpl("AP2"));

        // Fire events and evaluate results
        ValueChangeListenerTestImpl.trace(null);
        input.broadcast(event);
        assertEquals("/AP0/AP1/AP2", ValueChangeListenerTestImpl.trace());
    }

    // Test event queuing and broadcasting (mixed phase listeners)
    @Test
    public void testEventsMixed() {
        UIInput input = (UIInput) component;
        input.setRendererType(null);
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(input);
        ValueChangeEvent event = null;

        // Register three listeners
        input.addValueChangeListener(new ValueChangeListenerTestImpl("ARV"));
        input.addValueChangeListener(new ValueChangeListenerTestImpl("PV"));
        input.addValueChangeListener(new ValueChangeListenerTestImpl("AP"));

        ValueChangeListenerTestImpl.trace(null);
        event = new ValueChangeEvent(input, null, null);
        event.setPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        input.queueEvent(event);

        event = new ValueChangeEvent(input, null, null);
        event.setPhaseId(PhaseId.PROCESS_VALIDATIONS);
        input.queueEvent(event);

        event = new ValueChangeEvent(input, null, null);
        event.setPhaseId(PhaseId.INVOKE_APPLICATION);
        input.queueEvent(event);

        // Fire events and evaluate results
        root.processDecodes(facesContext);
        root.processValidators(facesContext);
        root.processApplication(facesContext);
        assertEquals("/ARV/PV/AP/ARV/PV/AP/ARV/PV/AP", ValueChangeListenerTestImpl.trace());
    }

    // Test listener registration and deregistration
    @Test
    public void testListeners() {
        InputTestImpl input = new InputTestImpl();

        input.addValueChangeListener(new ValueChangeListenerTestImpl("ARV0"));
        input.addValueChangeListener(new ValueChangeListenerTestImpl("ARV1"));
        input.addValueChangeListener(new ValueChangeListenerTestImpl("PV0"));
        input.addValueChangeListener(new ValueChangeListenerTestImpl("PV1"));
        input.addValueChangeListener(new ValueChangeListenerTestImpl("PV2"));

        ValueChangeListener listeners[] = input.getValueChangeListeners();
        assertEquals(5, listeners.length);
        input.removeValueChangeListener(listeners[2]);
        listeners = input.getValueChangeListeners();
        assertEquals(4, listeners.length);
    }

    // Test empty listener list
    @Test
    public void testEmptyListeners() {
        InputTestImpl input = new InputTestImpl();

        // No listeners added, should be empty
        ValueChangeListener listeners[] = input.getValueChangeListeners();
        assertEquals(0, listeners.length);
    }

    // Test a pristine UIInput instance
    @Override
    @Test
    public void testPristine() {
        super.testPristine();
        UIInput input = (UIInput) component;

        assertNull(input.getSubmittedValue());
        assertTrue(!input.isRequired());
        assertTrue(input.isValid());
        assertTrue(!input.isImmediate());
    }

    // Test setting properties to invalid values
    @Override
    @Test
    public void testPropertiesInvalid() throws Exception {
        super.testPropertiesInvalid();
        UIInput input = (UIInput) component;
    }

    // Test validation of a required field
    @Test
    public void testValidateRequired() throws Exception {
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(component);
        UIInput input = (UIInput) component;
        input.setRequired(true);
        checkMessages(0);

        input.setValid(true);
        input.setSubmittedValue("foo");
        input.validate(facesContext);
        checkMessages(0);
        assertTrue(input.isValid());

        input.getAttributes().put("label", "mylabel");
        input.setValid(true);
        input.setSubmittedValue("");
        input.validate(facesContext);
        checkMessages(1);
        assertTrue(!input.isValid());

        Iterator<FacesMessage> messages = facesContext.getMessages();
        while (messages.hasNext()) {
            FacesMessage message = messages.next();
            assertTrue(message.getSummary().indexOf("mylabel") >= 0);
        }

        input.setValid(true);
        input.setSubmittedValue(null);
        input.validate(facesContext);
        // awiner: this was formerly "checkMessages(2)", but a submitted
        // value of null now explicitly means _do not validate_.
        checkMessages(1);
        // awiner: And this next line flipped as well
        assertTrue(input.isValid());
    }

    @Test
    public void testGetValueChangeListeners() throws Exception {
        UIInput command = (UIInput) component;
        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.getChildren().add(command);

        ValueChangeListenerTestImpl ta1 = new ValueChangeListenerTestImpl("ta1"), ta2 = new ValueChangeListenerTestImpl("ta2");

        command.addValueChangeListener(ta1);
        command.addValueChangeListener(ta2);
        ValueChangeListener[] listeners = command.getValueChangeListeners();
        assertEquals(2, listeners.length);
        ValueChangeListenerTestImpl[] taListeners = (ValueChangeListenerTestImpl[]) command.getFacesListeners(ValueChangeListenerTestImpl.class);
        assertTrue(taListeners != null);
    }

    // --------------------------------------------------------- Support Methods
    // Check that the number of queued messages equals the expected count
    // and that each of them is of severity ERROR
    protected void checkMessages(int expected) {
        facesContext.getExceptionHandler().handle();
        int n = 0;
        Iterator<FacesMessage> messages = facesContext.getMessages();
        while (messages.hasNext()) {
            FacesMessage message = messages.next();
            assertEquals(FacesMessage.SEVERITY_ERROR, message.getSeverity());
            n++;
            // System.err.println(message.getSummary());
        }
        assertEquals(expected, n);
    }

    // Create a pristine component of the type to be used in state holder tests
    @Override
    protected UIComponent createComponent() {
        UIComponent component = new UIInput();
        component.setRendererType(null);
        return component;
    }

    protected boolean listenersAreEqual(FacesContext context, UIInput comp1, UIInput comp2) {

        ValueChangeListener list1[] = comp1.getValueChangeListeners();
        ValueChangeListener list2[] = comp2.getValueChangeListeners();
        assertNotNull(list1);
        assertNotNull(list2);
        assertEquals(list1.length, list2.length);
        for (int i = 0; i < list1.length; i++) {
            assertTrue(list1[i].getClass() == list2[i].getClass());
        }
        return true;
    }

    protected void setupNewValue(UIInput input) {
        input.setSubmittedValue("foo");
    }

    protected boolean validatorsAreEqual(FacesContext context, UIInput comp1, UIInput comp2) {

        Validator list1[] = comp1.getValidators();
        Validator list2[] = comp2.getValidators();
        assertNotNull(list1);
        assertNotNull(list2);
        assertEquals(list1.length, list2.length);
        for (int i = 0; i < list1.length; i++) {
            assertTrue(list1[i].getClass() == list2[i].getClass());
        }
        return true;
    }
}
