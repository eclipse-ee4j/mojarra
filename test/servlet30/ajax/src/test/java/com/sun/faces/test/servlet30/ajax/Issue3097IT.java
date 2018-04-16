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
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Issue3097IT {

    private String webUrl;
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

    /*
     * This test is purposely only triggering when server side state saving 
     * as there is no way to force a view to expire when client side state 
     * saving.
     */
    @Test
    public void testViewExpired1() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/viewExpired1.xhtml");

        if (page.asXml().indexOf("State Saving Method: server") != -1) {
            HtmlElement expireButton = page.getHtmlElementById("form:expireSessionSoon");
            expireButton.click();
            webClient.waitForBackgroundJavaScript(60000);
            Thread.sleep(25000);
            HtmlElement submitButton = page.getHtmlElementById("form:submit");
            page = submitButton.click();
            webClient.waitForBackgroundJavaScript(60000);
            String text = page.asXml();
            assertTrue(text.indexOf("class javax.faces.application.ViewExpiredException") != -1);
        }
    }
}
