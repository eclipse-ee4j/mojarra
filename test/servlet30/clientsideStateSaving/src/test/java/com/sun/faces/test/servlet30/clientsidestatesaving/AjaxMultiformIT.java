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

package com.sun.faces.test.servlet30.clientsidestatesaving;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

public class AjaxMultiformIT {

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

    @Test
    public void testAjaxMultiform() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/ajaxMultiform.xhtml");

        // First we'll check the first page was output correctly
        HtmlElement element = page.getHtmlElementById("countForm1:out1");
        assertEquals("0", element.asText());
        element = page.getHtmlElementById("countForm2:out1");
        assertEquals("1", element.asText());
        element = page.getHtmlElementById("countForm3:out1");
        assertEquals("2", element.asText());
        element = page.getHtmlElementById("out2");
        assertEquals("3", element.asText());

        // Submit the ajax request
        HtmlSubmitInput button1 = (HtmlSubmitInput) page.getHtmlElementById("countForm1:button1");
        page = (HtmlPage) button1.click();
        webClient.waitForBackgroundJavaScript(60000);

        // Check that the ajax request succeeds
        element = page.getHtmlElementById("countForm1:out1");
        assertEquals("4", element.asText());

        // Check that the request did NOT update the rest of the page.
        element = page.getHtmlElementById("out2");
        assertEquals("3", element.asText());

        // Submit the ajax request
        button1 = (HtmlSubmitInput) page.getHtmlElementById("countForm2:button1");
        page = (HtmlPage) button1.click();
        webClient.waitForBackgroundJavaScript(60000);

        // Check that the ajax request succeeds
        element = page.getHtmlElementById("countForm2:out1");
        assertEquals("5", element.asText());

        // Check that the request did NOT update the rest of the page.
        element = page.getHtmlElementById("out2");
        assertEquals("3", element.asText());
    }
}
