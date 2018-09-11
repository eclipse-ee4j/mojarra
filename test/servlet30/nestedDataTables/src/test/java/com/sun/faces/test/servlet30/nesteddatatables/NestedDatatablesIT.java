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

package com.sun.faces.test.servlet30.nesteddatatables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

@Ignore("Quite old, is this still useful?")
public class NestedDatatablesIT {

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
    public void testInputFieldUpdate() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/test.jsp");

        for (int i = 0; i < 3; i++) {
            HtmlTextInput input = (HtmlTextInput) page.getHtmlElementById("form:outerData:0:innerData:" + i + ":inputText");
            input.setValueAttribute("" + i);
        }
        for (int i = 3; i < 6; i++) {
            HtmlTextInput input = (HtmlTextInput) page.getHtmlElementById("form:outerData:1:innerData:" + (i - 3) + ":inputText");
            input.setValueAttribute("" + i);
        }

        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("form:submit");
        page = button.click();

        for (int i = 0; i < 3; i++) {
            HtmlTextInput input = (HtmlTextInput) page.getHtmlElementById("form:outerData:0:innerData:" + i + ":inputText");
            assertEquals("" + i, input.getValueAttribute());
        }
        for (int i = 3; i < 6; i++) {
            HtmlTextInput input = (HtmlTextInput) page.getHtmlElementById("form:outerData:1:innerData:" + (i - 3) + ":inputText");
            assertEquals("" + i, input.getValueAttribute());
        }

        for (int i = 0; i < 3; i++) {
            HtmlTextInput input = (HtmlTextInput) page.getHtmlElementById("form:outerData:0:innerData:" + i + ":inputText");
            input.setValueAttribute(Character.toString((char) ('a' + i)));
        }
        for (int i = 3; i < 6; i++) {
            HtmlTextInput input = (HtmlTextInput) page.getHtmlElementById("form:outerData:1:innerData:" + (i - 3) + ":inputText");
            input.setValueAttribute(Character.toString((char) ('a' + i)));
        }

        button = (HtmlSubmitInput) page.getHtmlElementById("form:submit");
        page = button.click();

        for (int i = 0; i < 3; i++) {
            HtmlTextInput input = (HtmlTextInput) page.getHtmlElementById("form:outerData:0:innerData:" + i + ":inputText");
            assertEquals(Character.toString((char) ('a' + i)), input.getValueAttribute());
        }
        for (int i = 3; i < 6; i++) {
            HtmlTextInput input = (HtmlTextInput) page.getHtmlElementById("form:outerData:1:innerData:" + (i - 3) + ":inputText");
            assertEquals(Character.toString((char) ('a' + i)), input.getValueAttribute());
        }
    }

    public void testInputFieldUpdate2() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/nested.jsp");

        HtmlTextInput input1 = page.getHtmlElementById("form:outer:0:inputText");
        input1.setValueAttribute("" + 0);
        HtmlTextInput input2 = page.getHtmlElementById("form:outer:1:inputText");
        input2.setValueAttribute("" + 1);

        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("form:submit");
        page = button.click();

        input1 = page.getHtmlElementById("form:outer:0:inputText");
        assertEquals("" + 0, input1.getValueAttribute());
        input2 = page.getHtmlElementById("form:outer:1:inputText");
        assertEquals("" + 1, input2.getValueAttribute());

        input1 = page.getHtmlElementById("form:outer:0:inputText");
        input1.setValueAttribute(Character.toString((char) ('a' + 0)));
        input2 = page.getHtmlElementById("form:outer:1:inputText");
        input2.setValueAttribute(Character.toString((char) ('a' + 1)));

        button = (HtmlSubmitInput) page.getHtmlElementById("form:submit");
        page = button.click();

        input1 = page.getHtmlElementById("form:outer:0:inputText");
        assertEquals(Character.toString((char) ('a' + 0)), input1.getValueAttribute());
        input2 = page.getHtmlElementById("form:outer:1:inputText");
        assertEquals(Character.toString((char) ('a' + 1)), input2.getValueAttribute());

        button = (HtmlSubmitInput) page.getHtmlElementById("form:outer:0:inner:add-port");
        page = button.click();

        HtmlTextInput portNumberInput = (HtmlTextInput) page.getHtmlElementById("form:outer:0:inner:0:portNumber");
        portNumberInput.setValueAttribute("12");

        button = (HtmlSubmitInput) page.getHtmlElementById("form:submit");
        page = button.click();

        portNumberInput = (HtmlTextInput) page.getHtmlElementById("form:outer:0:inner:0:portNumber");
        assertEquals("12", portNumberInput.getValueAttribute());

        button = (HtmlSubmitInput) page.getHtmlElementById("form:outer:1:inner:add-port");
        page = button.click();

        portNumberInput = (HtmlTextInput) page.getHtmlElementById("form:outer:1:inner:0:portNumber");
        assertTrue(-1 == portNumberInput.getValueAttribute().indexOf("12"));
    }
}
