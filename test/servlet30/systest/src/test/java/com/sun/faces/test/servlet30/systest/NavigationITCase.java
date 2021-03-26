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

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlBody;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.faces.component.NamingContainer;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * <p>Test Case for JSP Interoperability.</p>
 */

public class NavigationITCase extends HtmlUnitFacesITCase {


    // ------------------------------------------------------------ Constructors


    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public NavigationITCase(String name) {
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
        return (new TestSuite(NavigationITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }


    // ------------------------------------------------- Individual Test Methods


    // Test dynamically adding and removing components
    public void testRedirect() throws Exception {
        client.getOptions().setRedirectEnabled(false);
        
        // the navigation-case for this url is set up to cause a redirect
        HtmlPage page = getPage("/faces/redirect.jsp");
        HtmlForm form = getFormById(page, "redirect");
        assertNotNull("form exists", form);
        HtmlSubmitInput submit = (HtmlSubmitInput)
            form.getInputByName("redirect" +
                                NamingContainer.SEPARATOR_CHAR +
                                "submit");
        boolean exceptionThrown = false;
        try {
            page = (HtmlPage) submit.click();
        } catch (FailingHttpStatusCodeException fhsce) {
            assertEquals("Didn't get expected redirect",
                         fhsce.getStatusCode(), 302);
            exceptionThrown = true;
        }
        assertTrue("Didn't get expected redirect", exceptionThrown);
    }


    public void testNavigateWithVerbatim() throws Exception {
        HtmlForm form;
        HtmlSubmitInput submit;
        HtmlPage page, page1;

        page = getPage("/faces/jsp/verbatim-test.jsp");
        form = getFormById(page, "form1");
        assertNotNull("form exists", form);
        submit = (HtmlSubmitInput)
            form.getInputByName("form1" + NamingContainer.SEPARATOR_CHAR +
                                "submit");

        // press the button
	try {
            page1 = (HtmlPage) submit.click();
            assertTrue(-1 != page1.asText().indexOf("Thank you"));
	} catch (Exception e) {
	    e.printStackTrace();
	    assertTrue(false);
	}
    }

    public void testNavigateWithVerbatim_One() throws Exception {
        HtmlForm form;
        HtmlSubmitInput submit;
        HtmlPage page, page1;
                                                                                
        page = getPage("/faces/jsp/verbatim-one-test.jsp");
        form = getFormById(page, "form");
        assertNotNull("form exists", form);
        submit = (HtmlSubmitInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "submit");
                                                                                
        // press the link, return to the same page, and check that
        // output text (header) is still present...
 
        try {
            page1 = (HtmlPage) submit.click();
            assertTrue(-1 != page1.asText().indexOf("this is the header"));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }

    public void testNavigateWithVerbatim_Two() throws Exception {
        HtmlForm form;
        HtmlSubmitInput submit;
        HtmlPage page, page1;
                                                                               
                                                                               
        page = getPage("/faces/jsp/verbatim-two-test.jsp");
        form = getFormById(page, "form");
        assertNotNull("form exists", form);
        submit = (HtmlSubmitInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "submit");
                                                                               
                                                                               
        // submit the form, return to the same page, and check that
        // output text (header) is still present...
        // and verbatim text is still present...
                                                                               
        try {
            page1 = (HtmlPage) submit.click();
            assertTrue(-1 != page1.asText().indexOf("verbatim one text here"));
            assertTrue(-1 != page1.asText().indexOf("this is the header"));
            assertTrue(-1 != page1.asText().indexOf("verbatim two text here"));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
    }
    
    public void testNavigateWithEnum() throws Exception {
        HtmlForm form;
        HtmlSubmitInput submit;
        HtmlPage page, page1;
                                                                               
                                                                               
        page = getPage("/faces/enum01.jsp");
        form = getFormById(page, "form");
        assertNotNull("form exists", form);
        submit = (HtmlSubmitInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "go");
                                                                               
        // submit the form, go to next page, check that the text exists
                                                                               
        try {
            page1 = (HtmlPage) submit.click();
            assertTrue(-1 != page1.asText().indexOf("/hello.jsp PASSED"));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
        
        page = getPage("/faces/enum01.jsp");
        form = getFormById(page, "form");
        assertNotNull("form exists", form);
        submit = (HtmlSubmitInput)
            form.getInputByName("form" + NamingContainer.SEPARATOR_CHAR +
                                "stay");
                                                                               
        // submit the form, stay on same page, check that the text does not exist
                                                                               
        try {
            page1 = (HtmlPage) submit.click();
            assertTrue(-1 == page1.asText().indexOf("/hello.jsp PASSED"));
            assertTrue(-1 != page1.asText().indexOf("stay here"));
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
        
    }


    public void testNullOutcomeNoMessage() throws Exception {
        HtmlPage page = getPage("/faces/standard/selectmany05.xhtml");

        HtmlInput input = getInputContainingGivenId(page, "command");
        page = (HtmlPage) input.click();

        // if there is no outcome, no message should be displayed to the user
        assertTrue(!page.asText().contains("javax_faces_developmentstage_messages"));
    }

}
