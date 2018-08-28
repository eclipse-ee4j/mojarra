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

package com.sun.faces.test.servlet30.lifeycleCsrf;

import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlHiddenInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class Spec869IT {

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
    public void testSimpleCSRF() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlButtonInput button = (HtmlButtonInput) page.getElementById("button");
        page = button.click();

        String pageText = page.getBody().asText();
        assertTrue(pageText.contains("protected view"));

        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        page = webClient.getPage(webUrl + "faces/i_spec_869_war_protected.xhtml");
        pageText = page.getBody().asText();

        assertTrue(pageText.contains("javax.faces.application.ProtectedViewException"));

    }

    @Test
    public void testSimpleCSRFPostback() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button_postback");
        page = button.click();

        String pageText = page.getBody().asText();
        assertTrue(pageText.contains("protected view"));
    }

    // Tests a request with an invalid referer header request parameter.
    @Test
    public void testBadRefererCSRF() throws Exception {
        webClient.removeRequestHeader("Referer");
        webClient.addRequestHeader("Referer", "foobar");
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl);
        HtmlButtonInput button = (HtmlButtonInput) page.getElementById("button");
        page = button.click();
        String pageText = page.getBody().asText();
        assertTrue(pageText.contains("Referer [sic] header value foobar does not appear to be a protected view"));
    }

    // Tests a request with an invalid origin header request parameter.
    @Test
    public void testBadOriginCSRF() throws Exception {
        webClient.removeRequestHeader("Origin");
        webClient.addRequestHeader("Origin", "foobar");
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl);
        HtmlButtonInput button = (HtmlButtonInput) page.getElementById("button");
        page = button.click();
        String pageText = page.getBody().asText();
        assertTrue(pageText.contains("Origin [sic] header value foobar does not appear to be a protected view"));
    }

    // Tests a request with a valid referer header request parameter.
    // In this case the referer is an unprotected page, but it is the originating
    // page in this webapp (for the protected page).
    @Test
    public void testGoodRefererCSRF() throws Exception {
        webClient.removeRequestHeader("Referer");
        webClient.addRequestHeader("Referer", "i_spec_869_war.xhtml");
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl);
        HtmlButtonInput button = (HtmlButtonInput) page.getElementById("button");
        page = button.click();
        String pageText = page.getBody().asText();
        assertTrue(!pageText.contains("javax.faces.application.ProtectedViewException"));
    }

    // Tests a request with a valid origin header request parameter.
    // In this case the origin is an unprotected page, but it is the originating
    // page in this webapp (for the protected page).
    @Test
    public void testGoodOriginCSRF() throws Exception {
        webClient.removeRequestHeader("Origin");
        webClient.addRequestHeader("Origin", "i_spec_869_war.xhtml");
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl);
        HtmlButtonInput button = (HtmlButtonInput) page.getElementById("button");
        page = button.click();
        String pageText = page.getBody().asText();
        assertTrue(!pageText.contains("javax.faces.application.ProtectedViewException"));
    }

    @Test
    public void testFakeStatelessViewState() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/i_spec_869_war_not_stateless.xhtml");

        HtmlSpan invokeCount = page.getHtmlElementById("invokeCount");
        String invokeCountStrA = invokeCount.asText();

        HtmlHiddenInput stateField = (HtmlHiddenInput) page.getHtmlElementById("j_id1:javax.faces.ViewState:0");
        stateField.setValueAttribute("stateless");
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button_postback");
        page = button.click();
        assertEquals(500, page.getWebResponse().getStatusCode());

        // Verify the button action was not invoked.
        page = webClient.getPage(webUrl + "faces/i_spec_869_war_not_stateless.xhtml");
        invokeCount = page.getHtmlElementById("invokeCount");
        String invokeCountStrB = invokeCount.asText();
        assertEquals(invokeCountStrA, invokeCountStrB);

        page = webClient.getPage(webUrl + "faces/i_spec_869_war_not_stateless.xhtml");
        button = (HtmlSubmitInput) page.getElementById("button_postback");
        page = button.click();
        assertEquals(200, page.getWebResponse().getStatusCode());

        invokeCount = page.getHtmlElementById("invokeCount");
        String invokeCountStrC = invokeCount.asText();
        int b = Integer.parseInt(invokeCountStrB);
        int c = Integer.parseInt(invokeCountStrC);
        assertTrue(b < c);

    }

}
