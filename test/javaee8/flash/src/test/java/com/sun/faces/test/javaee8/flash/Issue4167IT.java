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

package com.sun.faces.test.javaee8.flash;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.sun.faces.test.htmlunit.IgnoringIncorrectnessListener;
import com.sun.faces.test.junit.JsfTestRunner;

@RunWith(JsfTestRunner.class)
public class Issue4167IT {

    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(120000);
    }

    @Test
    public void test() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());

        testIssue4167();
    }

    public void testIssue4167() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "issue4167.xhtml");

        assertTrue(page.getHtmlElementById("result1").asText().isEmpty());
        assertTrue(page.getHtmlElementById("result2").asText().isEmpty());
        assertTrue(page.getHtmlElementById("result3").asText().isEmpty());

        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("form:button");
        page = button.click();

        assertTrue(page.getHtmlElementById("result1").asText().equals("issue4167"));
        assertTrue(page.getHtmlElementById("result2").asText().equals("issue4167"));
        assertTrue(page.getHtmlElementById("result3").asText().equals("issue4167"));
    }

    @After
    public void tearDown() {
        webClient.close();
    }

}
