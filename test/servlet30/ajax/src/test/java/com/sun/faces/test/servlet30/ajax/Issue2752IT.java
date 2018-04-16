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

package com.sun.faces.test.servlet30.ajax; 

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

import org.junit.*;
import static org.junit.Assert.*;

public class Issue2752IT {

    /**
     * Stores the web URL.
     */
    private String webUrl;
    /**
     * Stores the web client.
     */
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(60000);
    }

    @After
    public void tearDown() {
        webClient.close();
    }


    // ------------------------------------------------------------ Test Methods

    /**
     * This test verifies that the page contains updated information from an
     * Ajax response.  The response is updated in <code>InsertBean</code>.
     */
    @Test
    public void testInsertFromResponse() throws Exception {
        HtmlPage page = webClient.getPage(webUrl+"faces/insertElement.xhtml");
        HtmlSubmitInput beforeButton = page.getHtmlElementById("beforeButton");
        page = beforeButton.click();
        // This will ensure JavaScript finishes before evaluating the page.
        webClient.waitForBackgroundJavaScript(60000);
        assertTrue(page.asText().contains("This is before textalpha"));
        HtmlSubmitInput afterButton = page.getHtmlElementById("afterButton");
        page = afterButton.click();
        // This will ensure JavaScript finishes before evaluating the page.
        webClient.waitForBackgroundJavaScript(60000);
        assertTrue(page.asText().contains("This is before textalphaThis is after text"));
    }

}
