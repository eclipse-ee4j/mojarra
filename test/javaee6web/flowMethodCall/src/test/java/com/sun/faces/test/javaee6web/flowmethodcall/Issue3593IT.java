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

package com.sun.faces.test.javaee6web.flowmethodcall;

import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import static org.junit.Assert.assertTrue;

public class Issue3593IT {

    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testMethodCallToMethodCall() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("start_method-call-start-node");
        page = button.click();
        button = (HtmlSubmitInput) page.getElementById("method-call-button-02");
        page = button.click();
        String pageText = page.asText();
        assertTrue(pageText.contains("View node at end of method calls"));
        
    }

    @Test
    public void testMethodCallToFlowCall() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("start_method-call-start-node");
        page = button.click();
        button = (HtmlSubmitInput) page.getElementById("method-call-button-03");
        page = button.click();
        String pageText = page.asText();
        assertTrue(pageText.contains("First page in the flow"));
        
    }    

    @Test
    public void testMethodCallToSwitch() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("start_method-call-start-node");
        page = button.click();
        button = (HtmlSubmitInput) page.getElementById("method-call-button-04");
        page = button.click();
        String pageText = page.asText();
        assertTrue(pageText.contains("View node at end of method calls"));
        
    }    
    
}
