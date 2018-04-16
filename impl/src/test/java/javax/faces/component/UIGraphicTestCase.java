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

import javax.faces.el.ValueBinding;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>
 * Unit tests for {@link UIGraphic}.</p>
 */
public class UIGraphicTestCase extends UIComponentBaseTestCase {

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public UIGraphicTestCase(String name) {
        super(name);
    }

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @Override
    public void setUp() throws Exception {
        super.setUp();
        component = new UIGraphic();
        expectedFamily = UIGraphic.COMPONENT_FAMILY;
        expectedId = null;
        expectedRendererType = "javax.faces.Image";
    }

    // Return the tests included in this test case.
    public static Test suite() {
        return (new TestSuite(UIGraphicTestCase.class));
    }

    // ------------------------------------------------- Individual Test Methods
    // Test attribute-property transparency
    @Override
    public void testAttributesTransparency() {

        super.testAttributesTransparency();
        UIGraphic graphic = (UIGraphic) component;

        assertEquals(graphic.getValue(),
                (String) component.getAttributes().get("value"));
        graphic.setValue("foo");
        assertEquals("foo", (String) component.getAttributes().get("value"));
        graphic.setValue(null);
        assertNull((String) component.getAttributes().get("value"));
        component.getAttributes().put("value", "bar");
        assertEquals("bar", graphic.getValue());
        component.getAttributes().put("value", null);
        assertNull(graphic.getValue());

        assertEquals(graphic.getUrl(),
                (String) graphic.getAttributes().get("url"));
        graphic.setUrl("foo");
        assertEquals("foo", (String) graphic.getAttributes().get("url"));
        graphic.setUrl(null);
        assertNull((String) graphic.getAttributes().get("url"));
        graphic.getAttributes().put("url", "bar");
        assertEquals("bar", graphic.getUrl());
        graphic.getAttributes().put("url", null);
        assertNull(graphic.getUrl());
    }

    // Suppress lifecycle tests since we do not have a renderer
    @Override
    public void testLifecycleManagement() {
    }

    // Test a pristine UIGraphic instance
    @Override
    public void testPristine() {
        super.testPristine();
        UIGraphic graphic = (UIGraphic) component;

        assertNull("no value", graphic.getValue());
        assertNull("no url", graphic.getUrl());
    }

    // Test setting properties to invalid values
    @Override
    public void testPropertiesInvalid() throws Exception {
        super.testPropertiesInvalid();
        UIGraphic graphic = (UIGraphic) component;
    }

    // Test setting properties to valid values
    @Override
    public void testPropertiesValid() throws Exception {
        super.testPropertiesValid();
        UIGraphic graphic = (UIGraphic) component;

        // value
        graphic.setValue("foo.bar");
        assertEquals("expected value",
                "foo.bar", graphic.getValue());
        graphic.setValue(null);
        assertNull("erased value", graphic.getValue());

        // Test transparency between "value" and "url" properties
        graphic.setUrl("foo");
        assertEquals("foo", (String) graphic.getValue());
        graphic.setUrl(null);
        assertNull(graphic.getValue());
        graphic.setValue("bar");
        assertEquals("bar", graphic.getUrl());
        graphic.setValue(null);
        assertNull(graphic.getUrl());

        // Transparency applies to value bindings as well
        assertNull(graphic.getValueBinding("url"));
        assertNull(graphic.getValueBinding("value"));
        request.setAttribute("foo", "bar");
        ValueBinding vb = application.createValueBinding("#{foo}");
        graphic.setValueBinding("url", vb);
        assertTrue(vb == graphic.getValueBinding("url"));
        assertTrue(vb == graphic.getValueBinding("value"));
        graphic.setValueBinding("url", null);
        assertNull(graphic.getValueBinding("url"));
        assertNull(graphic.getValueBinding("value"));
        graphic.setValueBinding("value", vb);
        assertTrue(vb == graphic.getValueBinding("url"));
        assertTrue(vb == graphic.getValueBinding("value"));
        graphic.setValueBinding("url", null);
        assertNull(graphic.getValueBinding("url"));
        assertNull(graphic.getValueBinding("value"));
    }

    public void PENDING_FIXME_testValueBindings() {

        super.testValueBindings();
        UIGraphic test = (UIGraphic) component;

        // "value" property
        request.setAttribute("foo", "bar");
        test.setValue(null);
        assertNull(test.getValue());
        test.setValueBinding("value", application.createValueBinding("#{foo}"));
        assertNotNull(test.getValueBinding("value"));
        assertEquals("bar", test.getValue());
        test.setValue("baz");
        assertEquals("baz", test.getValue());
        test.setValue(null);
        assertEquals("bar", test.getValue());
        test.setValueBinding("value", null);
        assertNull(test.getValueBinding("value"));
        assertNull(test.getValue());

    }

    // --------------------------------------------------------- Support Methods
    // Create a pristine component of the type to be used in state holder tests
    @Override
    protected UIComponent createComponent() {
        UIComponent component = new UIGraphic();
        component.setRendererType(null);
        return (component);
    }
}
