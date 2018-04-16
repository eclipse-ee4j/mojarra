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

package com.sun.faces.test.javaee7.el;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;
import static com.sun.faces.test.junit.JsfVersion.JSF_2_2_0;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class BasicLambdaIT {

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

    @JsfTest(value = JSF_2_2_0)
    @Test
    public void testIndex() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlElement out = page.getHtmlElementById("output");
        assertEquals("20", out.asText());

        HtmlTextInput input = (HtmlTextInput) page.getElementById("input");
        input.setValueAttribute("1");
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();
        out = page.getHtmlElementById("output");
        assertEquals("40", out.asText());

        input = (HtmlTextInput) page.getElementById("input");
        input.setValueAttribute("2");
        button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();
        out = page.getHtmlElementById("output");
        assertEquals("60", out.asText());
    }

    @JsfTest(value = JSF_2_2_0)
    @Test
    public void testBookTable() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/bookTable.xhtml");
        assertTrue(page.asText().contains("At Swim Two Birds"));
        assertTrue(page.asText().contains("The Third Policeman"));
        HtmlElement out = page.getHtmlElementById("output2");
        assertEquals("The Picture of Dorian Gray", out.asText());
    }
}
