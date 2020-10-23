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
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Issue2829IT {

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
    public void testValueBindingSet() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/valueBindingSet1.xhtml");
        assertTrue(page.asXml().indexOf("one") != -1);
    }

    @Test
    public void testValueBindingSetNull() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/valueBindingSet2.xhtml");
        assertTrue(page.asXml().indexOf("NULL") != -1);
    }

    @Test
    public void testValueBindingSetMapNotation() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/valueBindingSet3.xhtml");
        assertTrue(page.asXml().indexOf("three") != -1);
    }

    @Test
    public void testValueBindingSetNullMapNotation() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/valueBindingSet4.xhtml");
        assertTrue(page.asXml().indexOf("NULL") != -1);
    }

    @Test
    public void testValueBindingSetDoesNotExist() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/valueBindingSet5.xhtml");
        assertTrue(page.asXml().indexOf("five") != -1);
    }

    @Test
    public void testValueBindingSetSessionVariable() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/valueBindingSet6.xhtml");
        assertTrue(page.asXml().indexOf("six") != -1);
    }
}
