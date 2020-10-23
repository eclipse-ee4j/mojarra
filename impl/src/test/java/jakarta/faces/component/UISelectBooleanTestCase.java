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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UISelectBoolean;
import jakarta.faces.el.ValueBinding;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>
 * Unit tests for {@link UISelectBoolean}.</p>
 */
public class UISelectBooleanTestCase extends UIInputTestCase {

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public UISelectBooleanTestCase(String name) {
        super(name);
    }

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @Override
    public void setUp() throws Exception {
        super.setUp();
        component = new UISelectBoolean();
        expectedFamily = UISelectBoolean.COMPONENT_FAMILY;
        expectedRendererType = "jakarta.faces.Checkbox";
    }

    // Return the tests included in this test case.
    public static Test suite() {
        return (new TestSuite(UISelectBooleanTestCase.class));
    }

    // ------------------------------------------------- Individual Test Methods
    // Test attribute-property transparency
    @Override
    public void testAttributesTransparency() {
        super.testAttributesTransparency();
        UISelectBoolean selectBoolean = (UISelectBoolean) component;

        selectBoolean.setSelected(false);
        assertEquals(Boolean.FALSE,
                (Boolean) selectBoolean.getAttributes().get("selected"));
        selectBoolean.setSelected(true);
        assertEquals(Boolean.TRUE,
                (Boolean) selectBoolean.getAttributes().get("selected"));
        selectBoolean.getAttributes().put("selected", Boolean.FALSE);
        assertTrue(!selectBoolean.isSelected());
        selectBoolean.getAttributes().put("selected", Boolean.TRUE);
        assertTrue(selectBoolean.isSelected());
    }

    // Test a pristine UISelectBoolean instance
    @Override
    public void testPristine() {
        super.testPristine();
        UISelectBoolean selectBoolean = (UISelectBoolean) component;
        assertTrue("not selected", !selectBoolean.isSelected());
    }

    // Test setting properties to invalid values
    @Override
    public void testPropertiesInvalid() throws Exception {
        super.testPropertiesInvalid();
        UISelectBoolean selectBoolean = (UISelectBoolean) component;
    }

    // Test setting properties to valid values
    @Override
    public void testPropertiesValid() throws Exception {
        super.testPropertiesValid();
        UISelectBoolean selectBoolean = (UISelectBoolean) component;
        selectBoolean.setSelected(true);
        assertTrue(selectBoolean.isSelected());
        assertEquals(Boolean.TRUE,
                (Boolean) selectBoolean.getValue());
        selectBoolean.setSelected(false);
        assertTrue(!selectBoolean.isSelected());
        assertEquals(Boolean.FALSE,
                (Boolean) selectBoolean.getValue());

        // Test transparency between "value" and "selected" properties
        selectBoolean.setValue(Boolean.TRUE);
        assertTrue(selectBoolean.isSelected());
        selectBoolean.setValue(Boolean.FALSE);
        assertTrue(!selectBoolean.isSelected());
        selectBoolean.resetValue();
        assertTrue(!selectBoolean.isSelected());

        // Transparency applies to value bindings as well
        assertNull(selectBoolean.getValueBinding("selected"));
        assertNull(selectBoolean.getValueBinding("value"));
        request.setAttribute("foo", Boolean.TRUE);
        ValueBinding vb = application.createValueBinding("#{foo}");
        selectBoolean.setValueBinding("selected", vb);
        assertTrue(vb == selectBoolean.getValueBinding("selected"));
        assertTrue(vb == selectBoolean.getValueBinding("value"));
        selectBoolean.setValueBinding("selected", null);
        assertNull(selectBoolean.getValueBinding("selected"));
        assertNull(selectBoolean.getValueBinding("value"));
        selectBoolean.setValueBinding("value", vb);
        assertTrue(vb == selectBoolean.getValueBinding("selected"));
        assertTrue(vb == selectBoolean.getValueBinding("value"));
        selectBoolean.setValueBinding("selected", null);
        assertNull(selectBoolean.getValueBinding("selected"));
        assertNull(selectBoolean.getValueBinding("value"));
    }

    @Override
    public void testValueBindings() {
        super.testValueBindings();
        UISelectBoolean test = (UISelectBoolean) component;

        // "value" property
        request.setAttribute("foo", "bar");
        test.resetValue();
        assertNull(test.getValue());
        test.setValueBinding("value", application.createValueBinding("#{foo}"));
        assertNotNull(test.getValueBinding("value"));
        assertEquals("bar", test.getValue());
        test.setValue("baz");
        assertEquals("baz", test.getValue());
        test.resetValue();
        assertEquals("bar", test.getValue());
        test.setValueBinding("value", null);
        assertNull(test.getValueBinding("value"));
        assertNull(test.getValue());
    }

    // --------------------------------------------------------- Support Methods
    // Create a pristine component of the type to be used in state holder tests
    @Override
    protected UIComponent createComponent() {
        UIComponent component = new UISelectBoolean();
        component.setRendererType(null);
        return (component);
    }
}
