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

import static org.junit.Assert.assertTrue;

import org.junit.*;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

public class Issue3833IT {

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
    public void testUIRepeatStateSaving() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/issue3833.xhtml");

        HtmlSelect select1 = (HtmlSelect) page.getElementById("form:repeat:0:list");
        HtmlSelect select2 = (HtmlSelect) page.getElementById("form:repeat:1:list");

        page = select1.getOption(0).click();
        webClient.waitForBackgroundJavaScript(60000);
        assertTrue(page.getElementById("form:message").asText().equals("null -> 1"));

        page = select2.getOption(0).click();
        webClient.waitForBackgroundJavaScript(60000);
        assertTrue(page.getElementById("form:message").asText().equals("null -> 3"));

        page = select1.getOption(1).click();
        webClient.waitForBackgroundJavaScript(60000);
        assertTrue(page.getElementById("form:message").asText().equals("1 -> 2"));

        page = select2.getOption(1).click();
        webClient.waitForBackgroundJavaScript(60000);
        assertTrue(page.getElementById("form:message").asText().equals("3 -> 4"));
    }
}
