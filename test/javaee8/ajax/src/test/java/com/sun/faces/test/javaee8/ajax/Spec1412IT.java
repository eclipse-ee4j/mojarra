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

package com.sun.faces.test.javaee8.ajax;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.sun.faces.test.htmlunit.IgnoringIncorrectnessListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Spec1412IT {

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
    public void testSpec1412() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());

        HtmlPage page = webClient.getPage(webUrl + "spec1412.xhtml");
        assertTrue(!page.asText().contains("Success!"));

        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("form:button");
        assertTrue(button.asText().equals("foo"));

        page = button.click();
        webClient.waitForBackgroundJavaScript(60000);
        assertTrue(page.asText().contains("Success!"));

        button = (HtmlSubmitInput) page.getHtmlElementById("form:button");
        assertTrue(button.asText().equals("bar"));
    }

    @After
    public void tearDown() {
        webClient.close();
    }

}
