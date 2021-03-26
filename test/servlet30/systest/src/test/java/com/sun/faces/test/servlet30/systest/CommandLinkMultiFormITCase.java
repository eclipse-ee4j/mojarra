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

import java.util.List;


/**
 * <p>Test Case for JSP Interoperability.</p>
 */

public class CommandLinkMultiFormITCase extends HtmlUnitFacesITCase {

    // ------------------------------------------------------------ Constructors


    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public CommandLinkMultiFormITCase(String name) {
        super(name);
        addExclusion(HtmlUnitFacesITCase.Container.TOMCAT6, "testMultiForm");
        addExclusion(HtmlUnitFacesITCase.Container.TOMCAT7, "testMultiForm");
        addExclusion(HtmlUnitFacesITCase.Container.WLS_10_3_4_NO_CLUSTER, "testMultiForm");
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
     * @return Tests included in suite
     */
    public static Test suite() {
        return (new TestSuite(CommandLinkMultiFormITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }

    // ------------------------------------------------- Individual Test Methods


    public void testMultiForm() throws Exception {
//        HtmlForm form1, form2;
//        HtmlAnchor link1, link2, link3;
//        HtmlTextInput input;
//        HtmlPage page, page1;
//        HtmlHiddenInput hidden1, hidden2;
//
//        page = getPage("/faces/taglib/commandLink_multiform_test.jsp");
//        // press all command links..
//        List forms = page.getForms();
//        form1 = (HtmlForm) forms.get(0);
//        form2 = (HtmlForm) forms.get(1);
//
//        // links within the first form
//        hidden1 = (HtmlHiddenInput) form1.getInputByName("form01:j_idcl");
//        assertNotNull(hidden1);
//        //hidden1.setValueAttribute("form01:Link1");
//        //page1 = (HtmlPage) form1.submit();
//        page1 = page.getAnchorByName("form01:Link1").click();
//        assertTrue(-1 != page1.asText().indexOf("Thank you"));
//        //hidden1.setValueAttribute("form01:Link2");
//        //page1 = (HtmlPage) form1.submit();
//        page1 = page.getAnchorByName("form01:Link2").click();
//        assertTrue(-1 != page1.asText().indexOf("Thank you"));
//
//        // links within second form
//        hidden2 = (HtmlHiddenInput) form2.getInputByName("form02:j_idcl");
//        assertNotNull(hidden2);
//        //hidden2.setValueAttribute("form02:Link3");
//        //page1 = (HtmlPage) form1.submit();
//        page1 = page.getAnchorByName("form02:Link3").click();
//        assertTrue(-1 != page1.asText().indexOf("Thank you"));
//        //hidden2.setValueAttribute("form02:Link4");
//        //page1 = (HtmlPage) form1.submit();
//        page1 = page.getAnchorByName("form02:Link4").click();
//        assertTrue(-1 != page1.asText().indexOf("Thank you"));
    }
}
