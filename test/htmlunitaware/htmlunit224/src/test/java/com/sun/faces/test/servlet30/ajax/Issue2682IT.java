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

import static java.lang.System.getProperty;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

public class Issue2682IT {

    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = getProperty("integration.url");
        webClient = new WebClient();
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testIssue2682() throws Exception {
        
        // DebugHelper debugHelper = new DebugHelper(webClient, "issue2682.xhtml");
        
        HtmlPage page = webClient.getPage(webUrl + "issue2682.xhtml");
        System.out.println("\n\n\n ****************************" + webClient.getCookieManager().getCookies());
        
        assertTrue(page.asText().contains("Check Box Status: false"));
        
        HtmlCheckBoxInput cbox = (HtmlCheckBoxInput) page.getElementById("form:cbox");
        
        HtmlPage page1 = cbox.click();
        webClient.waitForBackgroundJavaScript(120000);
        
        // debugHelper.print(page1, "After cbox click");
        
        assertTrue(page.asText().contains("Check Box Status: true"));
        
        HtmlSubmitInput input = (HtmlSubmitInput) page1.getHtmlElementById("form:button");
        
        page1 = input.click();
        
        // debugHelper.print(page1, "After button click");
        
        assertTrue(page1.asText().contains("Check Box Status: true"));
        
        cbox = (HtmlCheckBoxInput) page1.getHtmlElementById("form:cbox");
        page1 = cbox.click();
        webClient.waitForBackgroundJavaScript(120000);
        assertTrue(page1.asText().contains("Check Box Status: false"));
        
        input = (HtmlSubmitInput) page1.getHtmlElementById("form:button");
        page1 = input.click();
        webClient.waitForBackgroundJavaScript(120000);
        assertTrue(page1.asText().contains("Check Box Status: false"));
    }
    
   
}
