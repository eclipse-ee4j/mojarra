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

public class AjaxMultiformITCase extends HtmlUnitFacesITCase {

    public AjaxMultiformITCase(String name) {
        super(name);
    }

    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    /*
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(AjaxMultiformITCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    public void testAjaxMultiform() throws Exception {
        getPage("/faces/ajax/ajaxMultiform.xhtml");
        System.out.println("Start ajax multiform test");
        // First we'll check the first page was output correctly
        checkTrue("countForm1:out1", "0");
        checkTrue("countForm2:out1", "1");
        checkTrue("countForm3:out1", "2");
        checkTrue("out2", "3");

        // Submit the ajax request
        HtmlSubmitInput button1 = (HtmlSubmitInput) lastpage.getHtmlElementById("countForm1:button1");
        lastpage = (HtmlPage) button1.click();

        // Check that the ajax request succeeds
        checkTrue("countForm1:out1", "4");

        // Check that the request did NOT update the rest of the page.
        checkTrue("out2", "3");

        // Submit the ajax request
        button1 = (HtmlSubmitInput) lastpage.getHtmlElementById("countForm2:button1");
        lastpage = (HtmlPage) button1.click();

        // Check that the ajax request succeeds
        checkTrue("countForm2:out1", "5");

        // Check that the request did NOT update the rest of the page.
        checkTrue("out2", "3");
    }
}
