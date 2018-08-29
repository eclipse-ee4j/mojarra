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

package com.sun.faces.test.servlet30.facelets;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

public class ForEachIT {

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
    public void test_false_false_false_false_false_false_false_false_false() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/false_false_false_false_false_false_false_false_false.xhtml");

        assertTrue(page.asXml().matches("(?s).*<form.*>.*<input.+type=\"text\".+value=\"Bobby\".+disabled=\"disabled\".*>.*<input.+id=\"literalId\".+type=\"text\".+name=\"literalId\".+value=\"Bobby\".+disabled=\"disabled\".*/>.*<input.+type=\"text\".+value=\"Jerry\".+disabled=\"disabled\".*/>.*<input.+type=\"text\".+value=\"Jerry\".+disabled=\"disabled\".*/>.*<input.+type=\"text\".+value=\"Phil\".+disabled=\"disabled\".*/>.*<input.+type=\"text\".+value=\"Phil\".+disabled=\"disabled\".*/>.*</form>.*"));

        HtmlSubmitInput button = page.getHtmlElementById("submit");
        page = button.click();

        assertTrue(page.asXml().matches("(?s).*<form.*id=\"form01\".*name=\"form01\".*>.*<input.+type=\"text\".+name=\"j_idt[0-9]+\".+value=\"Bobby\".+disabled=\"disabled\".*/>.*<input.+id=\"literalId\".+type=\"text\".+name=\"literalId\".+value=\"Bobby\".+disabled=\"disabled\".*/>.*<input.+type=\"text\".+name=\"j_idt[0-9]+\".+value=\"Jerry\".+disabled=\"disabled\".*/>.*<input.+type=\"text\".+name=\"j_idt[0-9]+\".+value=\"Jerry\".+disabled=\"disabled\".*/>.*<input.+type=\"text\".+name=\"j_idt[0-9]+\".+value=\"Phil\".+disabled=\"disabled\".*/>.*<input.+type=\"text\".+name=\"j_idt[0-9]+\".+value=\"Phil\".+disabled=\"disabled\".*/>.*<input.+type=\"text\".+name=\"j_idt[0-9]+\".+value=\"Bobby\".+disabled=\"disabled\".*/>.*<input.+type=\"text\".+name=\"j_idt[0-9]+\".+value=\"Bobby\".+disabled=\"disabled\".*/>.*<input.+type=\"text\".+name=\"j_idt[0-9]+\".+value=\"Jerry\".+disabled=\"disabled\".*/>.*<input.+type=\"text\".+name=\"j_idt[0-9]+\".+value=\"Jerry\".+disabled=\"disabled\".*/>.*<input.+type=\"text\".+name=\"j_idt[0-9]+\".+value=\"Phil\".+disabled=\"disabled\".*/>.*<input.+type=\"text\".+name=\"j_idt[0-9]+\".+value=\"Phil\".+disabled=\"disabled\".*/>.*</form>.*"));
    }

    @Test
    public void test_false_false_false_false_false_false_false_false_true() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/false_false_false_false_false_false_false_false_true.xhtml");
    }


    @Test
    public void test_false_false_false_false_false_false_false_true_false() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/false_false_false_false_false_false_false_true_false.xhtml");
    }


    @Test
    public void test_false_false_false_false_false_false_false_true_true() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/false_false_false_false_false_false_false_true_true.xhtml");


    }


    @Test
    public void test_false_false_false_false_false_false_true_false_false() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/false_false_false_false_false_false_true_false_false.xhtml");


    }


    @Test
    public void test_false_false_false_false_false_false_true_false_true() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/false_false_false_false_false_false_true_false_true.xhtml");


    }


    @Test
    public void test_false_false_false_false_false_false_true_true_false() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/false_false_false_false_false_false_true_true_false.xhtml");
    }


    @Test
    public void test_false_false_false_false_false_false_true_true_true() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/true_true_true_true_true_true_true_true_true.xhtml");
    }


    @Test
    public void test_false_false_false_false_false_true_false_false_false() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/false_false_false_false_false_true_false_false_false.xhtml");
    }


    @Test
    public void test_false_false_false_false_false_true_false_false_true() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/false_false_false_false_false_true_false_false_true.xhtml");
    }


    @Test
    public void test_false_false_false_false_false_true_false_true_false() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/false_false_false_false_false_true_false_true_false.xhtml");
    }


    @Test
    public void test_false_false_false_false_false_true_false_true_true() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/false_false_false_false_false_true_false_true_true.xhtml");
    }


    @Test
    public void test_false_false_false_false_false_true_true_false_false() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/false_false_false_false_false_true_true_false_false.xhtml");
    }


    @Test
    public void test_false_false_false_false_false_true_true_false_true() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/false_false_false_false_false_true_true_false_true.xhtml");
    }


    @Test
    public void test_false_false_false_false_false_true_true_true_false() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/false_false_false_false_false_true_true_true_false.xhtml");
    }


    @Test
    public void test_false_false_false_false_false_true_true_true_true() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/false_false_false_false_false_true_true_true_true.xhtml");
    }


    @Test
    public void test_false_false_false_false_true_true_true_true_false() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/false_false_false_false_true_true_true_true_false.xhtml");
    }


    @Test
    public void test_false_false_false_true_false_true_false_true_true() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/false_false_false_true_false_true_false_true_true.xhtml");
    }


    @Test
    public void test_false_false_true_false_false_false_true_true_false() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/false_false_true_false_false_false_true_true_false.xhtml");
    }


    @Test
    public void test_false_true_false_false_false_true_false_true_false() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/false_true_false_false_false_true_false_true_false.xhtml");
    }


    @Test
    public void test_true_false_false_false_false_false_false_false_false() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/true_false_false_false_false_false_false_false_false.xhtml");
    }


    @Test
    public void test_true_true_true_true_true_true_true_true_true() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/forEach/index.xhtml");

        page = webClient.getPage(webUrl + "faces/forEach/true_true_true_true_true_true_true_true_true.xhtml");
    }

}
