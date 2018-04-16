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

package com.sun.faces.test.servlet30.dynamic;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Issue2395IT {

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
    public void testAdd() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/issue2395.xhtml");
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("form:add");
        page = button.click();
        assertTrue(page.asXml().indexOf("I was dynamically added") != -1);
    }

    @Test
    public void testAddRemove() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/issue2395.xhtml");
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("form:addRemove");
        page = button.click();
        assertTrue(page.asXml().indexOf("I was dynamically added") == -1);
    }

    @Test
    public void testAddRemoveAdd() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/issue2395.xhtml");
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("form:addRemoveAdd");
        page = button.click();
        assertTrue(page.asXml().indexOf("I was dynamically added") != -1);
    }

    @Test
    public void testRemove() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/issue2395.xhtml");
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("form:remove");
        page = button.click();
        assertTrue(page.asXml().indexOf("Remove Me") == -1);
    }

    @Test
    public void testRemoveAdd() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/issue2395.xhtml");
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("form:removeAdd");
        page = button.click();
        assertTrue(page.asXml().indexOf("Remove Me") != -1);
    }

    @Test
    public void testRemoveAddRemove() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/issue2395.xhtml");
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("form:removeAddRemove");
        page = button.click();
        assertTrue(page.asXml().indexOf("Remove Me") == -1);
    }
}
