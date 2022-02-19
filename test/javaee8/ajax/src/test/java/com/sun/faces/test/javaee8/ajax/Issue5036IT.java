/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation.
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

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import com.sun.faces.test.htmlunit.IgnoringIncorrectnessListener;

public class Issue5036IT {

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
    public void testIssue5036() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());

        for (char c = 0x00; c <= 0x1f; c++) {
            String hexValue = String.format("%02x", (int) c);

            HtmlPage page = webClient.getPage(webUrl + "issue5036.xhtml");
            HtmlTextInput hexInput = (HtmlTextInput) page.getElementById("form:hex");
            hexInput.setValueAttribute(hexValue);
            page = page.getHtmlElementById("form:submit").click();
            webClient.waitForBackgroundJavaScript(3000);

            String output = page.getElementById("form:output").asText().trim();
            assertEquals("Ajax output must be successfully printed when containing control character 0x" + hexValue, "Output", output);
        }
     }

    @After
    public void tearDown() {
        webClient.close();
    }

}
