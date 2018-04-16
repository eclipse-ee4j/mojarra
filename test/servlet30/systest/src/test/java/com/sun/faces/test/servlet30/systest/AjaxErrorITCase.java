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

import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.List;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

public class AjaxErrorITCase extends HtmlUnitFacesITCase {

    public AjaxErrorITCase(String name) {
        super(name);
    }

    /*
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        super.setUp();
    }


    /*
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(AjaxErrorITCase.class));
    }


    /*
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }

    // ------------------------------------------------------------ Test Methods
    public void testAjaxError() throws Exception {
        List<String> collectedAlerts = new ArrayList<String>(1);
        client.setAlertHandler(new CollectingAlertHandler(collectedAlerts));

        HtmlPage page = getPage("/faces/ajax/ajaxError2.xhtml");

        if (page.asXml().contains("Project Stage: Development")) {
            HtmlSubmitInput button = (HtmlSubmitInput) getInputContainingGivenId(page, "form:error");
            assertNotNull(button);

            button.click();

            assertEquals(1, collectedAlerts.size());
            assertEquals("serverError: errorName Error Message", collectedAlerts.get(0));
        }
    }

    public void testAjaxServerError() throws Exception {
        List<String> collectedAlerts = new ArrayList<String>(1);
        client.setAlertHandler(new CollectingAlertHandler(collectedAlerts));

        HtmlPage page = getPage("/faces/ajax/ajaxError.xhtml");

        if (page.asXml().contains("Project Stage: Development")) {
            HtmlSubmitInput button = (HtmlSubmitInput) getInputContainingGivenId(page, "form:eval");
            assertNotNull(button);

            button.click();

            assertEquals(1, collectedAlerts.size());
            String serverError = "serverError:\\sclass javax.faces.el.MethodNotFoundException /ajax/ajaxError.xhtml.*action=.*evalBean.error.*: Method not fou.*";
            String collectedAlertsStr = collectedAlerts.get(0).substring(0, 130);
            assertTrue(collectedAlertsStr.matches(serverError));

            page = getPage("/faces/ajax/ajaxError3.xhtml");
            button = (HtmlSubmitInput) getInputContainingGivenId(page, "form:error");
            assertNotNull(button);

            HtmlPage page1 = button.click();
            HtmlElement element = (HtmlElement) page1.getElementById("statusArea");
            assertNotNull(element);
            String statusText = element.getAttribute("value");
            assertTrue(statusText.equals("Name: form:error Error: serverError "));
        }
    }

    public void testAjaxMalformedXMLError() throws Exception {
        List<String> collectedAlerts = new ArrayList<String>(1);
        client.setAlertHandler(new CollectingAlertHandler(collectedAlerts));

        HtmlPage page = getPage("/faces/ajax/ajaxMalformedXML.xhtml");

        if (page.asXml().contains("Project Stage: Development")) {
            HtmlSubmitInput button = (HtmlSubmitInput) getInputContainingGivenId(page, "form:error");
            assertNotNull(button);

            button.click();

            assertEquals(1, collectedAlerts.size());
            assertEquals("malformedXML: During update: doesntExist not found", collectedAlerts.get(0));
        }
    }
}
