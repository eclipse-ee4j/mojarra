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

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.sun.faces.test.junit.JsfTestRunner;

@RunWith(JsfTestRunner.class)
public class Spec1433IT {

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
    public void testSpec1433() throws Exception {

        HtmlPage page = webClient.getPage(webUrl + "spec1433.xhtml");
        HtmlTextInput input = (HtmlTextInput) page.getElementById("input");
        input.setAttribute("id", "");
        input.setAttribute("name", "");
        input.setValueAttribute("non-empty value");

	HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("submit");

        page = button.click();

        String output = page.asText();

        assertTrue(output.contains("Spec1433Bean Validator Message"));

    }

    @After
    public void tearDown() {
        webClient.close();
    }

}
