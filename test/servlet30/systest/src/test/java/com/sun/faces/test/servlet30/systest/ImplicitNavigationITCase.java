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
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

public class ImplicitNavigationITCase extends HtmlUnitFacesITCase {

    public ImplicitNavigationITCase(String name) {
        super(name);
    }

    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(ImplicitNavigationITCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // ------------------------------------------------------------ Test Methods
    public void testImplicitNavigation() throws Exception {

        HtmlPage page = getPage("/faces/implicitnav/page01.xhtml");

        // Assert that the navCase getters work correctly
        String text = page.asText();
        assertTrue(-1 != text.indexOf("/jsf-systest/faces/implicitnav/page01.xhtml"));
        assertTrue(-1 != text.indexOf("/jsf-systest/implicitnav/page01.xhtml"));
        // Make sure we're on the expected page
        assertTrue(-1 != text.indexOf("[page01]"));

        // click the page02 button
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("page02");
        page = button.click();
        text = page.asText();
        // Make sure we're on the expected page
        assertTrue(-1 != text.indexOf("[page02]"));

        // click the page01 button to go back to page01
        button = (HtmlSubmitInput) page.getElementById("page01");
        page = button.click();
        text = page.asText();
        // Make sure we're on the expected page
        assertTrue(-1 != text.indexOf("[page01]"));

        // go back again to page02
        button = (HtmlSubmitInput) page.getElementById("page02");
        page = button.click();
        text = page.asText();
        // Make sure we're on the expected page
        assertTrue(-1 != text.indexOf("[page02]"));

        // go forward to page03
        button = (HtmlSubmitInput) page.getElementById("page03");
        page = button.click();
        text = page.asText();
        // Make sure we're on the expected page
        assertTrue(-1 != text.indexOf("[page03]"));

        // click the page02 button to go back to page02
        button = (HtmlSubmitInput) page.getElementById("page02");
        page = button.click();
        text = page.asText();
        // Make sure we're on the expected page
        assertTrue(-1 != text.indexOf("[page02]"));

        // go forward to page03
        button = (HtmlSubmitInput) page.getElementById("page03");
        page = button.click();
        text = page.asText();
        // Make sure we're on the expected page
        assertTrue(-1 != text.indexOf("[page03]"));

        // go forward to page04. This uses a redirect
        button = (HtmlSubmitInput) page.getElementById("page04");
        client.getOptions().setRedirectEnabled(false);
        boolean exceptionThrown = false;
        try {
            page = button.click();
        } catch (FailingHttpStatusCodeException fhsce) {
            exceptionThrown = true;
            assertEquals(302, fhsce.getStatusCode());
        }
        assertTrue(exceptionThrown);

        client.getOptions().setRedirectEnabled(true);
        page = button.click();
        text = page.asText();
        // Make sure we're on the expected page
        assertTrue(-1 != text.indexOf("This is the last page."));
    }

    public void testImplicitNavigationWithredirect() throws Exception {

        HtmlPage page = getPage("/faces/implicitnav/implicitNavRedirect.xhtml");
        String text;
        final String SEARCH_TEXT = "SEARCH_TEXT";

        // case 1, h:button, make sure the input value is lost.
        HtmlTextInput input = (HtmlTextInput) page.getElementById("input");
        input.setValueAttribute(SEARCH_TEXT);
        HtmlButtonInput buttonButton = (HtmlButtonInput) page.getElementById("httpGet");

        page = buttonButton.click();
        text = page.asText();
        ;
        assertTrue(!text.contains(SEARCH_TEXT));

        // case 2 h:commandButton that does redirect. Make sure a redirect is
        // performed and the value is lost.
        client.getOptions().setRedirectEnabled(false);
        boolean exceptionThrown = false;
        page = getPage("/faces/implicitnav/implicitNavRedirect.xhtml");
        input = (HtmlTextInput) page.getElementById("input");
        input.setValueAttribute(SEARCH_TEXT);
        HtmlSubmitInput submitButton = (HtmlSubmitInput) page.getElementById("httpPostRedirect");
        try {
            page = submitButton.click();
        } catch (FailingHttpStatusCodeException e) {
            assertEquals(302, e.getStatusCode());
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
        client.getOptions().setRedirectEnabled(true);
        page = submitButton.click();
        text = page.asText();
        ;
        assertTrue(!text.contains(SEARCH_TEXT));

        // case 3 h:commandButton with empty query string
        page = getPage("/faces/implicitnav/implicitNavRedirect.xhtml");
        input = (HtmlTextInput) page.getElementById("input");
        input.setValueAttribute(SEARCH_TEXT);
        submitButton = (HtmlSubmitInput) page.getElementById("httpPostInvalidQueryString");
        page = submitButton.click();
        text = page.asText();
        assertTrue(text.contains(SEARCH_TEXT));
        if (text.contains("Project Stage: Development")) {
            assertTrue(text.contains("Invalid query string in outcome \'implicitNavRedirect02?\'"));
        }
        // case 4: h:commandButton, regular post.
        page = getPage("/faces/implicitnav/implicitNavRedirect.xhtml");
        input = (HtmlTextInput) page.getElementById("input");
        input.setValueAttribute(SEARCH_TEXT);
        submitButton = (HtmlSubmitInput) page.getElementById("httpPost");
        page = submitButton.click();
        text = page.asText();
        assertTrue(text.contains(SEARCH_TEXT));
    }

    public void testImplicitNavEmptyString() throws Exception {
        HtmlPage page = getPage("/faces/implicitnav/implicitNavEmptyString.xhtml");
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();
        assertTrue(page.asText().contains("Implicit Navigation with empty string action"));
    }
}
