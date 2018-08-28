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

import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>
 * Test that invalid values don't cause valueChangeEvents to occur.
 * </p>
 */

public class ForEachITCase extends HtmlUnitFacesITCase {

    // ------------------------------------------------------------ Constructors

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public ForEachITCase(String name) {
        super(name);

        // TODO this test would actually work if the client IDs were adapted for execution on tomcat
        addExclusion(HtmlUnitFacesITCase.Container.TOMCAT6, "testForEachIssue714");
        addExclusion(HtmlUnitFacesITCase.Container.TOMCAT7, "testForEachIssue714");
        addExclusion(HtmlUnitFacesITCase.Container.WLS_10_3_4_NO_CLUSTER, "testForEachIssue714");
    }

    // ------------------------------------------------------ Instance Variables

    // ---------------------------------------------------- Overall Test Methods

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(ForEachITCase.class));
    }

    // ------------------------------------------------- Individual Test Methods
    public void testForEach() throws Exception {
//        HtmlPage page = getPage("/faces/forEach01.jsp");
//
//        // Make sure values are displayed properly for the initial request
//        //assert outputText values are as expected
//        assertTrue(-1 != page.asText().indexOf("output1"));
//        assertTrue(-1 != page.asText().indexOf("output2"));
//        assertTrue(-1 != page.asText().indexOf("output3"));
//
//        //assert inputText without "id" values are as expected
//        assertTrue(-1 != page.asText().indexOf("inputText1=input1"));
//        assertTrue(-1 != page.asText().indexOf("inputText2=input2"));
//        assertTrue(-1 != page.asText().indexOf("inputText3=input3"));
//
//        //assert inputText with "id" values are as expected
//        assertTrue(-1 != page.asText().indexOf("inputid1"));
//        assertTrue(-1 != page.asText().indexOf("inputid2"));
//        assertTrue(-1 != page.asText().indexOf("inputid3"));
//
//        // Assign new values to input fields, submit the form.
//        List list;
//        list = getAllElementsOfGivenClass(page, null,
//                HtmlTextInput.class);
//
//        ((HtmlTextInput) list.get(0)).setValueAttribute("newValue1");
//        ((HtmlTextInput) list.get(1)).setValueAttribute("newValue2");
//        ((HtmlTextInput) list.get(2)).setValueAttribute("newValue3");
//
//        ((HtmlTextInput) list.get(3)).setValueAttribute("newValueid1");
//        ((HtmlTextInput) list.get(4)).setValueAttribute("newValueid2");
//        ((HtmlTextInput) list.get(5)).setValueAttribute("newValueid3");
//
//        list = getAllElementsOfGivenClass(page, null,
//                HtmlSubmitInput.class);
//        HtmlSubmitInput button = (HtmlSubmitInput) list.get(0);
//        page = (HtmlPage) button.click();
//
//        // make sure the values are as expected on post back.
//        assertTrue(-1 != page.asText().indexOf("output1"));
//        assertTrue(-1 != page.asText().indexOf("output2"));
//        assertTrue(-1 != page.asText().indexOf("output3"));
//
//        //assert inputText without "id" values are as expected
//        assertTrue(-1 != page.asText().indexOf("inputText1=input1"));
//        assertTrue(-1 != page.asText().indexOf("inputText2=input2"));
//        assertTrue(-1 != page.asText().indexOf("inputText3=input3"));
//
//        assertTrue(-1 != page.asText().indexOf("newValue1"));
//        assertTrue(-1 != page.asText().indexOf("newValue2"));
//        assertTrue(-1 != page.asText().indexOf("newValue3"));
//
//        //assert inputText with "id" values are as expected
//        assertTrue(-1 != page.asText().indexOf("inputid1"));
//        assertTrue(-1 != page.asText().indexOf("inputid2"));
//        assertTrue(-1 != page.asText().indexOf("inputid3"));
//
//        assertTrue(-1 != page.asText().indexOf("newValueid1"));
//        assertTrue(-1 != page.asText().indexOf("newValueid2"));
//        assertTrue(-1 != page.asText().indexOf("newValueid3"));
    }

    public void testForEachIssue714() throws Exception {

        HtmlPage page = getPage("/faces/forEach04.jsp");
        List<HtmlSpan> spans = new ArrayList<HtmlSpan>(2);
        getAllElementsOfGivenClass(page, spans, HtmlSpan.class);
        assertTrue(spans.size() == 2);
        HtmlSpan span = spans.get(0);
        assertTrue(span.getAttribute("id").matches(".*:idfrag1:frag1"));
        span = spans.get(1);
        assertTrue(span.getAttribute("id").matches(".*:idfrag2:frag2"));

        // submit the form to ensure no duplicate ID exceptions are
        // raised during post-back
        List<HtmlSubmitInput> buttons = new ArrayList<HtmlSubmitInput>(1);
        buttons = getAllElementsOfGivenClass(page, buttons, HtmlSubmitInput.class);
        assertTrue(buttons.size() == 1);
        HtmlSubmitInput submit = buttons.get(0);
        page = (HtmlPage) submit.click();

        // validate the IDs are as expected after post-back
        spans = new ArrayList<HtmlSpan>(2);
        getAllElementsOfGivenClass(page, spans, HtmlSpan.class);
        assertTrue(spans.size() == 2);
        span = spans.get(0);
        assertTrue(span.getAttribute("id").matches(".*:idfrag1:frag1"));
        span = spans.get(1);
        assertTrue(span.getAttribute("id").matches(".*:idfrag2:frag2"));

    }

}
