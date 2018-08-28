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

package com.sun.faces.test.servlet30.systest;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>
 * Test Case for JSP Interoperability.
 * </p>
 */
public class ViewXmlITCase extends HtmlUnitFacesITCase {

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public ViewXmlITCase(String name) {
        super(name);
    }

    // ------------------------------------------------------ Instance Variables
    // ---------------------------------------------------- Overall Test Methods
    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(ViewXmlITCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // ------------------------------------------------- Individual Test Methods
    public void testPrefixAndExtensionMapping() throws Exception {
        doTest("/view.xml/templateClientUsingXmlAndXhtml.faces", "foo", "bar");
        doTest("/faces/view.xml/templateClientUsingXmlAndXhtml.view.xml", "baz", "baba");
    }

    public void testSimpleXml() throws Exception {
        HtmlPage page = getPage("/view.xml/index.faces");
        String text = page.asText();
        assertTrue(text.matches("(?s).*Raw XML View\\s*hello\\s*reload.*"));

    }

    private void doTest(String path, String param1, String param2) throws Exception {
        HtmlPage page = getPage(path + "?param1=" + param1 + "&param2=" + param2);
        String text = page.asText();
        assertTrue(text.matches(
                "(?s).*Templating and XML views\\s*column1\\s*column2\\s*column3\\s*This is the header text declared in xhtmlTemplate.xhtml. The preceding columns are declared in header.view.xml.\\s*hello\\s*reload\\s*HTML table column 1\\s*HTML table column 2\\s*"
                        + param1 + "\\s*" + param2 + ".*"));
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();
        text = page.asText();
        assertTrue(text.matches(
                "(?s).*Templating and XML views\\s*column1\\s*column2\\s*column3\\s*This is the header text declared in xhtmlTemplate.xhtml. The preceding columns are declared in header.view.xml.\\s*hello\\s*reload\\s*HTML table column 1\\s*HTML table column 2\\s*"
                        + param1 + "\\s*" + param2 + ".*"));

    }
}
