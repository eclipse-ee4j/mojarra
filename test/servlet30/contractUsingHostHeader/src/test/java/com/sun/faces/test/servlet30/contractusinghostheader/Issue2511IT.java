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

package com.sun.faces.test.servlet30.contractusinghostheader;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;
import com.sun.faces.test.junit.JsfVersion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.IOException;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class Issue2511IT {

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
    public void testResources() throws Exception {
        checkCss("defaultHost", null);
        checkCss("host1", "host1");
        checkCss("host2", null);
    }

    @Test
    public void testDefaultTemplate() throws Exception {
        webClient.removeRequestHeader("Host");
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");
        DomElement footer = page.getElementById("footer");
        assertThat(footer, nullValue());
        DomElement content = page.getElementById("content");
        assertThat(content, notNullValue());
        assertThat(content.getTextContent().trim(), containsString("main content"));
    }

    @Test
    public void testAnotherTemplate() throws Exception {
        webClient.addRequestHeader("Host", "host2");
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");
        HtmlElement host2content = page.getHtmlElementById("host2content");
        assertThat(host2content, notNullValue());
        HtmlElement footer = page.getHtmlElementById("footer");
        assertThat(footer, notNullValue());
        assertThat(footer.getTextContent().trim(), is("footer info"));
    }

    @Test
    public void testFalsePositive() throws Exception {
        webClient.addRequestHeader("Host", "host3");
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");
        HtmlElement content = page.getHtmlElementById("content");
        assertThat(content, notNullValue());
        assertThat(content.getTextContent().trim(), is("false positive"));
    }

    @Test
    public void testInclude() throws Exception {
        webClient.removeRequestHeader("Host");
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");
        DomElement header = page.getElementById("header");
        assertThat(header, notNullValue());
        assertThat(header.getTextContent().trim(), is("header content"));
        webClient.addRequestHeader("Host", "host1");
        page = webClient.getPage(webUrl + "faces/index.xhtml");
        header = page.getElementById("header");
        assertThat(header, nullValue());
    }

    @Test
    public void testExtension() throws Exception {
        webClient.addRequestHeader("Host", "host4");
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");
        HtmlElement content = page.getHtmlElementById("host2content");
        assertThat(content, notNullValue());
        assertThat(content.getTextContent().trim(), is("host4 content"));
        HtmlElement footer = page.getHtmlElementById("footer");
        assertThat(footer, notNullValue());
        assertThat(footer.getTextContent().trim(), is(""));
    }

    @JsfTest(JsfVersion.JSF_2_2_1)
    @Test
    public void testCompositeComponent() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");

        HtmlElement template = page.getHtmlElementById("ccTemplate");
        assertThat(template, notNullValue());
        assertThat(template.getTextContent().trim(), is("lib/2_3/template.xhtml"));

        HtmlElement content = page.getHtmlElementById("ccContent");
        assertThat(content, notNullValue());
        assertThat(content.getTextContent().trim(), containsString("lib/2_3/cc.xhtml"));

        webClient.addRequestHeader("Host", "host1");
        page = webClient.getPage(webUrl + "faces/index.xhtml");

        content = page.getHtmlElementById("ccContent");
        assertThat(content, notNullValue());
        assertThat(content.getTextContent().trim(), containsString("host1/lib/cc.xhtml"));

    }

    private void checkCss(String host, String contract) throws IOException {
        webClient.addRequestHeader("Host", host);
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");
        String titleText = page.getTitleText();
        assertThat(titleText, is(host));
        DomNodeList<DomElement> linkElements = page.getElementsByTagName("link");
        for (DomElement linkElement : linkElements) {
            HtmlLink link = (HtmlLink) linkElement;
            if(contract == null) {
                assertThat(link.getHrefAttribute(), not(containsString("con=")));
            } else {
                assertThat(link.getHrefAttribute(), containsString("con=" + contract));
            }
        }
    }
}
