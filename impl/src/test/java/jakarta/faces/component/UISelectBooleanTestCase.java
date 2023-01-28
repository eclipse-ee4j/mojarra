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
 * Unit tests for {@link UISelectBoolean}.
 * </p>
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
        return new TestSuite(UISelectBooleanTestCase.class);
    }

    // ------------------------------------------------- Individual Test Methods
    // Test attribute-property transparency
    @Override
    public void testAttributesTransparency() {
        super.testAttributesTransparency();
        UISelectBoolean selectBoolean = (UISelectBoolean) component;

        selectBoolean.setSelected(false);
        assertEquals(Boolean.FALSE, selectBoolean.getAttributes().get("selected"));
        selectBoolean.setSelected(true);
        assertEquals(Boolean.TRUE, selectBoolean.getAttributes().get("selected"));
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

    // --------------------------------------------------------- Support Methods
    // Create a pristine component of the type to be used in state holder tests
    @Override
    protected UIComponent createComponent() {
        UIComponent component = new UISelectBoolean();
        component.setRendererType(null);
        return component;
    }
}
