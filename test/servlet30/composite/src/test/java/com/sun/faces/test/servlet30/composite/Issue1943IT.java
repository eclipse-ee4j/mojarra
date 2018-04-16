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
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Issue1943IT {

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
    public void testIssue1943() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/issue1943.xhtml");

        DomNodeList<HtmlElement> selectList = page.getDocumentElement().getElementsByTagName("select");
        HtmlSelect select = (HtmlSelect) selectList.get(0);
        List<HtmlOption> options = select.getOptions();
        select.setSelectedAttribute(options.get(0), true);
        DomNodeList<HtmlElement> buttonList = page.getDocumentElement().getElementsByTagName("input");
        HtmlSubmitInput submitButton = (HtmlSubmitInput) buttonList.get(1);
        page = (HtmlPage) submitButton.click();
        assertTrue(page.asText().contains("valueChange Called"));

        page = webClient.getPage(webUrl + "faces/issue1943-action.xhtml");
        buttonList = page.getDocumentElement().getElementsByTagName("input");
        HtmlSubmitInput submitButton1 = (HtmlSubmitInput) buttonList.get(1);
        page = (HtmlPage) submitButton1.click();
        assertTrue(page.asText().contains("removeGroup Called"));
        HtmlSubmitInput submitButton2 = (HtmlSubmitInput) buttonList.get(2);
        page = (HtmlPage) submitButton2.click();
        assertTrue(!page.asText().contains("removeGroup Called"));
    }
}
