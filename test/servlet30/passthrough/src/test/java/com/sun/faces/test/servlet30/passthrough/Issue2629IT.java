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

package com.sun.faces.test.servlet30.passthrough;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlFieldSet;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.*;
import org.junit.Test;

public class Issue2629IT {

    /**
     * Stores the web URL.
     */
    private String webUrl;
    /**
     * Stores the web client.
     */
    private WebClient webClient;

    /**
     * Setup before testing.
     */
    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(60000);
    }

    /**
     * Tear down after testing.
     */
    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testFieldSetAjaxBehavior() throws Exception {
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(60000);
        HtmlPage page = webClient.getPage(webUrl + "/faces/fieldset.xhtml");
        HtmlFieldSet fieldset = (HtmlFieldSet) page.getHtmlElementById("fieldset4");
        page = fieldset.click();
        webClient.waitForBackgroundJavaScript(60000);
        assertTrue(page.asXml().contains("fieldset4 Event: begin"));
        assertTrue(page.asXml().contains("fieldset4 Event: complete"));
        assertTrue(page.asXml().contains("fieldset4 Event: success"));
    }

    @Test
    public void testLabelAjaxBehavior() throws Exception {
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(60000);
        HtmlPage page = webClient.getPage(webUrl + "/faces/label.xhtml");
        HtmlElement label = page.getHtmlElementById("label2");
        page = (HtmlPage) label.mouseOver();
        webClient.waitForBackgroundJavaScript(60000);
        assertTrue(page.asXml().contains("label2 Event: begin"));
        assertTrue(page.asXml().contains("label2 Event: complete"));
        assertTrue(page.asXml().contains("label2 Event: success"));
    }
}
