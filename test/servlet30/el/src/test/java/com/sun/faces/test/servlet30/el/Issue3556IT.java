/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates.
 * Copyright (c) 2018 Payara Services Limited.
 * All rights reserved.
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

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Issue3556IT {

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
    public void testResource1en() throws Exception {
        webClient.addRequestHeader("Accept-Language", "en");
        HtmlPage page = webClient.getPage(webUrl + "faces/resource1.xhtml");
        HtmlImage image = page.getHtmlElementById("image");
        assertTrue(image.getSrcAttribute().contains("javax.faces.resource/resource1.gif"));
        assertTrue(image.getSrcAttribute().contains("?loc=en"));
    }

    @Test
    public void testResource1de() throws Exception {
        webClient.addRequestHeader("Accept-Language", "de");
        HtmlPage page = webClient.getPage(webUrl + "faces/resource1.xhtml");
        HtmlImage image = page.getHtmlElementById("image");
        assertTrue(image.getSrcAttribute().contains("javax.faces.resource/resource1.gif"));
        assertTrue(image.getSrcAttribute().contains("?loc=de"));
    }

    @Test
    public void testResource1fr() throws Exception {
        webClient.addRequestHeader("Accept-Language", "fr");
        HtmlPage page = webClient.getPage(webUrl + "faces/resource1.xhtml");
        HtmlImage image = page.getHtmlElementById("image");
        assertTrue(image.getSrcAttribute().contains("javax.faces.resource/resource1.gif"));
        assertTrue(image.getSrcAttribute().contains("?loc=fr"));
    }

    @Test
    public void testResource1ja() throws Exception {
        webClient.addRequestHeader("Accept-Language", "ja");
        HtmlPage page = webClient.getPage(webUrl + "faces/resource1.xhtml");
        HtmlImage image = page.getHtmlElementById("image");
        assertTrue(image.getSrcAttribute().contains("javax.faces.resource/resource1.gif"));
        assertTrue(image.getSrcAttribute().contains("?loc=en"));
    }
    @Test
    public void testResource2en() throws Exception {
        webClient.addRequestHeader("Accept-Language", "en");
        HtmlPage page = webClient.getPage(webUrl + "faces/resource2.xhtml");
        HtmlImage image = page.getHtmlElementById("image");
        assertTrue(image.getSrcAttribute().contains("javax.faces.resource/resource2.gif"));
        assertTrue(image.getSrcAttribute().contains("?ln=resource2&loc=en"));
    }

    @Test
    public void testResource2de() throws Exception {
        webClient.addRequestHeader("Accept-Language", "de");
        HtmlPage page = webClient.getPage(webUrl + "faces/resource2.xhtml");
        HtmlImage image = page.getHtmlElementById("image");
        assertTrue(image.getSrcAttribute().contains("javax.faces.resource/resource2.gif"));
        assertTrue(image.getSrcAttribute().contains("?ln=resource2&loc=de"));
    }

    @Test
    public void testResource2fr() throws Exception {
        webClient.addRequestHeader("Accept-Language", "fr");
        HtmlPage page = webClient.getPage(webUrl + "faces/resource2.xhtml");
        HtmlImage image = page.getHtmlElementById("image");
        assertTrue(image.getSrcAttribute().contains("javax.faces.resource/resource2.gif"));
        assertTrue(image.getSrcAttribute().contains("?ln=resource2&loc=fr"));
    }

    @Test
    public void testResource2ja() throws Exception {
        webClient.addRequestHeader("Accept-Language", "ja");
        HtmlPage page = webClient.getPage(webUrl + "faces/resource2.xhtml");
        HtmlImage image = page.getHtmlElementById("image");
        assertTrue(image.getSrcAttribute().contains("javax.faces.resource/resource2.gif"));
        assertTrue(image.getSrcAttribute().contains("?ln=resource2&loc=en"));
    }

    @Test
    public void testResource3en() throws Exception {
        webClient.addRequestHeader("Accept-Language", "en");
        HtmlPage page = webClient.getPage(webUrl + "faces/resource3.xhtml");
        HtmlImage image = page.getHtmlElementById("image");
        assertTrue(image.getSrcAttribute().contains("javax.faces.resource/resource3.gif"));
    }

    @Test
    public void testResource3de() throws Exception {
        webClient.addRequestHeader("Accept-Language", "de");
        HtmlPage page = webClient.getPage(webUrl + "faces/resource3.xhtml");
        HtmlImage image = page.getHtmlElementById("image");
        assertTrue(image.getSrcAttribute().contains("javax.faces.resource/resource3.gif"));
    }

    @Test
    public void testResource3fr() throws Exception {
        webClient.addRequestHeader("Accept-Language", "fr");
        HtmlPage page = webClient.getPage(webUrl + "faces/resource3.xhtml");
        HtmlImage image = page.getHtmlElementById("image");
        assertTrue(image.getSrcAttribute().contains("javax.faces.resource/resource3.gif"));
    }

    @Test
    public void testResource3ja() throws Exception {
        webClient.addRequestHeader("Accept-Language", "ja");
        HtmlPage page = webClient.getPage(webUrl + "faces/resource3.xhtml");
        HtmlImage image = page.getHtmlElementById("image");
        assertTrue(image.getSrcAttribute().contains("javax.faces.resource/resource3.gif"));
    }

    @Test
    public void testResource4en() throws Exception {
        webClient.addRequestHeader("Accept-Language", "en");
        HtmlPage page = webClient.getPage(webUrl + "faces/resource4.xhtml");
        HtmlImage image = page.getHtmlElementById("image");
        assertTrue(image.getSrcAttribute().contains("javax.faces.resource/resource4.gif"));
        assertTrue(image.getSrcAttribute().contains("ln=resource4"));
    }

    @Test
    public void testResource4de() throws Exception {
        webClient.addRequestHeader("Accept-Language", "de");
        HtmlPage page = webClient.getPage(webUrl + "faces/resource4.xhtml");
        HtmlImage image = page.getHtmlElementById("image");
        assertTrue(image.getSrcAttribute().contains("javax.faces.resource/resource4.gif"));
        assertTrue(image.getSrcAttribute().contains("ln=resource4"));
    }

    @Test
    public void testResource4fr() throws Exception {
        webClient.addRequestHeader("Accept-Language", "fr");
        HtmlPage page = webClient.getPage(webUrl + "faces/resource4.xhtml");
        HtmlImage image = page.getHtmlElementById("image");
        assertTrue(image.getSrcAttribute().contains("javax.faces.resource/resource4.gif"));
        assertTrue(image.getSrcAttribute().contains("ln=resource4"));
    }

    @Test
    public void testResource4ja() throws Exception {
        webClient.addRequestHeader("Accept-Language", "ja");
        HtmlPage page = webClient.getPage(webUrl + "faces/resource4.xhtml");
        HtmlImage image = page.getHtmlElementById("image");
        assertTrue(image.getSrcAttribute().contains("javax.faces.resource/resource4.gif"));
        assertTrue(image.getSrcAttribute().contains("ln=resource4"));
    }
}
