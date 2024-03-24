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

/**
 * <p>
 * Unit tests for {@link UIGraphic}.
 * </p>
 */
public class UIGraphicTestCase extends UIComponentBaseTestCase {

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        component = new UIGraphic();
        expectedFamily = UIGraphic.COMPONENT_FAMILY;
        expectedId = null;
        expectedRendererType = "jakarta.faces.Image";
    }

    // ------------------------------------------------- Individual Test Methods
    // Test attribute-property transparency
    @Override
    @Test
    public void testAttributesTransparency() {

        super.testAttributesTransparency();
        UIGraphic graphic = (UIGraphic) component;

        assertEquals(graphic.getValue(), component.getAttributes().get("value"));
        graphic.setValue("foo");
        assertEquals("foo", component.getAttributes().get("value"));
        graphic.setValue(null);
        assertNull(component.getAttributes().get("value"));
        component.getAttributes().put("value", "bar");
        assertEquals("bar", graphic.getValue());
        component.getAttributes().put("value", null);
        assertNull(graphic.getValue());

        assertEquals(graphic.getUrl(), graphic.getAttributes().get("url"));
        graphic.setUrl("foo");
        assertEquals("foo", graphic.getAttributes().get("url"));
        graphic.setUrl(null);
        assertNull(graphic.getAttributes().get("url"));
        graphic.getAttributes().put("url", "bar");
        assertEquals("bar", graphic.getUrl());
        graphic.getAttributes().put("url", null);
        assertNull(graphic.getUrl());
    }

    // Suppress lifecycle tests since we do not have a renderer
    @Override
    @Test
    public void testLifecycleManagement() {
    }

    // Test a pristine UIGraphic instance
    @Override
    @Test
    public void testPristine() {
        super.testPristine();
        UIGraphic graphic = (UIGraphic) component;

        assertNull(graphic.getValue());
        assertNull(graphic.getUrl());
    }

    // Test setting properties to invalid values
    @Override
    @Test
    public void testPropertiesInvalid() throws Exception {
        super.testPropertiesInvalid();
    }

    // --------------------------------------------------------- Support Methods
    // Create a pristine component of the type to be used in state holder tests
    @Override
    protected UIComponent createComponent() {
        UIComponent component = new UIGraphic();
        component.setRendererType(null);
        return component;
    }
}
