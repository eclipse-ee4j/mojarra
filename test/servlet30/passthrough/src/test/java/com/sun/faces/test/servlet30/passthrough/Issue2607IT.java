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

package com.sun.faces.test.servlet30.passthrough;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlFieldSet;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;
import org.junit.Test;

public class Issue2607IT {
    
    /**
     * Stores the web URL.
     */
    private String webUrl;
    /**
     * Stores the web client.
     */
    private WebClient webClient;

    /**
     * Setup before testing.
     */
    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(60000);
    }

    /**
     * Tear down after testing.
     */
    @After
    public void tearDown() {
        webClient.close();
    }

    // This tests the following markup:
    // <base jsf:id="base1" href="http://foobar.com" target="_blank" />
    //
    @Test
    public void testBase() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/base.xhtml");
        HtmlElement base = page.getHtmlElementById("base1");
        String xml = base.asXml();
        assertTrue(xml.contains("<base"));
        assertTrue(xml.contains("id=\"" + "base1" + "\""));
        assertTrue(xml.contains("href=\"" + "http://foobar.com" + "\""));
        assertTrue(xml.contains("target=\"" + "_blank" + "\""));
    } 

    // This tests the following markup:
    // <command jsf:id="command1" onclick="doThat()"/>
    //
    @Test
    public void testCommand() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/command.xhtml");
        HtmlElement command = page.getHtmlElementById("command1");
        String xml = command.asXml();
        assertTrue(xml.contains("<command"));
        assertTrue(xml.contains("id=\"" + "command1" + "\""));
        assertTrue(xml.contains("onclick=\"" + "doThat()" + "\""));
    } 

    // This tests the following markup:
    // <meta jsf:id="meta1" charset="UTF-8"/>
    //
    @Test
    public void testMeta() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/meta.xhtml");
        HtmlElement meta = page.getHtmlElementById("meta1");
        String xml = meta.asXml();
        assertTrue(xml.contains("<meta"));
        assertTrue(xml.contains("id=\"" + "meta1" + "\""));
        assertTrue(xml.contains("charset=\"" + "UTF-8" + "\""));
    } 

}
