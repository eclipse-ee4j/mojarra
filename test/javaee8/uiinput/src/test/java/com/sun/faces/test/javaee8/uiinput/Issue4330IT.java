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

package com.sun.faces.test.javaee8.uiinput;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.sun.faces.test.htmlunit.IgnoringIncorrectnessListener;
import com.sun.faces.test.junit.JsfTestRunner;

@RunWith(JsfTestRunner.class)
public class Issue4330IT {

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
    public void testIssue4330() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());

        HtmlPage page;
        HtmlRadioButtonInput disabledRadio;
        HtmlCheckBoxInput enabledCheckbox;
        HtmlCheckBoxInput disabledCheckbox;
        HtmlButtonInput hack;
        HtmlSubmitInput submit;

        page = webClient.getPage(webUrl + "issue4330.xhtml");
        assertTrue(page.getHtmlElementById("form:result").asText().isEmpty());

        disabledRadio = page.getHtmlElementById("form:one:1");
        enabledCheckbox = page.getHtmlElementById("form:many:0");
        disabledCheckbox = page.getHtmlElementById("form:many:1");
        hack = page.getHtmlElementById("form:hack");
        submit = page.getHtmlElementById("form:submit");

        assertTrue(disabledRadio.isDisabled());
        assertTrue(disabledCheckbox.isDisabled());

        hack.click();
        webClient.waitForBackgroundJavaScript(1000);

        assertFalse(disabledRadio.isDisabled());
        assertFalse(disabledCheckbox.isDisabled());

        disabledRadio.setChecked(true);
        enabledCheckbox.setChecked(true);
        disabledCheckbox.setChecked(true);

        page = submit.click();
        assertTrue(page.getHtmlElementById("form:result").asText().equals("[enabled]")); // Thus not "disabled[enabled, disabled]"
    }

    @After
    public void tearDown() {
        webClient.close();
    }

}