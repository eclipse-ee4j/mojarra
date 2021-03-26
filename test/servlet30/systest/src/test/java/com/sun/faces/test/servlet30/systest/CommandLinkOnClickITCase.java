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

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.html.*;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>Test Case for Multiple RenderKits.</p>
 */

public class CommandLinkOnClickITCase extends HtmlUnitFacesITCase {

    // ------------------------------------------------------------ Constructors


    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public CommandLinkOnClickITCase(String name) {
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
        return (new TestSuite(CommandLinkOnClickITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }

    // ------------------------------------------------- Individual Test Methods

    // This method tests that a user provided commandLink "onclick" javascript
    // method will get executed in addition to the internal one rendered
    // as part of CommandLinkRenderer.

    public void testOnClickReturnTrue() throws Exception {
//        HtmlPage page = getPage("/faces/jsp/commandLinkOnClickTrue.jsp");
//
//        HtmlForm form = getFormById(page, "form");
//        assertNotNull("form exists", form);
//        HtmlHiddenInput hidden = null;
//        try {
//            hidden = (HtmlHiddenInput) form.getInputByName("form:j_idcl");
//        } catch (ElementNotFoundException e) {
//            assertTrue(false);
//        }
//        // This initial value was set by an "onLoad" javascript function in the jsp.
//        assertTrue(hidden.getValueAttribute().equals("Goodbye"));
//
//        // click the link..
//        HtmlAnchor submit = (HtmlAnchor)
//                page.getFirstAnchorByText("submit");
//        assertTrue(submit.getOnClickAttribute().equals("var a=function(){setValue('form');};var b=function(){clearFormHiddenParams_form('form');document.forms['form']['form:j_idcl'].value='form:submit'; document.forms['form'].submit(); return false;};return (a()==false) ? false : b();"));
//        try {
//            page = (HtmlPage) submit.click();
//        } catch (Exception e) {
//            e.printStackTrace();
//            assertTrue(false);
//        }
//        // The value of this field was set by the user provided "onclick" javascript
//        // function.
//        HtmlTextInput input = (HtmlTextInput) form.getInputByName("form:init");
//        assertTrue(input.getValueAttribute().equals("Hello"));
//
//        // The value of this field was changed by the internal Faces javascript function
//        // created by CommandLinkRenderer..
//        try {
//            hidden = (HtmlHiddenInput) form.getInputByName("form:j_idcl");
//        } catch (ElementNotFoundException e) {
//            assertTrue(false);
//        }
//        assertTrue(hidden.getValueAttribute().equals("form:submit"));
    }

    // This method tests that a user provided commandLink "onclick" javascript
    // method will get executed.  The user provided function returns "false",
    // so, the internal Faces function should not execute.
    public void testOnClickReturnFalse() throws Exception {
//        HtmlPage page = getPage("/faces/jsp/commandLinkOnClickFalse.jsp");
//
//        HtmlForm form = getFormById(page, "form");
//        assertNotNull("form exists", form);
//        HtmlHiddenInput hidden = null;
//        try {
//            hidden = (HtmlHiddenInput) form.getInputByName("form:j_idcl");
//        } catch (ElementNotFoundException e) {
//            assertTrue(false);
//        }
//        // This initial value was set by an "onLoad" javascript function in the jsp.
//        assertTrue(hidden.getValueAttribute().equals("Goodbye"));
//
//        // click the link..
//        HtmlAnchor submit = (HtmlAnchor)
//                page.getFirstAnchorByText("submit");
//        assertTrue(submit.getOnClickAttribute().equals("var a=function(){setValue('form'); return false;};var b=function(){clearFormHiddenParams_form('form');document.forms['form']['form:j_idcl'].value='form:submit'; document.forms['form'].submit(); return false;};return (a()==false) ? false : b();"));
//        try {
//            page = (HtmlPage) submit.click();
//        } catch (Exception e) {
//            e.printStackTrace();
//            assertTrue(false);
//        }
//        HtmlTextInput input = (HtmlTextInput) form.getInputByName("form:init");
//        assertTrue(input.getValueAttribute().equals("Hello"));
//
//        // The value of this field remains unchanged from the initial value. 
//        try {
//            hidden = (HtmlHiddenInput) form.getInputByName("form:j_idcl");
//        } catch (ElementNotFoundException e) {
//            assertTrue(false);
//        }
//        assertTrue(hidden.getValueAttribute().equals("Goodbye"));
    }

}
