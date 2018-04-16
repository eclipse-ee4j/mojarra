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

package com.sun.faces.test.javaee8.facelets;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.sun.faces.test.htmlunit.IgnoringIncorrectnessListener;
import com.sun.faces.test.junit.JsfTestRunner;

@RunWith(JsfTestRunner.class)
public class Spec1102IT {

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
    public void testSpec1102() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());

        HtmlPage page = webClient.getPage(webUrl + "spec1102.xhtml");
        assertTrue(page.getHtmlElementById("repeat1").asText().equals("123"));
        assertTrue(page.getHtmlElementById("repeat2").asText().equals("-3-2-10123"));
        assertTrue(page.getHtmlElementById("repeat3").asText().equals("3210-1-2-3"));
        assertTrue(page.getHtmlElementById("repeat4").asText().equals("-3-113"));
        assertTrue(page.getHtmlElementById("repeat5").asText().equals("-3-2"));
        assertTrue(page.getHtmlElementById("repeat6").asText().equals("-3"));
        assertTrue(page.getHtmlElementById("repeat7").asText().equals("3"));
        assertTrue(page.getHtmlElementById("repeat8").asText().equals("0123"));
    }

    @After
    public void tearDown() {
        webClient.close();
    }

}
