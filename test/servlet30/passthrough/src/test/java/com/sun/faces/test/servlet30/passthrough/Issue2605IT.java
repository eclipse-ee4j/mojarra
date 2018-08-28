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
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.*;
import static org.junit.Assert.*;

public class Issue2605IT {

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
    public void testFieldset() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/fieldset.xhtml");
        HtmlElement elem = page.getHtmlElementById("fieldset1");
        String xml = elem.asXml();
        assertTrue(xml.contains("<fieldset"));
        assertTrue(xml.contains("id=\"" + "fieldset1" + "\""));

        elem = page.getHtmlElementById("fieldset2");
        xml = elem.asXml();
        assertTrue(xml.contains("<fieldset"));
        assertTrue(xml.contains("id=\"" + "fieldset2" + "\""));
        assertTrue(xml.contains("disabled=\"" + "disabled" + "\""));

        elem = page.getHtmlElementById("fieldset3");
        xml = elem.asXml();
        assertTrue(xml.contains("<fieldset"));
        assertTrue(xml.contains("id=\"" + "fieldset3" + "\""));
        assertTrue(xml.contains("form=\"" + "form" + "\""));
        assertTrue(xml.contains("name=\"" + "myfieldset" + "\""));
    }

    @Test
    public void testMeter() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/meter.xhtml");
        HtmlElement elem = page.getHtmlElementById("meter1");
        String xml = elem.asXml();
        assertTrue(xml.contains("<meter"));
        assertTrue(xml.contains("id=\"" + "meter1" + "\""));
        assertTrue(xml.contains("min=\"" + "200" + "\""));
        assertTrue(xml.contains("max=\"" + "500" + "\""));
        assertTrue(xml.contains("value=\"" + "350" + "\""));

        elem = page.getHtmlElementById("meter2");
        xml = elem.asXml();
        assertTrue(xml.contains("<meter"));
        assertTrue(xml.contains("id=\"" + "meter2" + "\""));
        assertTrue(xml.contains("min=\"" + "100" + "\""));
        assertTrue(xml.contains("max=\"" + "500" + "\""));
        assertTrue(xml.contains("value=\"" + "350" + "\""));
    }

    @Test
    public void testLabel() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/label.xhtml");
        HtmlElement elem = page.getHtmlElementById("label1");
        String xml = elem.asXml();
        assertTrue(xml.contains("<label"));
        assertTrue(xml.contains("id=\"" + "label1" + "\""));
        assertTrue(xml.contains("form=\"" + "form" + "\""));
        assertTrue(xml.contains("for=\"" + "input1" + "\""));
    }

    @Test
    public void testDataList() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/datalist.xhtml");
        HtmlElement elem = page.getHtmlElementById("colors");
        String xml = elem.asXml();
        assertTrue(xml.contains("<datalist"));
        assertTrue(xml.contains("id=\"" + "colors" + "\""));
        WebResponse resp = page.getWebResponse();
        String text = resp.getContentAsString();
        assertTrue(text.contains("<option id=" + '"' + "r" + '"' + " value=" + '"' + "red" + '"'));
        assertTrue(text.contains("<option id=" + '"' + "b" + '"' + " value=" + '"' + "blue" + '"'));
        assertTrue(text.contains("<option id=" + '"' + "g" + '"' + " value=" + '"' + "green" + '"'));
    }

    @Test
    public void testOutput() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/output.xhtml");
        HtmlElement elem = page.getHtmlElementById("output1");
        String xml = elem.asXml();
        assertTrue(xml.contains("<output"));
        assertTrue(xml.contains("id=\"" + "output1" + "\""));
    }

    @Test
    public void testKeygen() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/keygen.xhtml");
        HtmlElement elem = page.getHtmlElementById("keygen1");
        String xml = elem.asXml();
        assertTrue(xml.contains("<keygen"));
        assertTrue(xml.contains("id=\"" + "keygen1" + "\""));
    }
}
