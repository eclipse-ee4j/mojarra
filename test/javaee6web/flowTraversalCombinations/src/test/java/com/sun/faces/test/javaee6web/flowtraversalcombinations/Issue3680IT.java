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

public class Issue3680IT {
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
    public void testNestFlowCallNotShareFlowScopedBeans() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("go_to_nested_flow_call_f1");
        page = button.click();
        assertTrue(page.asXml().contains("The count value in the original stack frame should always be 0, even after the flow has been called recursively."));
        assertTrue(page.asXml().contains("FlowDeque size: 1"));
        
        button = (HtmlSubmitInput) page.getHtmlElementById("returnToOutsideOfAnyFlow");
        page = button.click();
        assertTrue(page.asXml().contains("Has a flow: false."));
        assertTrue(page.asXml().contains("flowScope value, should be empty: ."));
    } 
}
