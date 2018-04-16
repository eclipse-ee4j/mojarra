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

package com.sun.faces.test.servlet30.jspflash;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_1_4;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_2_1;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;
import com.sun.faces.test.junit.JsfVersion;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class JspFlashIT {

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

    @JsfTest(value = JsfVersion.JSF_2_3_0_M02, excludes = {WEBLOGIC_12_1_4, WEBLOGIC_12_2_1})
    @Test
    public void testFlash() throws Exception {
        // Get the first page
        HtmlPage page = webClient.getPage(webUrl + "home-flash.jsf");
        String pageText = page.asXml();
        // (?s) is an "embedded flag expression" for the "DOTALL" operator.
        // It says, "let . match any character including line terminators."
        // Because page.asXml() returns a big string with lots of \r\n chars
        // in it, we need (?s).
        // the page contains a table tag with a frame attribute whose value is hsides.

        // the page contains the following span, with the following id, with no contents
        assertTrue(pageText.matches("(?s)(?m).*<span.*id=\"fooValueId\">\\s*</span>.*"));

        // Click the reload button
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("reload");
        page = (HtmlPage) button.click();
        pageText = page.asXml();
        // verify that fooValue is there, indicating that it's been stored in the flash
        assertTrue(pageText.matches("(?s)(?m).*<span.*id=\"fooValueId\">\\s*fooValue\\s*</span>.*"));

        // Get the first page, again
        page = webClient.getPage(webUrl + "home-flash.jsf");

        // fill in "addMessage" in the textBox
        HtmlTextInput text = (HtmlTextInput) page.getHtmlElementById("inputText");
        text.setValueAttribute("addMessage");

        // go to the next page
        button = (HtmlSubmitInput) page.getHtmlElementById("next");
        page = (HtmlPage) button.click();
        pageText = page.asXml();
        // See that it has fooValue
        assertTrue(pageText.matches("(?s)(?m).*<span.*id=\"flash2FooValueId\">\\s*fooValue\\s*</span>.*"));
        // See that it has barValue
        assertTrue(pageText.matches("(?s)(?m).*<span.*id=\"flash2BarValueId\">\\s*barValue\\s*</span>.*"));
        // See that it has the message
        assertTrue(-1 != pageText.indexOf("test that this persists across the redirect"));

        // click the reload button
        button = (HtmlSubmitInput) page.getHtmlElementById("reload");
        page = (HtmlPage) button.click();
        pageText = page.asXml();

        // See that it doesn't have the message
        assertTrue(-1 == pageText.indexOf("test that this persists across the redirect"));

        // Click the back button
        button = (HtmlSubmitInput) page.getHtmlElementById("back");
        page = (HtmlPage) button.click();
        pageText = page.asXml();

        // Click the next button
        button = (HtmlSubmitInput) page.getHtmlElementById("next");
        page = (HtmlPage) button.click();
        pageText = page.asXml();

        // See that the page does not have the message
        assertTrue(-1 == pageText.indexOf("test that this persists across the redirect"));

        // Click the next button
        button = (HtmlSubmitInput) page.getHtmlElementById("next");
        page = (HtmlPage) button.click();
        pageText = page.asXml();

        // See that it has banzai
        assertTrue(pageText.matches("(?s)(?m).*<span.*id=\"flash3NowValueId\">\\s*banzai\\s*</span>.*"));

        // Click the next button
        button = (HtmlSubmitInput) page.getHtmlElementById("next");
        page = (HtmlPage) button.click();
        pageText = page.asXml();

        // See that it still has banzai
        assertTrue(pageText.matches("(?s)(?m).*<span.*id=\"flash4BuckarooValueId\">\\s*banzai\\s*</span>.*"));
    }
}
