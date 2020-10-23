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

import java.util.ArrayList;
import java.util.List;
import static junit.framework.TestCase.assertTrue;


/**
 * <p>Test Case for f:setProperty.</p>
 */

public class SetPropertyITCase extends HtmlUnitFacesITCase {

    // ------------------------------------------------------------ Constructors


    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public SetPropertyITCase(String name) {
        super(name);
    }

    // ------------------------------------------------------ Instance Variables

    // ---------------------------------------------------- Overall Test Methods


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        super.setUp();
    }


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(SetPropertyITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }

    // ------------------------------------------------- Individual Test Methods

    public void testSetPropertyPositive() throws Exception {
        HtmlPage page = getPage("/faces/jsp/jsp-setProperty-01.jsp");

        // press the button to increment the property
        assertTrue(page.asText().contains("Integer Property is: 123."));
        assertTrue(page
                .asText().contains("String Property is: This is a String property."));
        List buttons = getAllElementsOfGivenClass(page, new ArrayList(),
                HtmlSubmitInput.class);
        page = (HtmlPage) ((HtmlSubmitInput) buttons.get(0)).click();
        assertTrue(page.asText().contains("Integer Property is: 100."));
        assertTrue(page
                .asText().contains("String Property is: This is a String property."));

        buttons = getAllElementsOfGivenClass(page, new ArrayList(),
                HtmlSubmitInput.class);
        page = (HtmlPage) ((HtmlSubmitInput) buttons.get(1)).click();
        assertTrue(page.asText().contains("Integer Property is: 100."));
        assertTrue(page.asText().contains("String Property is: 100."));

        buttons = getAllElementsOfGivenClass(page, new ArrayList(),
                HtmlSubmitInput.class);
        page = (HtmlPage) ((HtmlSubmitInput) buttons.get(2)).click();
        assertTrue(page.asText().contains("Integer Property is: 100."));
        assertTrue(page.asText().contains("String Property is: String."));

        buttons = getAllElementsOfGivenClass(page, new ArrayList(),
                HtmlSubmitInput.class);
        page = (HtmlPage) ((HtmlSubmitInput) buttons.get(3)).click();
        assertTrue(page.asText().contains("Integer Property is: 100."));
        assertTrue(page
                .asText().contains("String Property is: com.sun.faces.context.FacesContextImpl"));

    }


}
