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

package jakarta.faces.webapp;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import jakarta.faces.component.UIComponent;
import jakarta.faces.webapp.AttributeTag;
import jakarta.faces.webapp.UIComponentTag;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>
 * Unit tests for <code>AttributeTag</code>.</p>
 */
public class AttributeTagTestCase extends TagTestCaseBase {

    // ------------------------------------------------------ Instance Variables
    protected UIComponentTag ctag = null; // Component tag
    protected UIComponentTag rtag = null; // Root tag

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public AttributeTagTestCase(String name) {
        super(name);
    }

    // ---------------------------------------------------- Overall Test Methods
    /**
     * Set up our root and component tags.
     * @throws java.lang.Exception
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        rtag = new TagTestImpl("ROOT", "root") {
            @Override
            protected void setProperties(UIComponent component) {
            }
        };
        rtag.setPageContext(this.pageContext);
        ctag = new OutputTagTestImpl();
        ctag.setParent(this.rtag);
        ctag.setPageContext(this.pageContext);
        rtag.doStartTag();
        ctag.doStartTag();
    }

    /**
     * Return the tests included in this test suite.
     * @return 
     */
    public static Test suite() {
        return (new TestSuite(AttributeTagTestCase.class));
    }

    /**
     * Clear our root and component tags.
     * @throws java.lang.Exception
     */
    @Override
    public void tearDown() throws Exception {
        ctag.doEndTag();
        rtag.doEndTag();

        ctag = null;
        rtag = null;

        super.tearDown();
    }

    // ------------------------------------------------- Individual Test Methods
    // Test literal name and literal value
    public void testLiteralLiteral() throws Exception {
        UIComponent component = ((UIComponentTag) ctag).getComponentInstance();
        assertNotNull(component);
        assertTrue(!component.getAttributes().containsKey("foo"));
        AttributeTag tag = new AttributeTag();
        tag.setName("foo");
        tag.setValue("bar");
        add(tag);
        tag.doStartTag();
        assertEquals("bar",
                (String) component.getAttributes().get("foo"));
        tag.doEndTag();
    }

    // Test literal name and expression value
    public void testLiteralExpression() throws Exception {
        UIComponent component = ((UIComponentTag) ctag).getComponentInstance();
        assertNotNull(component);
        assertTrue(!component.getAttributes().containsKey("foo"));
        AttributeTag tag = new AttributeTag();
        tag.setName("foo");
        tag.setValue("#{barValue}");
        add(tag);
        request.setAttribute("barValue", "bar");
        tag.doStartTag();
        assertEquals("bar",
                (String) component.getAttributes().get("foo"));
        tag.doEndTag();
    }

    // Test expression name and literal value
    public void testExpressionLiteral() throws Exception {
        UIComponent component = ((UIComponentTag) ctag).getComponentInstance();
        assertNotNull(component);
        assertTrue(!component.getAttributes().containsKey("foo"));
        AttributeTag tag = new AttributeTag();
        tag.setName("#{fooValue}");
        tag.setValue("bar");
        add(tag);
        request.setAttribute("fooValue", "foo");
        tag.doStartTag();
        assertEquals("bar",
                (String) component.getAttributes().get("foo"));
        tag.doEndTag();
    }

    // Test expression name and expression value
    public void testExpressionExpression() throws Exception {
        UIComponent component = ((UIComponentTag) ctag).getComponentInstance();
        assertNotNull(component);
        assertTrue(!component.getAttributes().containsKey("foo"));
        AttributeTag tag = new AttributeTag();
        tag.setName("#{fooValue}");
        tag.setValue("#{barValue}");
        add(tag);
        request.setAttribute("fooValue", "foo");
        request.setAttribute("barValue", "bar");
        tag.doStartTag();
        assertEquals("bar",
                (String) component.getAttributes().get("foo"));
        tag.doEndTag();
    }

    // Test pre-existing attribute
    public void testPreExisting() throws Exception {
        UIComponent component = ((UIComponentTag) ctag).getComponentInstance();
        assertNotNull(component);
        component.getAttributes().put("foo", "bap");
        AttributeTag tag = new AttributeTag();
        tag.setName("foo");
        tag.setValue("bar");
        add(tag);
        tag.doStartTag();
        assertEquals("bap",
                (String) component.getAttributes().get("foo"));
        tag.doEndTag();
    }

    // ------------------------------------------------------- Protected Methods
    // Add the specified AttributeTag to our component tag
    protected void add(AttributeTag tag) {
        tag.setParent(root);
        tag.setPageContext(this.pageContext);
    }
}
