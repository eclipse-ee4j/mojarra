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
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.faces.model.SelectItem;

/**
 * <p>
 * Unit tests for {@link UISelectItems}.
 * </p>
 */
public class UISelectItemsTestCase extends UIComponentBaseTestCase {

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        component = new UISelectItems();
        expectedFamily = UISelectItems.COMPONENT_FAMILY;
        expectedId = null;
        expectedRendererType = null;
    }

    // ------------------------------------------------- Individual Test Methods
    // Test attribute-property transparency
    @Override
    @Test
    public void testAttributesTransparency() {
        super.testAttributesTransparency();
        UISelectItems selectItems = (UISelectItems) component;

        assertEquals(selectItems.getValue(), component.getAttributes().get("value"));
        SelectItem item = new SelectItem("foo");
        selectItems.setValue(item);
        assertEquals(item, component.getAttributes().get("value"));
        selectItems.setValue(null);
        assertNull(component.getAttributes().get("value"));
        component.getAttributes().put("value", "bar");
        assertEquals("bar", selectItems.getValue());
        component.getAttributes().put("value", null);
        assertNull(selectItems.getValue());
    }

    // Suppress lifecycle tests since we do not have a renderer
    @Override
    @Test
    public void testLifecycleManagement() {
    }

    // Test a pristine UISelectItems instance
    @Override
    @Test
    public void testPristine() {
        super.testPristine();
        UISelectItems selectItems = (UISelectItems) component;
        assertNull(selectItems.getValue());
    }

    // Test setting properties to valid values
    @Override
    @Test
    public void testPropertiesValid() throws Exception {
        super.testPropertiesValid();
        UISelectItems selectItems = (UISelectItems) component;

        // value
        SelectItem item = new SelectItem("foo");
        selectItems.setValue(item);
        assertEquals(item, selectItems.getValue());
        selectItems.setValue(null);
        assertNull(selectItems.getValue());
    }

    // --------------------------------------------------------- Support Methods
    // Create a pristine component of the type to be used in state holder tests
    @Override
    protected UIComponent createComponent() {
        UIComponent component = new UISelectItems();
        component.setRendererType(null);
        return component;
    }
}
