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

package com.sun.faces.test.servlet30.lifeycleDebugObjectOutputStream;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import org.junit.*;
import static org.junit.Assert.*;

public class Issue2466IT {

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
    public void testConfigurationEffective() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        
        HtmlCheckBoxInput checkbox = (HtmlCheckBoxInput) page.getElementById("checkbox");
        checkbox.setChecked(true);
        
        HtmlTextInput inputText = (HtmlTextInput) page.getElementById("inputText");
        final String textValue = System.currentTimeMillis() + "";
        inputText.setValueAttribute(textValue);
        
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");
        
        page = button.click();
        
        String text = page.getBody().asText();
        
        assertTrue(text.contains("checkbox: true"));
        assertTrue(text.contains("inputText: " + textValue));
        
        /******
         * Because this test has no way to run only when the 
         * state saving mode would always cause serialization, this 
         * is commented out.  But it is useful when running the test app
         * interactively.
         * 
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlAnchor fail = (HtmlAnchor) page.getElementById("fail");
        page = fail.click();
        
        assertTrue(page.asText().contains("Intentional failure"));
         *****/

    }
}
