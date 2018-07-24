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

package com.sun.faces.test.servlet30.ajax;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

/**
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
public class Issue1533IT {

    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(60000);
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testIssue1533() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/issue1533.xhtml");

        HtmlInput input = (HtmlInput) page.getHtmlElementById("form:vip:0");
        page = input.click();
        webClient.waitForBackgroundJavaScript(120000);
        assertTrue(page.asText().indexOf("form:vip-true") != -1);

        input = (HtmlInput) page.getHtmlElementById("form:vip:1");
        page = input.click();
        webClient.waitForBackgroundJavaScript(120000);
        assertTrue(page.asText().indexOf("form:vip-false") != -1);
    }
}
