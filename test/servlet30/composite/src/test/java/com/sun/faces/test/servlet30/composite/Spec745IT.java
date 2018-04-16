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

package com.sun.faces.test.servlet30.composite;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class Spec745IT {

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
    public void testType1() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/attribute/attributeType.xhtml?test=untypedXunset");
        assertTrue(page.asXml().contains("type of @untypedXunset: Object"));
        HtmlElement button = (HtmlElement) page.getElementById("form:submit");
        page = button.click();
        assertTrue(page.asXml().contains("type of @untypedXunset: Object"));
    }

    @Test
    public void testType2() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/attribute/attributeType.xhtml?test=untypedXsetByApi");
        assertTrue(page.asXml().contains("type of @untypedXsetByApi: Object"));
        HtmlElement button = (HtmlElement) page.getElementById("form:submit");
        page = button.click();
        assertTrue(page.asXml().contains("type of @untypedXsetByApi: Object"));
    }

    @Test
    public void testType3() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/attribute/attributeType.xhtml?test=untypedXliteral");
        assertTrue(page.asXml().contains("type of @untypedXliteral: Object"));
        HtmlElement button = (HtmlElement) page.getElementById("form:submit");
        page = button.click();
        assertTrue(page.asXml().contains("type of @untypedXliteral: Object"));
    }

    @Test
    public void testType4() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/attribute/attributeType.xhtml?test=untypedXwideEL");
        assertTrue(page.asXml().contains("type of @untypedXwideEL: Animal"));
        HtmlElement button = (HtmlElement) page.getElementById("form:submit");
        page = button.click();
        assertTrue(page.asXml().contains("type of @untypedXwideEL: Animal"));
    }

    @Test
    public void testType5() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/attribute/attributeType.xhtml?test=untypedXmediumEL");
        assertTrue(page.asXml().contains("type of @untypedXmediumEL: Dog"));
        HtmlElement button = (HtmlElement) page.getElementById("form:submit");
        page = button.click();
        assertTrue(page.asXml().contains("type of @untypedXmediumEL: Dog"));
    }

    @Test
    public void testType6() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/attribute/attributeType.xhtml?test=untypedXnarrowEL");
        assertTrue(page.asXml().contains("type of @untypedXnarrowEL: Wienerdoodle"));
        HtmlElement button = (HtmlElement) page.getElementById("form:submit");
        page = button.click();
        assertTrue(page.asXml().contains("type of @untypedXnarrowEL: Wienerdoodle"));
    }

    @Test
    public void testType7() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/attribute/attributeType.xhtml?test=untypedXnullEL");
        assertTrue(page.asXml().contains("type of @untypedXnullEL: Dog"));
        HtmlElement button = (HtmlElement) page.getElementById("form:submit");
        page = button.click();
        assertTrue(page.asXml().contains("type of @untypedXnullEL: Dog"));
    }

    @Test
    public void testType8() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/attribute/attributeType.xhtml?test=typedXunset");
        assertTrue(page.asXml().contains("type of @typedXunset: Dog"));
        HtmlElement button = (HtmlElement) page.getElementById("form:submit");
        page = button.click();
        assertTrue(page.asXml().contains("type of @typedXunset: Dog"));
    }

    @Test
    public void testType9() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/attribute/attributeType.xhtml?test=typedXliteral");
        assertTrue(page.asXml().contains("type of @typedXliteral: Integer"));
        HtmlElement button = (HtmlElement) page.getElementById("form:submit");
        page = button.click();
        assertTrue(page.asXml().contains("type of @typedXliteral: Integer"));
    }

    @Test
    public void testType10() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/attribute/attributeType.xhtml?test=typedXwideEL");
        assertTrue(page.asXml().contains("type of @typedXwideEL: Dog"));
        HtmlElement button = (HtmlElement) page.getElementById("form:submit");
        page = button.click();
        assertTrue(page.asXml().contains("type of @typedXwideEL: Dog"));
    }

    @Test
    public void testType11() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/attribute/attributeType.xhtml?test=typedXmediumEL");
        assertTrue(page.asXml().contains("type of @typedXmediumEL: Dog"));
        HtmlElement button = (HtmlElement) page.getElementById("form:submit");
        page = button.click();
        assertTrue(page.asXml().contains("type of @typedXmediumEL: Dog"));
    }

    @Test
    public void testType12() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/attribute/attributeType.xhtml?test=typedXnarrowEL");
        assertTrue(page.asXml().contains("type of @typedXnarrowEL: Wienerdoodle"));
        HtmlElement button = (HtmlElement) page.getElementById("form:submit");
        page = button.click();
        assertTrue(page.asXml().contains("type of @typedXnarrowEL: Wienerdoodle"));
    }

    @Test
    public void testType13() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/attribute/attributeType.xhtml?test=typedXnullEL");
        assertTrue(page.asXml().contains("type of @typedXnullEL: Dog"));
        HtmlElement button = (HtmlElement) page.getElementById("form:submit");
        page = button.click();
        assertTrue(page.asXml().contains("type of @typedXnullEL: Dog"));
    }

    @Test
    public void testType14() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/attribute/attributeType.xhtml?test=typedXsetByApi");
        assertTrue(page.asXml().contains("type of @typedXsetByApi: Dog"));
        HtmlElement button = (HtmlElement) page.getElementById("form:submit");
        page = button.click();
        assertTrue(page.asXml().contains("type of @typedXsetByApi: Dog"));
    }
}
