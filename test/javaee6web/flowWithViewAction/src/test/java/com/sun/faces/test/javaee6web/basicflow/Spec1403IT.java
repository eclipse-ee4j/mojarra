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

package com.sun.faces.test.javaee6web.basicflow;

import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import static org.junit.Assert.assertTrue;

public class Spec1403IT {
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

    /**
     * Tear down after testing.
     */
    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testFlowEntryExit() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl);

        assertTrue(page.getBody().asText().contains("First page in the flow."));
        assertTrue(page.getBody().asText().contains("flowA"));

        HtmlButtonInput button = (HtmlButtonInput) page.getElementById("flowA-next");
        page = button.click();
        
        String pageText = page.getBody().asText();
        assertTrue(!pageText.contains("flowA"));
        assertTrue(pageText.contains("Gracefully handle ContextNotActive"));
        
    }

    @Test
    public void testNestedFlowEntryExit() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl);

        assertTrue(page.getBody().asText().contains("First page in the flow."));
        assertTrue(page.getBody().asText().contains("flowA"));

        HtmlButtonInput button = (HtmlButtonInput) page.getElementById("flowB");
        page = button.click();

        assertTrue(page.getBody().asText().contains("called from flowA"));
        assertTrue(page.getBody().asText().contains("flowB"));

        button = (HtmlButtonInput) page.getElementById("flowB-next");
        page = button.click();

        assertTrue(page.getBody().asText().contains("First page in the flow."));
        assertTrue(page.getBody().asText().contains("flowA"));
        
        button = (HtmlButtonInput) page.getElementById("flowA-next");
        page = button.click();

        String pageText = page.getBody().asText();
        assertTrue(!pageText.contains("flowA"));
        assertTrue(pageText.contains("Gracefully handle ContextNotActive"));
        
    }
    
    
}
