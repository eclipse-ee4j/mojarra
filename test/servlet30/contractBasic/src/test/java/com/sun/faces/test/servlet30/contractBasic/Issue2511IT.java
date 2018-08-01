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

package com.sun.faces.test.servlet30.contractBasic;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlLink;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

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
    public void testTemplatesAreUsed() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");
        String text = page.asText();

        assertTrue(text.contains("Top Navigation Menu"));

        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();

        text = page.asText();
        assertTrue(text.contains("Left Side Navigation Menu"));

    }

    @Test
    public void testResourcesAreRendered() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");

        examineCss(page.getElementsByTagName("link"));

        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();

        examineCss(page.getElementsByTagName("link"));
    }

    private void examineCss(DomNodeList<DomElement> cssFiles) throws Exception {
        HtmlLink curLink;
        String href;
        String content;
        for (DomElement cur : cssFiles) {
            curLink = (HtmlLink) cur;
            href = curLink.getHrefAttribute();
            assertTrue(href.contains("con=siteLayout"));
            if (href.contains("default.css")) {
                content = curLink.getWebResponse(true).getContentAsString(UTF_8);
                assertTrue(content.contains("#AFAFAF"));
            } else if (href.contains("cssLayout.css")) {
                content = curLink.getWebResponse(true).getContentAsString(UTF_8);
                assertTrue(content.contains("#036fab"));
            } else {
                fail();
            }
        }

    }

}
