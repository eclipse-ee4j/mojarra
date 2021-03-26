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

public class AjaxTagEventWrappingITCase extends HtmlUnitFacesITCase {

    public AjaxTagEventWrappingITCase(String name) {
        super(name);
    }

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        super.setUp();
    }


    /*
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(AjaxTagEventWrappingITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }


    public void testAjaxTagEventWrapping() throws Exception {
        getPage("/faces/ajax/ajaxTagEventWrapping.xhtml");
        System.out.println("Start ajax tag event wrapping test");

        // Check initial values
        checkTrue("out1","0");
        checkTrue("say","init");
        checkTrue("paramOut","");
        checkTrue("out2","1");

        // Press Count
        HtmlSubmitInput button = (HtmlSubmitInput) lastpage.getHtmlElementById("button1");
        lastpage = (HtmlPage) button.click();

        checkTrue("out1","2");
        checkTrue("out2","1");

        // Press Say
        button = (HtmlSubmitInput) lastpage.getHtmlElementById("button2");
        lastpage = (HtmlPage) button.click();

        checkTrue("say","1");
        checkTrue("out1","2");
        checkTrue("out2","1");

        // Press Count and Say
        button = (HtmlSubmitInput) lastpage.getHtmlElementById("button3");
        lastpage = (HtmlPage) button.click();

        checkTrue("say","2");
        checkTrue("out1","3");
        checkTrue("out2","1");

        // Press Param
        button = (HtmlSubmitInput) lastpage.getHtmlElementById("button4");
        lastpage = (HtmlPage) button.click();

        checkTrue("say","init");
        checkTrue("out1","4");
        checkTrue("out2","5");
        checkTrue("paramOut","testval");

        // Reset Page
        button = (HtmlSubmitInput) lastpage.getHtmlElementById("reset");
        lastpage = (HtmlPage) button.click();
        button = (HtmlSubmitInput) lastpage.getHtmlElementById("reload");
        lastpage = (HtmlPage) button.click();

        // Check initial values
        checkTrue("out1","0");
        checkTrue("say","init");
        checkTrue("paramOut","");
        checkTrue("out2","1");

        // Press Count and Param
        button = (HtmlSubmitInput) lastpage.getHtmlElementById("button5");
        lastpage = (HtmlPage) button.click();

        checkTrue("out1","2");
        checkTrue("say","init");
        checkTrue("paramOut","testval");
        checkTrue("out2","1");

        // Reset Page
        button = (HtmlSubmitInput) lastpage.getHtmlElementById("reset");
        lastpage = (HtmlPage) button.click();
        button = (HtmlSubmitInput) lastpage.getHtmlElementById("reload");
        lastpage = (HtmlPage) button.click();

        // Check initial values
        checkTrue("out1","0");
        checkTrue("say","init");
        checkTrue("paramOut","");
        checkTrue("out2","1");

        // Press Count and Say and Param
        /*  Test is faulty - commenting out - race to see if say is actually set

        button = (HtmlSubmitInput) lastpage.getHtmlElementById("button6");
        lastpage = (HtmlPage) button.click();

        checkTrue("out1","2");
        checkTrue("say","1");
        checkTrue("paramOut","testval");
        checkTrue("out2","1");
        */
        // leaving out button 7

        // Reset Page
        button = (HtmlSubmitInput) lastpage.getHtmlElementById("reset");
        lastpage = (HtmlPage) button.click();
        button = (HtmlSubmitInput) lastpage.getHtmlElementById("reload");
        lastpage = (HtmlPage) button.click();

        // Check initial values
        checkTrue("out1","0");
        checkTrue("say","init");
        checkTrue("paramOut","");
        checkTrue("out2","1");


        // Check ajax checkbox
        HtmlCheckBoxInput checked = ((HtmlCheckBoxInput)lastpage.getHtmlElementById("checkbox1"));
        lastpage = (HtmlPage)checked.click();

        System.out.println(getText("checkedvalue1"));
        checkTrue("checkedvalue1","true");
        checkTrue("out2","1");

        // Check ajax + userwrap checkbox
        checked = ((HtmlCheckBoxInput)lastpage.getHtmlElementById("checkbox2"));
        lastpage = (HtmlPage)checked.click();

        checkTrue("checkedvalue2","true");
        checkTrue("say","1");
        checkTrue("out2","1");

        // Check user onchange checkbox
        checked = ((HtmlCheckBoxInput)lastpage.getHtmlElementById("checkbox3"));
        lastpage = (HtmlPage)checked.click();

        checkTrue("checkedvalue3","false");
        checkTrue("say","2");
        checkTrue("out2","1");

    }

}
