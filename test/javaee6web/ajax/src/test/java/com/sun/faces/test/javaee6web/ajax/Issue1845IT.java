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

package com.sun.faces.test.javaee6web.ajax;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.sun.faces.test.htmlunit.IgnoringIncorrectnessListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Issue1845IT {

    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(120000);
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testIssue1845() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());
        HtmlPage page = webClient.getPage(webUrl + "faces/ajaxBehaviorRestoreState.xhtml");
        assertTrue(!page.asText().contains("Click Me"));
        HtmlCheckBoxInput checkbox = (HtmlCheckBoxInput) page.getHtmlElementById("myForm:buttonCheckbox");
        page = checkbox.click();
        webClient.waitForBackgroundJavaScript(60000);
        HtmlSubmitInput reload = (HtmlSubmitInput) page.getHtmlElementById("myForm:reload");
        page = reload.click();
        webClient.waitForBackgroundJavaScript(60000);
        assertTrue(page.asText().contains("Click Me"));
        checkbox = (HtmlCheckBoxInput) page.getHtmlElementById("myForm:buttonCheckbox");
        page = checkbox.click();
        webClient.waitForBackgroundJavaScript(60000);
        reload = (HtmlSubmitInput) page.getHtmlElementById("myForm:reload");
        page = reload.click();
        webClient.waitForBackgroundJavaScript(60000);
        assertTrue(!page.asText().contains("Click Me"));
    }
}
