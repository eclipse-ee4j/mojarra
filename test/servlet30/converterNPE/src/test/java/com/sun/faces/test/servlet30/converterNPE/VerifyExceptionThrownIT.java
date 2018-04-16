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

package com.sun.faces.test.servlet30.converterNPE;

import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;
import com.sun.faces.test.junit.JsfVersion;
import static org.junit.Assert.assertTrue;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class VerifyExceptionThrownIT {

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

    @JsfTest(JsfVersion.JSF_2_2_1)
    @Test
    public void testConverterThrowsNPEViaAjax() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("button1");
        page = button.click();
        webClient.waitForBackgroundJavaScript(60000);
        HtmlElement span = page.getHtmlElementById("ajaxResponseOutput");
        assertTrue(span.asText().contains("NullPointerException"));
    }

    @Test
    public void testConverterThrowsNPEViaNonAjax() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("button2");
        page = button.click();
        assertTrue(page.asText().contains("NullPointerException"));
    }
}
