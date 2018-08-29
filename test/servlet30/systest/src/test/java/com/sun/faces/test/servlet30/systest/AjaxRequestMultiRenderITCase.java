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
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

public class AjaxRequestMultiRenderITCase extends HtmlUnitFacesITCase {

    public AjaxRequestMultiRenderITCase(String name) {
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
        return (new TestSuite(AjaxRequestMultiRenderITCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    public void testAjaxMultiRender() throws Exception {
        getPage("/faces/ajax/ajaxRequestMultiRender.xhtml");
        System.out.println("Start ajax multi render test");

        // First we'll check the first page was output correctly
        assertTrue(check("out1", "0"));
        assertTrue(check("out2", "0"));
        assertTrue(check("out3", "0"));
        assertTrue(check("out4", "0"));

        // Submit the ajax request
        HtmlSubmitInput button1 = (HtmlSubmitInput) lastpage.getHtmlElementById("button1");
        lastpage = (HtmlPage) button1.click();

        // Check that the request succeeds
        assertTrue(check("out1", "1"));
        assertTrue(check("out2", "1"));
        assertTrue(check("out3", "1"));

        // Check that the request did NOT update the rest of the page.
        assertTrue(check("out4", "0"));

        // Submit the reset
        HtmlSubmitInput reset = (HtmlSubmitInput) lastpage.getHtmlElementById("reset");
        lastpage = (HtmlPage) reset.click();

        // Check that reset succeeds
        assertTrue(check("out1", "0"));
        assertTrue(check("out2", "0"));
        assertTrue(check("out3", "0"));
        assertTrue(check("out4", "0"));

    }

}
