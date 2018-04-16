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

package com.sun.faces.test.javaee7.protectedview;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import static org.junit.Assert.assertTrue;


public class Bug22995287IT {
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
    public void testPage2CanBeDisplayed1() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlAnchor link = page.getHtmlElementById("get_parameter_fparam");
        page = link.click();
        
        String pageXml = page.getBody().asXml();
        assertTrue(pageXml.contains("foo bar"));
    }
    
    @Test
    public void testPage2CanBeDisplayed2() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlAnchor link = page.getHtmlElementById("get_parameter_outcome");

        page = link.click();
        
        String pageXml = page.getBody().asXml();
        assertTrue(pageXml.contains("foo bar"));
    }

    @Test
    public void testPage2CanBeDisplayed3() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlAnchor link = page.getHtmlElementById("get_parameter_none");

        page = link.click();
        
        String pageXml = page.getBody().asXml();
        assertTrue(pageXml.contains("Welcome to Page2"));
    }
    
    @Test
    public void testPage2CanBeDisplayed4() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button_to_page2");
        page = button.click();
        
        String pageXml = page.getBody().asXml();
        assertTrue(pageXml.contains("Welcome to Page2"));
    }

    @Test
    public void testPage3CanBeDisplayed1() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlAnchor link = page.getHtmlElementById("page3_get_parameter_fparam");
        page = link.click();
        
        String pageXml = page.getBody().asXml();
        assertTrue(pageXml.contains("foo bar"));
    }
    
    @Test
    public void testPage3CanBeDisplayed2() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlAnchor link = page.getHtmlElementById("page3_get_parameter_outcome");

        page = link.click();
        
        String pageXml = page.getBody().asXml();
        assertTrue(pageXml.contains("foo bar"));
    }

    @Test
    public void testPage3CanBeDisplayed3() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlAnchor link = page.getHtmlElementById("page3_get_parameter_none");

        page = link.click();
        
        String pageXml = page.getBody().asXml();
        assertTrue(pageXml.contains("Welcome to Page2"));
    }
    
    @Test
    public void testPage3CanBeDisplayed4() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("page3_button_to_page2");
        page = button.click();
        
        String pageXml = page.getBody().asXml();
        assertTrue(pageXml.contains("Welcome to Page2"));
    }
}
