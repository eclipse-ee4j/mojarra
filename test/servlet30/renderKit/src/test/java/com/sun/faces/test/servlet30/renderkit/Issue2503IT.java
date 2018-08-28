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

import static com.gargoylesoftware.htmlunit.BrowserVersion.FIREFOX_45;
import static java.lang.System.getProperty;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Issue2503IT {

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
        webUrl = getProperty("integration.url");
        webClient = new WebClient();
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    // ------------------------------------------------------------ Test Methods

    @Test
    public void testEscape() throws Exception {

        String expected1 = "@import url(\"resources/import1.css\");";
        String expected2 = "Tom &amp; Jerry";

        /*
         * We don't want this to be simulated as an IE browser since IE does some automatic replacing.
         */
        webClient = new WebClient(FIREFOX_45);
        HtmlPage page = webClient.getPage(webUrl + "faces/outputEscape1.xhtml");
        assertTrue(page.asXml().contains(expected1));
        assertTrue(page.asXml().contains(expected2));
    }

}
