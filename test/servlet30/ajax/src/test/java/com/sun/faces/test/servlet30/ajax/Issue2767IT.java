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

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;

import org.junit.*;
import static org.junit.Assert.*;

public class Issue2767IT {

    /**
     * Stores the web URL.
     */
    private String webUrl;
    /**
     * Stores the web client.
     */
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

    // ------------------------------------------------------------ Test Methods

    @Test
    public void testCommandLinkRadio() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/issue2767.xhtml");
        HtmlAnchor anchor = (HtmlAnchor) page.getElementById("testLink");
        // This will ensure JavaScript finishes before evaluating the page.
        webClient.waitForBackgroundJavaScript(60000);
        anchor.click();
        webClient.waitForBackgroundJavaScript(60000);
        assertTrue(page.asXml().contains("LINK ACTION"));
        HtmlRadioButtonInput radio1 = (HtmlRadioButtonInput) page.getElementById("testRadio:0");
        HtmlRadioButtonInput radio2 = (HtmlRadioButtonInput) page.getElementById("testRadio:1");
        HtmlRadioButtonInput radio3 = (HtmlRadioButtonInput) page.getElementById("testRadio:2");

        page = radio1.click();
        webClient.waitForBackgroundJavaScript(60000);
        assertTrue(page.asXml().contains("RADIO:red"));

        page = radio2.click();
        webClient.waitForBackgroundJavaScript(60000);
        assertTrue(page.asXml().contains("RADIO:blue"));

        page = radio3.click();
        webClient.waitForBackgroundJavaScript(60000);
        assertTrue(page.asXml().contains("RADIO:white"));
    }
}
