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

package javax.faces.component;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>
 * Unit tests for {@link UIOutput}.</p>
 */
public class UIOutputTestCase extends ValueHolderTestCaseBase {

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public UIOutputTestCase(String name) {
        super(name);
    }

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @Override
    public void setUp() throws Exception {
        super.setUp();
        component = new UIOutput();
        expectedFamily = UIOutput.COMPONENT_FAMILY;
        expectedId = null;
        expectedRendererType = "javax.faces.Text";
    }

    // Return the tests included in this test case.
    public static Test suite() {
        return (new TestSuite(UIOutputTestCase.class));
    }

    // ------------------------------------------------- Individual Test Methods
    // Test attribute-property transparency
    @Override
    public void testAttributesTransparency() {

        super.testAttributesTransparency();
        UIOutput output = (UIOutput) component;

    }

    // Suppress lifecycle tests since we do not have a renderer
    @Override
    public void testLifecycleManagement() {
    }

    // Test a pristine UIOutput instance
    @Override
    public void testPristine() {

        super.testPristine();
        UIOutput output = (UIOutput) component;

    }

    // Test setting properties to invalid values
    @Override
    public void testPropertiesInvalid() throws Exception {

        super.testPropertiesInvalid();
        UIOutput output = (UIOutput) component;

    }

    // Test setting properties to valid values
    @Override
    public void testPropertiesValid() throws Exception {

        super.testPropertiesValid();
        UIOutput output = (UIOutput) component;

    }

    // --------------------------------------------------------- Support Methods
    // Create a pristine component of the type to be used in state holder tests
    @Override
    protected UIComponent createComponent() {
        UIComponent component = new UIOutput();
        component.setRendererType(null);
        return (component);
    }
}
