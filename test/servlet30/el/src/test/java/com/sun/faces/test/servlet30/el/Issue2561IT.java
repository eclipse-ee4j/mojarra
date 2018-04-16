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

package com.sun.faces.test.servlet30.el;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Issue2561IT {

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

    @Test
    public void testConstructor() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/viewConstructor.xhtml");
        assertTrue(page.asText().indexOf("This is constructed") != -1);
    }

    @Test
    public void testPostConstruct() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/viewPostconstruct.xhtml");
        assertTrue(page.asText().indexOf("This is from the @PostConstruct") != -1);
    }

    @Test
    public void testNavigate() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/viewNavigate.xhtml");
        assertTrue(page.asText().indexOf("This is from the @PostConstruct") != -1);
        HtmlElement anchor = page.getHtmlElementById("form:submit");
        page = anchor.click();
        assertTrue(page.asText().indexOf("This is from the @PostConstruct") != -1);
    }

    @Test
    public void testNavigateAway() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/viewNavigateAway.xhtml");
        assertTrue(page.asText().indexOf("This is from the @PostConstruct") != -1);
        HtmlElement anchor = page.getHtmlElementById("form:submit");
        page = anchor.click();
        assertTrue(page.asText().indexOf("true") != -1);
    }

    @Test
    public void testInvalidatedSession() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/viewInvalidatedSession.xhtml");
        assertTrue(page.asText().indexOf("This is from the @PostConstruct") != -1);
        webClient.getPage(webUrl + "faces/viewInvalidatedPerform.xhtml");
        page = webClient.getPage(webUrl + "faces/viewInvalidatedVerify.xhtml");
        assertTrue(page.asText().indexOf("true") != -1);
    }
}
