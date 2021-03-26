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

package com.sun.faces.test.servlet30.ajax; 

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;

import org.junit.*;
import static org.junit.Assert.*;

public class Issue2750IT {

    /**
     * Stores the web URL.
     */
    private String webUrl;
    /**
     * Stores the web client.
     */
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


    // ------------------------------------------------------------ Test Methods

    /**
     * This test verifies that an attribute named 'checked' can be successfully updated
     * from a partial response (over Ajax). 
     */
    @Test
    public void testUpdateAttributeNamedChecked() throws Exception {
        HtmlPage page = webClient.getPage(webUrl+"faces/attributeNameIsChecked.xhtml");
        HtmlCheckBoxInput cbox = (HtmlCheckBoxInput)page.getElementById("form1:foo");
        assertTrue(cbox.isChecked() == false);
        assertTrue(page.asXml().contains("foo"));
        HtmlSubmitInput button = (HtmlSubmitInput)page.getElementById("form1:button");
        page = button.click();
        webClient.waitForBackgroundJavaScript(60000);
        cbox = (HtmlCheckBoxInput)page.getElementById("form1:foo");
        assertTrue(cbox.isChecked() == true);
    }

    /**
     * This test verifies that an attribute named 'readonly' can be successfully updated
     * from a partial response (over Ajax).
     */
    @Test
    public void testUpdateAttributeNamedReadonly() throws Exception {
        HtmlPage page = webClient.getPage(webUrl+"faces/attributeNameIsReadonly.xhtml");
        HtmlTextInput input = (HtmlTextInput)page.getElementById("form1:foo");
        assertTrue(input.isReadOnly() == false);
        HtmlSubmitInput button = (HtmlSubmitInput)page.getElementById("form1:button");
        page = button.click();
        webClient.waitForBackgroundJavaScript(60000);
        input = (HtmlTextInput)page.getElementById("form1:foo");
        assertTrue(input.isReadOnly() == true);
    }

}
