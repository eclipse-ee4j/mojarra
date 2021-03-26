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

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;

import junit.framework.Test;
import junit.framework.TestSuite;


public class AjaxMessageITCase extends HtmlUnitFacesITCase {

    public AjaxMessageITCase(String name) {
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
        return new TestSuite(AjaxMessageITCase.class);
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }


    public void testCommandButtonButton() throws Exception {
        getPage("/faces/ajax/ajaxMessage.xhtml");


        // Check that ids were rendered
        try {
            lastpage.getHtmlElementById("testform1:msgs");
        } catch (Exception e) {
            fail("testform1:msgs not rendered");
        }
        try {
            lastpage.getHtmlElementById("testform1a:msgs");
        } catch (Exception e) {
            fail("testform1a:msgs not rendered");
        }
        try {
            lastpage.getHtmlElementById("testform2:msg");
        } catch (Exception e) {
            fail("testform2:msg not rendered");
        }

        // check that other ids weren't

        try {
            lastpage.getHtmlElementById("testform3:msgs");
            fail("testform3:msgs rendered - not correct");
        } catch (Exception e) {
            //  Success
        }
        try {
            lastpage.getHtmlElementById("testform3a:msgs");
            fail("testform3:msgs rendered - not correct");
        } catch (Exception e) {
            //  Success
        }
        try {
            lastpage.getHtmlElementById("testform4:msg");
            fail("testform4:msg rendered - not correct");
        } catch (Exception e) {
            //  Success
        }

        // Check initial state
        checkTrue("testform1:in1","0");
        checkTrue("testform1a:in1","0");
        checkTrue("testform2:in1","0");
        checkTrue("testform3:in1","0");
        checkTrue("testform3a:in1","0");
        checkTrue("testform4:in1","0");


        HtmlTextInput in1 = (HtmlTextInput) lastpage.getHtmlElementById("testform1:in1");
        in1.setText("1");

        // Submit the ajax request
        HtmlSubmitInput button2 = (HtmlSubmitInput) lastpage.getHtmlElementById("testform1:button2");
        lastpage = (HtmlPage) button2.click();
        getClient().waitForBackgroundJavaScript(60000);

        // Check that the ajax request succeeds
        checkTrue("testform1:in1","1");

        // And that others weren't effected
        checkTrue("testform1a:in1","0");
        checkTrue("testform2:in1","0");
        checkTrue("testform3:in1","0");
        checkTrue("testform3a:in1","0");
        checkTrue("testform4:in1","0");


        in1 = (HtmlTextInput) lastpage.getHtmlElementById("testform1:in1");
        in1.setText("1a");

        // Submit the ajax request
        button2 = (HtmlSubmitInput) lastpage.getHtmlElementById("testform1:button2");
        lastpage = (HtmlPage) button2.click();
        getClient().waitForBackgroundJavaScript(60000);

        HtmlUnorderedList ul = (HtmlUnorderedList) lastpage.getHtmlElementById("testform1:msgs");
        DomNode node = ul.getFirstChild();
        System.out.println(node.asText());
        assertTrue("not equal to: testform1:in1: '1a' must be a number consisting of one or more digits. ",
                node.asText().trim().equals("testform1:in1: '1a' must be a number consisting of one or more digits."));


        checkTrue("testform1a:in1","0");
        checkTrue("testform2:in1","0");
        checkTrue("testform3:in1","0");
        checkTrue("testform3a:in1","0");
        checkTrue("testform4:in1","0");
        
    }
}
