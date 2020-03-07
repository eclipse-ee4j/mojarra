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

import junit.framework.Test;
import junit.framework.TestSuite;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.ValueHolder;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.IntegerConverter;
import jakarta.faces.webapp.ConverterTag;
import jakarta.faces.webapp.UIComponentTag;

/**
 * <p>
 * Unit tests for <code>ConverterTag</code>.</p>
 */
public class ConverterTagTestCase extends TagTestCaseBase {

    // ------------------------------------------------------ Instance Variables
    protected UIComponentTag ctag = null; // Component tag
    protected UIComponentTag rtag = null; // Root tag

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public ConverterTagTestCase(String name) {
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
     * Return the tests include
     * @return d in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(ConverterTagTestCase.class));
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
    // Test literal converter id
    public void testLiteral() throws Exception {
        UIComponent component = ctag.getComponentInstance();
        assertNotNull(component);
        assertNull(((ValueHolder) component).getConverter());
        ConverterTag tag = new ConverterTag();
        tag.setConverterId("Integer");
        add(tag);
        tag.doStartTag();
        Converter converter = ((ValueHolder) component).getConverter();
        assertNotNull(converter);
        assertTrue(converter instanceof IntegerConverter);
        tag.doEndTag();
    }

    // Test expression converter id
    public void testExpression() throws Exception {
        UIComponent component = ctag.getComponentInstance();
        assertNotNull(component);
        assertNull(((ValueHolder) component).getConverter());
        ConverterTag tag = new ConverterTag();
        tag.setConverterId("#{foo}");
        add(tag);
        request.setAttribute("foo", "Integer");
        tag.doStartTag();
        Converter converter = ((ValueHolder) component).getConverter();
        assertNotNull(converter);
        assertTrue(converter instanceof IntegerConverter);
        tag.doEndTag();
    }

    // ------------------------------------------------------- Protected Methods
    // Add the specified ConverterTag to our component tag
    protected void add(ConverterTag tag) {
        tag.setParent(ctag);
        tag.setPageContext(this.pageContext);
    }
}
