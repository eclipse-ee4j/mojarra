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

import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import junit.framework.Test;
import junit.framework.TestSuite;

public class AjaxIncludedTextITCase extends HtmlUnitFacesITCase {

    public AjaxIncludedTextITCase(String name) {
        super(name);
    }

    /*
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
        return (new TestSuite(AjaxIncludedTextITCase.class));
    }

    /*
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    public void testAjaxIncludedText() throws Exception {
        getPage("/faces/ajax/ajaxIncludedText.xhtml");
        System.out.println("Start ajax included text test");

        // First we'll check the first page was output correctly
        checkTrue("finalSpan1", "Text should stay");
        checkTrue("finalSpan2", "Text should stay");

        // Submit the ajax request
        HtmlSubmitInput button1 = (HtmlSubmitInput) lastpage.getHtmlElementById("form1:refresh");
        lastpage = (HtmlPage) button1.click();
        HtmlSubmitInput button2 = (HtmlSubmitInput) lastpage.getHtmlElementById("form2:refresh");
        lastpage = (HtmlPage) button2.click();

        // Check that the ajax request succeeds
        checkTrue("finalSpan1", "Text should stay");
        checkTrue("finalSpan2", "Text should stay");
    }
}
