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

package com.sun.faces.test.servlet30.composite;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import org.junit.*;
import static org.junit.Assert.assertTrue;

public class Issue2195IT {

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
    public void testActionListener1() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/actionListener/actionListener1.xhtml");
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getElementById("form:cc_action:button");
        page = (HtmlPage) submit.click();
        assertTrue(page.asXml().contains("PROCESSACTION CALLED"));
    }

    @Test
    public void testAjax1() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/ajax/ajax1.xhtml");
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getElementById("form:cc_ajax:button");
        page = (HtmlPage) submit.click();
        webClient.waitForBackgroundJavaScript(60000);
        assertTrue(page.asXml().contains("PROCESSAJAXBEHAVIOR CALLED"));
    }

    @Test
    public void testEvent1() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/event/event1.xhtml");
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getElementById("form:button");
        page = (HtmlPage) submit.click();
        assertTrue(page.asXml().contains("LISTENER CALLED"));
    }

    @Test
    public void testValueChangeListener1() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/valueChangeListener/valueChangeListener1.xhtml");
        HtmlTextInput input = (HtmlTextInput) page.getElementById("form:cc_value:input");
        input.setValueAttribute("Foo");
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getElementById("form:button");
        page = (HtmlPage) submit.click();
        assertTrue(page.asXml().contains("VALUECHANGE CALLED"));
    }
}
