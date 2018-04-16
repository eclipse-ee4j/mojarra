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

package com.sun.faces.test.javaee6web.flowtraversalcombinations;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class MethodCallNaviToOtherNodesIT {
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
    public void testMethodCallNaviToMethodCall() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("go_to_start_from_method_call_flow");
        page = button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("navigate_to_method_call_node");
        page = button.click();
        assertTrue(page.asXml().contains("Great! You are now in the correct destination view."));
    }   
    
    @Test
    public void testMethodCallNaviToView() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("go_to_start_from_method_call_flow");
        page = button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("navigate_to_view_node");
        page = button.click();
        assertTrue(page.asXml().contains("Great! You are now in the correct destination view."));
    }  
    
    @Test
    public void testMethodCallNaviToSwitch() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("go_to_start_from_method_call_flow");
        page = button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("navigate_to_switch");
        page = button.click();
        assertTrue(page.asXml().contains("Great! You are now in the correct destination view."));
    } 
    
    @Test
    public void testMethodCallNaviToReturn() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("go_to_start_from_method_call_flow");
        page = button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("navigate_to_return");
        page = button.click();
        
        assertTrue(page.asXml().contains("Has a flow: false."));
        assertTrue(page.asXml().contains("flowScope value, should be empty: ."));
    } 

    @Test
    public void testMethodCallNaviToFlowCall() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("go_to_start_from_method_call_flow");
        page = button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("navigate_to_flow_call");
        page = button.click();
        assertTrue(page.asXml().contains("Great! You are now in the correct destination view."));
    }
}
