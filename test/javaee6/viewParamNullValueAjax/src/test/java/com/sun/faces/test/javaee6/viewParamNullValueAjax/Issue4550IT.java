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

package com.sun.faces.test.javaee6.viewParamNullValueAjax;

import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.sun.faces.test.junit.JsfTestRunner;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;

@RunWith(JsfTestRunner.class)
public class Issue4550IT {

    private static String TEST_STRING = "Test Rhuan";

    /**
     * Stores the web URL.
     */
    private String webUrl;
    /**
     * Stores the web client.
     */
    private WebClient webClient;

    /**
     * Setup before testing.
     * 
     * @throws Exception when a serious error occurs.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    /**
     * Cleanup after testing.
     * 
     * @throws Exception when a serious error occurs.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Setup before testing.
     */
    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
    }

    @Test
    public void testViewParamNullValueAjax() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/viewparam-nullvalue-ajax.xhtml");

        // Ajax submit click
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getHtmlElementById("form:ajaxCommandButton");
        assertNotNull(submit);
        page = (HtmlPage) submit.click();
        String pageAsText = page.asText();
        assertTrue(pageAsText.contains(TEST_STRING));

        // Ajax submit click
        submit = (HtmlSubmitInput) page.getHtmlElementById("form:ajaxCommandButton");
        assertNotNull(submit);
        page = (HtmlPage) submit.click();
        pageAsText = page.asText();
        assertTrue(pageAsText.contains(TEST_STRING));

        // Non Ajax submit click
        submit = (HtmlSubmitInput) page.getHtmlElementById("form:commandButton");
        assertNotNull(submit);
        page = (HtmlPage) submit.click();
        pageAsText = page.asText();
        assertTrue(pageAsText.contains(TEST_STRING));
    }

    @After
    public void tearDown() {
        webClient.close();
    }
}
