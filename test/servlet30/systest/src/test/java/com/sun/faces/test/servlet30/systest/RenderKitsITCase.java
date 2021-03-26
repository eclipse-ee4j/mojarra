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
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlHiddenInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.faces.component.NamingContainer;

/**
 * <p>Test Case for Multiple RenderKits.</p>
 */

public class RenderKitsITCase extends HtmlUnitFacesITCase {


    // ------------------------------------------------------------ Constructors


    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public RenderKitsITCase(String name) {
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
        return (new TestSuite(RenderKitsITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }


    // ------------------------------------------------- Individual Test Methods

    public void testRenderKits() throws Exception {
//        HtmlPage page = getPage("/faces/renderkit04.jsp");
//        assertTrue(-1 != page.asText().indexOf("HTML_BASIC"));
//            assertTrue(-1 != page.asText().indexOf("com.sun.faces.renderkit.html_basic.HtmlResponseWriter"));
//
//        HtmlForm form = getFormById(page, "form");
//        assertNotNull("form exists", form);
//        HtmlSubmitInput submit = (HtmlSubmitInput)
//            form.getInputByName("form" +
//                                NamingContainer.SEPARATOR_CHAR +
//                                "submit");
//        try {
//            page = (HtmlPage) submit.click();
//            assertTrue(-1 != page.asText().indexOf("CUSTOM"));
//            assertTrue(-1 != page.asText().indexOf("com.sun.faces.systest.render.CustomResponseWriter"));
//	} catch (Exception e) {
//	    e.printStackTrace();
//	    assertTrue(false);
//        }
//
//        form = getFormById(page, "form");
//        assertNotNull("form exists", form);
//        submit = (HtmlSubmitInput)
//            form.getInputByName("form" +
//                                NamingContainer.SEPARATOR_CHAR +
//                                "submit");
//        try {
//            page = (HtmlPage) submit.click();
//            assertTrue(-1 != page.asText().indexOf("HTML_BASIC"));
//            assertTrue(-1 != page.asText().indexOf("com.sun.faces.renderkit.html_basic.HtmlResponseWriter"));
//	} catch (Exception e) {
//	    e.printStackTrace();
//	    assertTrue(false);
//        }
    }

    // Assert with no renderKitId specfied on the view, and no defaultRenderKitId, 
    // HTML_BASIC is set.  Also assert that the hidden field was not written; 
    public void testNoRenderKitId() throws Exception {
//        HtmlPage page = getPage("/faces/renderkit06.jsp");
//        HtmlForm form = getFormById(page, "form");
//        assertNotNull("form exists", form);
//        HtmlHiddenInput hidden = null;
//        boolean exceptionThrown = false;
//        try {
//            hidden = (HtmlHiddenInput)form.getInputByName("jakarta.faces.RenderKitId");
//        } catch (ElementNotFoundException e) {
//            exceptionThrown = true;
//        }
//        assertTrue(exceptionThrown);
//        assertTrue(-1 != page.asText().indexOf("HTML_BASIC"));
//        assertTrue(-1 != page.asText().indexOf("com.sun.faces.renderkit.html_basic.HtmlResponseWriter"));
    }

    // Test when default-render-kit-id is set for application;
    public void testDefaultRenderKitId() throws Exception {
//        // sets default-render-kit-id in application
//        HtmlPage page = getPageSticky("/faces/renderkit-default.jsp");
//
//        // Load a page with renderKitId="CUSTOM"; 
//        // Assert hidden field is not written because renderKitId == defaultRenderKitId;
//        page = getPageSticky("/faces/renderkit06.jsp");
//        HtmlForm form = getFormById(page, "form");
//        assertNotNull("form exists", form);
//        HtmlHiddenInput hidden = null;
//        boolean exceptionThrown = false;
//        try {
//            hidden = (HtmlHiddenInput)form.getInputByName("jakarta.faces.RenderKitId");
//        } catch (ElementNotFoundException e) {
//            exceptionThrown = true;
//        }
//        assertTrue(-1 != page.asText().indexOf("CUSTOM"));
//        assertTrue(-1 != page.asText().indexOf("com.sun.faces.systest.render.CustomResponseWriter"));
//
//        // Load a page with renderKitId="HTML_BASIC";
//        // Assert hidden field is written because renderKitId != defaultRenderKitId;
//        page = getPageSticky("/faces/renderkit04.jsp");
//        form = getFormById(page, "form");
//        assertNotNull("form exists", form);
//        hidden = (HtmlHiddenInput)form.getInputByName("jakarta.faces.RenderKitId");
//        assertNotNull("hidden exists", hidden);
//        assertTrue(-1 != page.asText().indexOf("HTML_BASIC"));
//        assertTrue(-1 != page.asText().indexOf("com.sun.faces.renderkit.html_basic.HtmlResponseWriter"));
//        page = getPageSticky("/faces/renderkit-default-clear.jsp");
    }
}
