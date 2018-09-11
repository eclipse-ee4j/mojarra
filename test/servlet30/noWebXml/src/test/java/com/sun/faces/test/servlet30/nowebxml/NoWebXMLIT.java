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

package com.sun.faces.test.servlet30.nowebxml;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

public class NoWebXMLIT {

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
    public void testNoWebXML() throws Exception {

        HtmlPage page = webClient.getPage(webUrl + "hello.xhtml");

        HtmlSubmitInput button = page.getHtmlElementById("form:command");
        page = (HtmlPage) button.click();
        String pageAsText = page.asText();
        assertTrue(pageAsText.contains("Good Morning"));
        String pageText = page.asXml();
        assertTrue(pageText.contains("<input type=\"hidden\" name=\"javax.faces.ViewState\" id="));

        page = webClient.getPage(webUrl + "hello.jsf");

        button = page.getHtmlElementById("form:command");
        page = (HtmlPage) button.click();
        pageAsText = page.asText();
        assertTrue(pageAsText.contains("Good Morning"));
        pageText = page.asXml();
        assertTrue(pageText.contains("<input type=\"hidden\" name=\"javax.faces.ViewState\" id="));

        page = webClient.getPage(webUrl + "hello.faces");

        button = page.getHtmlElementById("form:command");
        page = (HtmlPage) button.click();
        pageAsText = page.asText();
        assertTrue(pageAsText.contains("Good Morning"));
        pageText = page.asXml();
        assertTrue(pageText.contains("<input type=\"hidden\" name=\"javax.faces.ViewState\" id="));
    }
}
