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
import static junit.framework.TestCase.assertTrue;

public class CommandButtonITCase extends HtmlUnitFacesITCase {

    public CommandButtonITCase(String name) {
        super(name);
    }

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
        return (new TestSuite(CommandButtonITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }


    public void testCommandButtonButton() throws Exception {
        getPage("/faces/render/commandButtonButton.xhtml");
        System.out.println("Start command Button type=button test");
        // First we'll check the first page was output correctly
        assertTrue(check("out1","0"));
        assertTrue(check("outside","1"));

        // Submit the ajax tagged request
        HtmlButtonInput button = (HtmlButtonInput) lastpage.getHtmlElementById("button1");
        lastpage = (HtmlPage) button.click();

        // Check that the ajax request succeeds
        assertTrue(check("out1","2"));

        // Check that the request did NOT update the rest of the page.
        assertTrue(check("outside","1"));

        // Submit the onclick enhanced request - with no return false
        button = (HtmlButtonInput) lastpage.getHtmlElementById("button2");
        lastpage = (HtmlPage) button.click();

        // Check that the ajax request succeeds
        assertTrue(check("out1","3"));

        // Check that the request did NOT update the rest of the page.
        assertTrue(check("outside","1"));

    }
}
