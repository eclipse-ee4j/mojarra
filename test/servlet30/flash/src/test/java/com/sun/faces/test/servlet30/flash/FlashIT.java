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

package com.sun.faces.test.servlet30.flash;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_1_4;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_2_1;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;
import static com.sun.faces.test.junit.JsfVersion.JSF_2_2_0_M02;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class FlashIT {

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

    @JsfTest(value = JSF_2_2_0_M02, excludes = {WEBLOGIC_12_1_4, WEBLOGIC_12_2_1})
    @Test
    public void testFlash() throws Exception {
        // Get the first page
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");
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
        page = webClient.getPage(webUrl + "faces/index.xhtml");

        // the page contains the following span, with the following id, with no contents
        // meaning the flash has no value for foo
        //assertTrue(pageText.matches("(?s)(?m).*<span.*id=\"fooValueId\">\\s*</span>.*"));
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

        // click the link
        HtmlAnchor link = (HtmlAnchor) page.getElementById("link");
        page = link.click();

        // on flash5
        link = (HtmlAnchor) page.getElementById("link");
        page = link.click();

        assertTrue(page.asText().contains("Value is 1."));

        // click the link on the next page
        link = (HtmlAnchor) page.getElementById("link");
        page = link.click();

        assertTrue(page.asText().contains("Value is 1."));

        // click the link on the same page
        link = (HtmlAnchor) page.getElementById("link");
        page = link.click();

        assertTrue(page.asText().contains("Value is 1."));

        // click the link on the same page
        link = (HtmlAnchor) page.getElementById("link");
        page = link.click();

        assertTrue(page.asText().contains("Value is 1."));

        link = (HtmlAnchor) page.getElementById("link2");
        page = link.click();

        assertTrue(page.asText().contains("Value is 1."));

        link = (HtmlAnchor) page.getElementById("link");
        page = link.click();

        // it went away because there was no keep
        assertTrue(page.asText().contains("Value is ."));

        page = webClient.getPage(webUrl + "faces/flash9.xhtml");

        text = (HtmlTextInput) page.getHtmlElementById("valueA");
        text.setValueAttribute("a value");

        text = (HtmlTextInput) page.getHtmlElementById("valueB");
        text.setValueAttribute("b value");

        text = (HtmlTextInput) page.getHtmlElementById("valueC");
        text.setValueAttribute("c value");

        button = (HtmlSubmitInput) page.getHtmlElementById("keep");
        page = (HtmlPage) button.click();

        pageText = page.asText();

        assertTrue(pageText.contains("valueA: a value"));
        assertTrue(pageText.contains("valueB: b value"));
        assertTrue(pageText.contains("valueC: c value"));

        link = (HtmlAnchor) page.getElementById("flash9");
        page = link.click();

        text = (HtmlTextInput) page.getHtmlElementById("valueA");
        assertEquals(text.getValueAttribute(), "a value");

        text = (HtmlTextInput) page.getHtmlElementById("valueB");
        assertEquals(text.getValueAttribute(), "b value");

        text = (HtmlTextInput) page.getHtmlElementById("valueC");
        assertEquals(text.getValueAttribute(), "c value");

        page = webClient.getPage(webUrl + "faces/flash9.xhtml");
        text = (HtmlTextInput) page.getHtmlElementById("valueA");
        assertEquals(text.getValueAttribute(), "");

        text = (HtmlTextInput) page.getHtmlElementById("valueB");
        assertEquals(text.getValueAttribute(), "");

        text = (HtmlTextInput) page.getHtmlElementById("valueC");
        assertEquals(text.getValueAttribute(), "");

        text = (HtmlTextInput) page.getHtmlElementById("valueA");
        text.setValueAttribute("A value");

        text = (HtmlTextInput) page.getHtmlElementById("valueB");
        text.setValueAttribute("B value");

        text = (HtmlTextInput) page.getHtmlElementById("valueC");
        text.setValueAttribute("C value");

        button = (HtmlSubmitInput) page.getHtmlElementById("nokeep");
        page = (HtmlPage) button.click();

        pageText = page.asText();

        assertTrue(pageText.contains("valueA: A value"));
        assertTrue(pageText.contains("valueB: B value"));
        assertTrue(pageText.contains("valueC: C value"));

        button = (HtmlSubmitInput) page.getHtmlElementById("reload");
        page = (HtmlPage) button.click();

        pageText = page.asText();

        assertTrue(!pageText.contains("valueA: A value"));
        assertTrue(!pageText.contains("valueB: B value"));
        assertTrue(!pageText.contains("valueC: C value"));

        // content from Sebastian Hennebrueder
        page = webClient.getPage(webUrl + "faces/flash12.xhtml");
        button = (HtmlSubmitInput) page.getHtmlElementById("start");
        page = (HtmlPage) button.click();

        pageText = page.asText();
        assertTrue(pageText.contains("4711"));

        page = (HtmlPage) page.refresh();

        pageText = page.asText();
        assertTrue(!pageText.contains("4711"));

        // Test for JAVASERVERFACES-2087
        page = webClient.getPage(webUrl + "faces/flash13.xhtml");
        button = (HtmlSubmitInput) page.getHtmlElementById("flashbtn");
        page = (HtmlPage) button.click();

        pageText = page.asText();
        assertTrue(pageText.contains("read strobist"));

        page = webClient.getPage(webUrl + "faces/flash13.xhtml");
        pageText = page.asText();
        assertTrue(!pageText.contains("read strobist"));

        page = webClient.getPage(webUrl + "faces/flash13.xhtml");
        pageText = page.asText();
        assertTrue(!pageText.contains("read strobist"));

        page = webClient.getPage(webUrl + "faces/flash14.xhtml");
        pageText = page.asText();
        assertTrue(pageText.matches("(?s).*\\[received jakarta.faces.event.PreClearFlashEvent source:\\{\\}\\].*"));
    }
}
