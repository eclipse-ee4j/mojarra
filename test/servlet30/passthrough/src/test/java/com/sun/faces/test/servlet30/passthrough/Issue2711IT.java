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

public class Issue2711IT {
    
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
        HtmlElement input1 = page.getHtmlElementById("input1");
        String xml = input1.asXml();
        assertTrue(xml.contains("<input"));
        assertTrue(xml.contains("id=\"" + "input1" + "\""));
        assertTrue(xml.contains("type=\"" + "text" + "\""));
        assertTrue(xml.contains("name=\"" + "input1" + "\""));
        assertTrue(xml.contains("placeholder=\"" + "Enter text here" + "\""));
    } 

}
