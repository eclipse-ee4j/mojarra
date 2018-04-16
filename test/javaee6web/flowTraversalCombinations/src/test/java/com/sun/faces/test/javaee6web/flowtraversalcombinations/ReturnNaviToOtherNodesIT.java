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
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class ReturnNaviToOtherNodesIT {
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
    public void testReturnNaviToMethodCall() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("go_to_start_from_return_flow");
        page = button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("go_to_initial_return_node_flow");
        page = button.click();
        HtmlTextInput returnNode = (HtmlTextInput)page.getHtmlElementById("parent-node-to-be-returned");
        returnNode.setValueAttribute("MethodCallNodeToBeCalled");
        button = (HtmlSubmitInput) page.getHtmlElementById("return-to-method-call-node");
        page = button.click();
        assertTrue(page.asXml().contains("Great! You are now in the correct destination view."));
    }    

    @Test
    public void testReturnNaviToViewNode() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("go_to_start_from_return_flow");
        page = button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("go_to_initial_return_node_flow");
        page = button.click();
        HtmlTextInput returnNode = (HtmlTextInput)page.getHtmlElementById("parent-node-to-be-returned");
        returnNode.setValueAttribute("ViewNodeToBeCalled");
        button = (HtmlSubmitInput) page.getHtmlElementById("return-to-method-call-node");
        page = button.click();
        assertTrue(page.asXml().contains("Great! You are now in the correct destination view."));
    } 
    
    @Test
    public void testReturnNaviToSwitchNode() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("go_to_start_from_return_flow");
        page = button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("go_to_initial_return_node_flow");
        page = button.click();
        HtmlTextInput returnNode = (HtmlTextInput)page.getHtmlElementById("parent-node-to-be-returned");
        returnNode.setValueAttribute("SwitchNodeToBeCalled");
        button = (HtmlSubmitInput) page.getHtmlElementById("return-to-method-call-node");
        page = button.click();
        assertTrue(page.asXml().contains("Great! You are now in the correct destination view."));
    } 
    
    @Test
    public void testReturnNaviToReturnNode() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("go_to_start_from_return_flow");
        page = button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("go_to_initial_return_node_flow");
        page = button.click();
        HtmlTextInput returnNode = (HtmlTextInput)page.getHtmlElementById("parent-node-to-be-returned");
        returnNode.setValueAttribute("ReturnNodeToBeCalled");
        button = (HtmlSubmitInput) page.getHtmlElementById("return-to-method-call-node");
        page = button.click();
        
        assertTrue(page.asXml().contains("Has a flow: false."));
        assertTrue(page.asXml().contains("flowScope value, should be empty: ."));
    } 
    
    @Test
    public void testReturnNaviToFlowCallNode() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("go_to_start_from_return_flow");
        page = button.click();
        button = (HtmlSubmitInput) page.getHtmlElementById("go_to_initial_return_node_flow");
        page = button.click();
        HtmlTextInput returnNode = (HtmlTextInput)page.getHtmlElementById("parent-node-to-be-returned");
        returnNode.setValueAttribute("FlowCallNodeToBeCalled");
        button = (HtmlSubmitInput) page.getHtmlElementById("return-to-method-call-node");
        page = button.click();
        assertTrue(page.asXml().contains("Great! You are now in the correct destination view."));
    }
}
