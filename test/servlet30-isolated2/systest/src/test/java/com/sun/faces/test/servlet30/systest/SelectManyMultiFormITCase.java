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

import com.gargoylesoftware.htmlunit.html.*;
import junit.framework.Test;
import junit.framework.TestSuite;

import javax.faces.component.NamingContainer;

/**
 * <p>
 * Test Case for JSP Interoperability.
 * </p>
 */

public class SelectManyMultiFormITCase extends HtmlUnitFacesITCase {

    // ------------------------------------------------------------ Constructors

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public SelectManyMultiFormITCase(String name) {
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
        return (new TestSuite(SelectManyMultiFormITCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // ------------------------------------------------- Individual Test Methods

    public void testMultiForm() throws Exception {
        HtmlForm form;
        HtmlSubmitInput submit;
        HtmlAnchor link;
        HtmlTextInput input;
        HtmlPage page;

        page = getPage("/faces/standard/selectmany01.jsp");
        // verify that the model tier is as expected
        assertTrue(-1 != page.asText().indexOf("Current model value: 1, 2, ,"));
        form = getFormById(page, "form2");
        submit = (HtmlSubmitInput) form.getInputByName("form2" + NamingContainer.SEPARATOR_CHAR + "doNotModify");

        // press button1
        page = (HtmlPage) submit.click();
        // verify that submitting the form does not change the model tier
        assertTrue(-1 != page.asText().indexOf("Current model value: 1, 2, ,"));

    }
}
