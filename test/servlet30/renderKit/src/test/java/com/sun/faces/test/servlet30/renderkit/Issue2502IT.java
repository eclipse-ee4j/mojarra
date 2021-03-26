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

package com.sun.faces.test.servlet30.renderkit; 

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.junit.*;
import static org.junit.Assert.*;

public class Issue2502IT {

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
    public void testEscape() throws Exception {

        String expected1 = "&lt;i&gt;test1&lt;/i&gt;";
        String expected2 = "&lt;i&gt;test2&lt;/i&gt;";
        String expected3 = "&lt;i&gt;test3&lt;/i&gt;";

        /*
         * We don't want this to be simulated as an IE browser since IE
         * does some automatic replacing.
         */
        webClient = new WebClient(BrowserVersion.FIREFOX_45);
        HtmlPage page = webClient.getPage(webUrl+"faces/outputEscape.xhtml");
        assertTrue(page.asXml().contains(expected1));
        assertTrue(page.asXml().contains(expected2));
        assertTrue(page.asXml().contains(expected3));
    }

}
