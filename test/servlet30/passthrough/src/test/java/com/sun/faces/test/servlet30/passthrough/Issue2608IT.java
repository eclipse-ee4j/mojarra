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
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;
import org.junit.Test;

public class Issue2608IT {

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
    // <h:inputText id="input1" type="text" p:placeholder="Enter text here" />
    //
    @Test
    public void testInputTextPlaceholder() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/input1.xhtml");
        HtmlElement input = page.getHtmlElementById("input1");
        String xml = input.asXml();
        assertTrue(xml.contains("<input"));
        assertTrue(xml.contains("id=\"" + "input1" + "\""));
        assertTrue(xml.contains("type=\"" + "text" + "\""));
        assertTrue(xml.contains("name=\"" + "input1" + "\""));
        assertTrue(xml.contains("placeholder=\"" + "Enter text here" + "\""));
    }

    // This tests the following markup:
    // <h:inputText id="input2" type="text" p:autocomplete="on" />
    //
    @Test
    public void testInputTextAutocomplete() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/input1.xhtml");
        HtmlElement input = page.getHtmlElementById("input2");
        String xml = input.asXml();
        assertTrue(xml.contains("<input"));
        assertTrue(xml.contains("id=\"" + "input2" + "\""));
        assertTrue(xml.contains("type=\"" + "text" + "\""));
        assertTrue(xml.contains("name=\"" + "input2" + "\""));
        assertTrue(xml.contains("autocomplete=\"" + "on" + "\""));
    }

    // This tests the following markup:
    // <h:inputText id="input3" type="text" p:autofocus="autofocus" />
    //
    @Test
    public void testInputTextAutofocus() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/input1.xhtml");
        HtmlElement input = page.getHtmlElementById("input3");
        String xml = input.asXml();
        assertTrue(xml.contains("<input"));
        assertTrue(xml.contains("id=\"" + "input3" + "\""));
        assertTrue(xml.contains("type=\"" + "text" + "\""));
        assertTrue(xml.contains("name=\"" + "input3" + "\""));
        assertTrue(xml.contains("autofocus=\"" + "autofocus" + "\""));
    }

    // This tests the following markup:
    // <h:inputText id="input4" type="text" p:list="mydatalist" />
    //
    @Test
    public void testInputTextList() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/input1.xhtml");
        HtmlElement input = page.getHtmlElementById("input4");
        String xml = input.asXml();
        assertTrue(xml.contains("<input"));
        assertTrue(xml.contains("id=\"" + "input4" + "\""));
        assertTrue(xml.contains("type=\"" + "text" + "\""));
        assertTrue(xml.contains("name=\"" + "input4" + "\""));
        assertTrue(xml.contains("list=\"" + "mydatalist" + "\""));
    }

    // This tests the following markup:
    // <h:inputText id="input5" type="text" p:pattern="[A-Za-z]{3}" />
    //
    @Test
    public void testInputTextPattern() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/input1.xhtml");
        HtmlElement input = page.getHtmlElementById("input5");
        String xml = input.asXml();
        assertTrue(xml.contains("<input"));
        assertTrue(xml.contains("id=\"" + "input5" + "\""));
        assertTrue(xml.contains("type=\"" + "text" + "\""));
        assertTrue(xml.contains("name=\"" + "input5" + "\""));
        assertTrue(xml.contains("pattern=\"" + "[A-Za-z]{3}" + "\""));
    }

    // This tests the following markup:
    // <h:inputText id="input6" type="text" p:required="required" />
    //
    @Test
    public void testInputTextRequired() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/input1.xhtml");
        HtmlElement input = page.getHtmlElementById("input6");
        String xml = input.asXml();
        assertTrue(xml.contains("<input"));
        assertTrue(xml.contains("id=\"" + "input6" + "\""));
        assertTrue(xml.contains("type=\"" + "text" + "\""));
        assertTrue(xml.contains("name=\"" + "input6" + "\""));
        assertTrue(xml.contains("required=\"" + "required" + "\""));
    }

    // This tests the following markup:
    // <h:inputText id="input7" type="text" p:dirname="input7.dir" />
    //
    @Test
    public void testInputTextDirname() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/input1.xhtml");
        HtmlElement input = page.getHtmlElementById("input7");
        String xml = input.asXml();
        assertTrue(xml.contains("<input"));
        assertTrue(xml.contains("id=\"" + "input7" + "\""));
        assertTrue(xml.contains("type=\"" + "text" + "\""));
        assertTrue(xml.contains("name=\"" + "input7" + "\""));
        assertTrue(xml.contains("dirname=\"" + "input7.dir" + "\""));
    }
}
