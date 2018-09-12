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

import junit.framework.Test;
import junit.framework.TestSuite;
import com.gargoylesoftware.htmlunit.html.*;

public class AjaxTagResolveITCase extends HtmlUnitFacesITCase {

    public AjaxTagResolveITCase(String name) {
        super(name);
    }

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
        return (new TestSuite(AjaxTagResolveITCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    /*
     * Check that the id's resolve correctly.
     */
    public void testAjaxTagWrapping() throws Exception {
        getPage("/faces/ajax/ajaxTagResolve.xhtml");
        System.out.println("Start ajax tag resolution test");

        // First we'll check the first page was output correctly
        assertTrue(check("form1:out1", "0"));
        assertTrue(check("form1:out5", "1"));
        assertTrue(check("form2:out2", "2"));
        assertTrue(check("out3", "3"));
        assertTrue(check("out4", "4"));

        HtmlSubmitInput button;
        button = (HtmlSubmitInput) lastpage.getHtmlElementById("form1:button1");
        lastpage = (HtmlPage) button.click();
        assertTrue(check("form1:out1", "5"));

        button = (HtmlSubmitInput) lastpage.getHtmlElementById("form1:button2");
        lastpage = (HtmlPage) button.click();
        assertTrue(check("form2:out2", "6"));

        button = (HtmlSubmitInput) lastpage.getHtmlElementById("form1:button3");
        lastpage = (HtmlPage) button.click();
        assertTrue(check("out3", "7"));

        button = (HtmlSubmitInput) lastpage.getHtmlElementById("form1:button4");
        lastpage = (HtmlPage) button.click();
        assertTrue(check("form1:out1", "8"));

        button = (HtmlSubmitInput) lastpage.getHtmlElementById("form1:button5");
        lastpage = (HtmlPage) button.click();
        assertTrue(check("form1:out1", "9"));
        assertTrue(check("form2:out2", "10"));
        assertTrue(check("out3", "11"));

        // Check that nothing updated that we didn't want
        assertTrue(check("out4", "4"));
        assertTrue(check("form1:out5", "1"));

    }

}
