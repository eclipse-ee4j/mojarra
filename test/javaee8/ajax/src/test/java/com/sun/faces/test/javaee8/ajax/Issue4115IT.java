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
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.sun.faces.test.htmlunit.IgnoringIncorrectnessListener;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_2_1;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;
import static com.sun.faces.test.junit.JsfVersion.JSF_2_3_0_M07;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class Issue4115IT {

    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(120000);
    }

    @JsfTest(value = JSF_2_3_0_M07, excludes = {WEBLOGIC_12_2_1})
    @Test
    public void testSpec1412() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());

        HtmlPage page = webClient.getPage(webUrl + "issue4115.xhtml");
        assertTrue(page.getHtmlElementById("form:output").asText().isEmpty());

        HtmlTextInput input = (HtmlTextInput) page.getHtmlElementById("form:input");
        input.setValueAttribute("execute");
        HtmlAnchor link = (HtmlAnchor) page.getHtmlElementById("form:link");
        page = link.click();
        webClient.waitForBackgroundJavaScript(60000);
        assertTrue(page.getHtmlElementById("form:output").asText().equals("executeParamValue"));
    }

    @After
    public void tearDown() {
        webClient.close();
    }
}
