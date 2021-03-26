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


import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlBody;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.faces.component.NamingContainer;
import javax.faces.component.UIViewRoot;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;


/**
 * <p>Test Case for JSP Interoperability.</p>
 */

public class MethodRefITCase extends HtmlUnitFacesITCase {


    // ------------------------------------------------------------ Constructors


    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public MethodRefITCase(String name) {
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
        return (new TestSuite(MethodRefITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }


    // ------------------------------------------------- Individual Test Methods
    
    public void testActionAndActionListener() throws Exception {
        HtmlForm form;
        HtmlSubmitInput submit;
        HtmlAnchor link;
        HtmlTextInput input;
        HtmlPage page;

        page = getPage("/faces/methodref01.jsp");
        form = getFormById(page, "form");
        submit = (HtmlSubmitInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "button1");

        // press button1
        page = (HtmlPage) submit.click();
        form = getFormById(page, "form");
        input = (HtmlTextInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "buttonStatus");
        assertTrue("Input does not have expected value",
                   -1 != input.asText().indexOf("button1 was pressed"));
        link = (HtmlAnchor) page.getAnchors().get(0);

        // press button2
        page = (HtmlPage) link.click(); 
        assertNotNull(page);
        form = getFormById(page, "form");
        input = (HtmlTextInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "buttonStatus");
        assertTrue("Input does not have expected value",
                   -1 != input.asText().indexOf("button2 was pressed"));
        submit = (HtmlSubmitInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "button3"); 

        // press button3
        page = (HtmlPage) submit.click();
        form = getFormById(page, "form");
        input = (HtmlTextInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "buttonStatus");
        assertTrue("Input does not have expected value",
                   -1 != input.asText().indexOf("button3 was pressed"));


    } 


    public void testValidatorReference() throws Exception {
        HtmlForm form;
        HtmlSubmitInput submit;
        HtmlAnchor link;
        HtmlTextInput input;
        HtmlPage page;

        page = getPage("/faces/methodref01.jsp");
        form = getFormById(page, "form");
        submit = (HtmlSubmitInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "validate");

        // press the button with no value, see that no value appears in
        // the "toValidate" textField.
        page = (HtmlPage) submit.click();
        form = getFormById(page, "form");
        input = (HtmlTextInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "toValidate");
        int fieldLen = input.asText().length();
        assertTrue("Input does not have expected value", 0 == fieldLen);

        // fill in an incorrect value, see that still no value appears
        // in the text field.
        input = (HtmlTextInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "toValidate");
        input.setValueAttribute("aoeuaoeu");
        submit = (HtmlSubmitInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "validate");
        page = (HtmlPage) submit.click();
        form = getFormById(page, "form");
        input = (HtmlTextInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "toValidate");
        fieldLen = input.asText().length();
        assertTrue("Input does not have expected value", 8 == fieldLen);

        // fill in the correct value, see that finally we have a value
        input = (HtmlTextInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "toValidate");
        input.setValueAttribute("batman");
        submit = (HtmlSubmitInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "validate");
        page = (HtmlPage) submit.click();
        form = getFormById(page, "form");
        input = (HtmlTextInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "toValidate");
        assertEquals("Input does not have expected value",
                     "batman", input.asText());
    }


    public void testValueChangeListenerByReference() throws Exception {
        HtmlForm form;
        HtmlSubmitInput submit;
        HtmlAnchor link;
        HtmlTextInput input;
        HtmlPage page;

        page = getPage("/faces/methodref01.jsp");
        form = getFormById(page, "form");
        submit = (HtmlSubmitInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "changeValue");

        // fill in a value, see we have a value
        input = (HtmlTextInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "toChange");
        input.setValueAttribute("batman");
        page = (HtmlPage) submit.click();
        form = getFormById(page, "form");
        input = (HtmlTextInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "toChange");
        assertEquals("Input does not have expected value",
                     "batman", input.getOnBlurAttribute());
    }
    
    /**
     * Test case for bug 5030555
     */
    public void testValueChangeListenerWithBinding() throws Exception {
        HtmlForm form;
        HtmlSubmitInput submit;
        HtmlAnchor link;
        HtmlTextInput input;
        HtmlPage page;

        page = getPage("/faces/binding01.jsp");
        form = getFormById(page, "form");
        submit = (HtmlSubmitInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "changeValue");

        // fill in a value, see we have a value
        input = (HtmlTextInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "toChange");
        input.setValueAttribute("binding works!!");
        page = (HtmlPage) submit.click();
        form = getFormById(page, "form");
        input = (HtmlTextInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "toChange");
        assertEquals("Input does not have expected value",
                     "binding works!!", input.getOnBlurAttribute());
    }
}
