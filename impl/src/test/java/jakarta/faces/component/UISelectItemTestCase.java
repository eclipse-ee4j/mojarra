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

import jakarta.faces.model.SelectItem;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>
 * Unit tests for {@link UISelectItem}.
 * </p>
 */
public class UISelectItemTestCase extends UIComponentBaseTestCase {

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public UISelectItemTestCase(String name) {
        super(name);
    }

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @Override
    public void setUp() throws Exception {
        super.setUp();
        component = new UISelectItem();
        expectedFamily = UISelectItem.COMPONENT_FAMILY;
        expectedId = null;
        expectedRendererType = null;
    }

    // Return the tests included in this test case.
    public static Test suite() {
        return new TestSuite(UISelectItemTestCase.class);
    }

    // ------------------------------------------------- Individual Test Methods
    // Suppress lifecycle tests since we do not have a renderer
    @Override
    public void testLifecycleManagement() {
    }

    // Test attribute-property transparency
    @Override
    public void testAttributesTransparency() {
        super.testAttributesTransparency();
        UISelectItem selectItem = (UISelectItem) component;

        assertEquals(selectItem.getValue(), component.getAttributes().get("value"));
        SelectItem item = new SelectItem("foo");
        selectItem.setValue(item);
        assertEquals(item, component.getAttributes().get("value"));
        selectItem.setValue(null);

        assertNull(component.getAttributes().get("value"));
        component.getAttributes().put("value", "bar");
        assertEquals("bar", selectItem.getValue());
        component.getAttributes().put("value", null);
        assertNull(selectItem.getValue());

        assertEquals(selectItem.getItemDescription(), (String) selectItem.getAttributes().get("itemDescription"));
        selectItem.setItemDescription("foo");
        assertEquals("foo", (String) selectItem.getAttributes().get("itemDescription"));
        selectItem.setItemDescription(null);
        assertNull(selectItem.getAttributes().get("itemDescription"));
        selectItem.getAttributes().put("itemDescription", "bar");
        assertEquals("bar", selectItem.getItemDescription());
        selectItem.getAttributes().put("itemDescription", null);
        assertNull(selectItem.getItemDescription());

        assertEquals(selectItem.isItemDisabled(), ((Boolean) selectItem.getAttributes().get("itemDisabled")).booleanValue());
        selectItem.setItemDisabled(true);
        assertTrue(((Boolean) selectItem.getAttributes().get("itemDisabled")).booleanValue());
        selectItem.setItemDisabled(false);
        assertFalse(((Boolean) selectItem.getAttributes().get("itemDisabled")).booleanValue());
        selectItem.getAttributes().put("itemDisabled", Boolean.FALSE);
        assertFalse(selectItem.isItemDisabled());
        selectItem.getAttributes().put("itemDisabled", Boolean.TRUE);
        assertTrue(selectItem.isItemDisabled());

        assertEquals(selectItem.getItemLabel(), (String) selectItem.getAttributes().get("itemLabel"));
        selectItem.setItemLabel("foo");
        assertEquals("foo", (String) selectItem.getAttributes().get("itemLabel"));
        selectItem.setItemLabel(null);
        assertNull(selectItem.getAttributes().get("itemLabel"));
        selectItem.getAttributes().put("itemLabel", "bar");
        assertEquals("bar", selectItem.getItemLabel());
        selectItem.getAttributes().put("itemLabel", null);
        assertNull(selectItem.getItemLabel());

        assertEquals(selectItem.getItemValue(), selectItem.getAttributes().get("itemValue"));
        selectItem.setItemValue("foo");
        assertEquals("foo", (String) selectItem.getAttributes().get("itemValue"));
        selectItem.setItemValue(null);
        assertNull(selectItem.getAttributes().get("itemValue"));
        selectItem.getAttributes().put("itemValue", "bar");
        assertEquals("bar", selectItem.getItemValue());
        selectItem.getAttributes().put("itemValue", null);
        assertNull(selectItem.getItemValue());
    }

    // Test a pristine UISelectItem instance
    @Override
    public void testPristine() {
        super.testPristine();
        UISelectItem selectItem = (UISelectItem) component;

        assertNull("no value", selectItem.getValue());
        assertNull("no itemDescription", selectItem.getItemDescription());
        assertFalse("no itemDisabled", selectItem.isItemDisabled());
        assertNull("no itemLabel", selectItem.getItemLabel());
        assertNull("no itemValue", selectItem.getItemValue());
    }

    // Test setting properties to valid values
    @Override
    public void testPropertiesValid() throws Exception {
        super.testPropertiesValid();
        UISelectItem selectItem = (UISelectItem) component;

        // value
        SelectItem item = new SelectItem("foo");
        selectItem.setValue(item);
        assertEquals("expected value", item, selectItem.getValue());
        selectItem.setValue(null);
        assertNull("erased value", selectItem.getValue());

        selectItem.setItemDescription("foo");
        assertEquals("foo", selectItem.getItemDescription());
        selectItem.setItemDescription(null);
        assertNull(selectItem.getItemDescription());

        selectItem.setItemDisabled(false);
        assertFalse(selectItem.isItemDisabled());
        selectItem.setItemDisabled(true);
        assertTrue(selectItem.isItemDisabled());

        selectItem.setItemLabel("foo");
        assertEquals("foo", selectItem.getItemLabel());
        selectItem.setItemLabel(null);
        assertNull(selectItem.getItemLabel());

        selectItem.setItemValue("foo");
        assertEquals("foo", selectItem.getItemValue());
        selectItem.setItemValue(null);
        assertNull(selectItem.getItemValue());
    }

    // --------------------------------------------------------- Support Methods
    // Check that the properties on the specified components are equal
    @Override
    protected void checkProperties(UIComponent comp1, UIComponent comp2) {
        super.checkProperties(comp1, comp2);
        UISelectItem si1 = (UISelectItem) comp1;
        UISelectItem si2 = (UISelectItem) comp2;
        assertEquals(si1.getItemDescription(), si2.getItemDescription());
        assertEquals(si1.isItemDisabled(), si2.isItemDisabled());
        assertEquals(si1.getItemLabel(), si2.getItemLabel());
        assertEquals(si1.getItemValue(), si2.getItemValue());
    }

    // Create a pristine component of the type to be used in state holder tests
    @Override
    protected UIComponent createComponent() {
        UIComponent component = new UISelectItem();
        component.setRendererType(null);
        return component;
    }

}
