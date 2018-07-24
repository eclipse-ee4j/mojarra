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
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;

import org.junit.*;
import static org.junit.Assert.*;

public class Issue2479IT {

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
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(60000);
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    // ------------------------------------------------------------ Test Methods

    /**
     * This test verifies that an attribute nameed 'value' can be successfully updated from a partial
     * response (over Ajax).
     */
    @Test
    public void testSelectDataTable() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/selectOneMenuDataTable.xhtml");
        HtmlSpan span1 = (HtmlSpan) page.getElementById("table:0:inCity");
        assertTrue((span1.asText()).equals("alpha"));
        HtmlSpan span2 = (HtmlSpan) page.getElementById("table:1:inCity");
        assertTrue((span2.asText()).equals("alpha"));
        HtmlSpan span3 = (HtmlSpan) page.getElementById("table:2:inCity");
        assertTrue((span3.asText()).equals("alpha"));
        HtmlSelect select = (HtmlSelect) page.getElementById("selectMenu");
        page = (HtmlPage) select.setSelectedAttribute("beta", true);
        webClient.waitForBackgroundJavaScript(60000);
        span1 = (HtmlSpan) page.getElementById("table:0:inCity");
        assertTrue((span1.asText()).equals("beta"));
        span2 = (HtmlSpan) page.getElementById("table:1:inCity");
        assertTrue((span2.asText()).equals("beta"));
        span3 = (HtmlSpan) page.getElementById("table:2:inCity");
        assertTrue((span3.asText()).equals("beta"));
    }
}
