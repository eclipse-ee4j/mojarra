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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>
 * Unit tests for {@link UIParameter}.
 * </p>
 */
public class UIParameterTestCase extends UIComponentBaseTestCase {

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public UIParameterTestCase(String name) {
        super(name);
    }

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @Override
    public void setUp() throws Exception {
        super.setUp();
        component = new UIParameter();
        expectedFamily = UIParameter.COMPONENT_FAMILY;
        expectedId = null;
        expectedRendererType = null;
    }

    // Return the tests included in this test case.
    public static Test suite() {
        return new TestSuite(UIParameterTestCase.class);
    }

    // ------------------------------------------------- Individual Test Methods
    // Test attribute-property transparency
    @Override
    public void testAttributesTransparency() {
        super.testAttributesTransparency();
        UIParameter parameter = (UIParameter) component;

        assertEquals(parameter.getValue(), component.getAttributes().get("value"));
        parameter.setValue("foo");
        assertEquals("foo", (String) component.getAttributes().get("value"));
        parameter.setValue(null);
        assertNull(component.getAttributes().get("value"));
        component.getAttributes().put("value", "bar");
        assertEquals("bar", parameter.getValue());
        component.getAttributes().put("value", null);
        assertNull(parameter.getValue());

        assertEquals(parameter.getName(), (String) parameter.getAttributes().get("name"));
        parameter.setName("foo");
        assertEquals("foo", (String) parameter.getAttributes().get("name"));
        parameter.setName(null);
        assertNull(parameter.getAttributes().get("name"));
        parameter.getAttributes().put("name", "bar");
        assertEquals("bar", parameter.getName());
        parameter.getAttributes().put("name", null);
        assertNull(parameter.getName());
    }

    // Suppress lifecycle tests since we do not have a renderer
    @Override
    public void testLifecycleManagement() {
    }

    // Test a pristine UIParameter instance
    @Override
    public void testPristine() {
        super.testPristine();
        UIParameter parameter = (UIParameter) component;

        assertNull("no value", parameter.getValue());
        assertNull("no name", parameter.getName());
    }

    // Test setting properties to valid values
    @Override
    public void testPropertiesValid() throws Exception {
        super.testPropertiesValid();
        UIParameter parameter = (UIParameter) component;

        // value
        parameter.setValue("foo.bar");
        assertEquals("expected value", "foo.bar", parameter.getValue());
        parameter.setValue(null);
        assertNull("erased value", parameter.getValue());

        parameter.setName("foo");
        assertEquals("foo", parameter.getName());
        parameter.setName(null);
        assertNull(parameter.getName());
    }

    // --------------------------------------------------------- Support Methods
    // Check that the properties on the specified components are equal
    @Override
    protected void checkProperties(UIComponent comp1, UIComponent comp2) {
        super.checkProperties(comp1, comp2);
        UIParameter p1 = (UIParameter) comp1;
        UIParameter p2 = (UIParameter) comp2;
        assertEquals(p1.getName(), p2.getName());
    }

    // Create a pristine component of the type to be used in state holder tests
    @Override
    protected UIComponent createComponent() {
        UIComponent component = new UIParameter();
        component.setRendererType(null);
        return component;
    }
}
