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

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import static junit.framework.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Spec220IT {

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
    public void testViewState() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/viewState1.xhtml");
        HtmlTextInput textField = (HtmlTextInput) page.getElementById("firstName");
        textField.setValueAttribute("ajaxFirstName");

        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("submitAjax");
        page = button.click();
        Thread.sleep(2000);
        String pageText = page.asText();
        assertTrue(pageText.contains("|ajaxFirstName|"));

        textField = (HtmlTextInput) page.getElementById("firstName");
        textField.setValueAttribute("nonAjaxFirstName");

        button = (HtmlSubmitInput) page.getElementById("submitNonAjax");
        page = button.click();
        Thread.sleep(2000);
        pageText = page.asText();
        assertTrue(pageText.contains("|nonAjaxFirstName|"));
    }
}
