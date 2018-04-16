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

package com.sun.faces.test.servlet30.flashBasic;

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;
import com.sun.faces.test.junit.JsfVersion;
import org.junit.After;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class Issue2973IT {

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

    @JsfTest(JsfVersion.JSF_2_2_2)
    @Test
    public void testServerRestartHandledGracefully() throws Exception {

        HtmlPage page = webClient.getPage(webUrl + "faces/issue2973/page1.xhtml") ;
        webClient.getOptions().setRedirectEnabled(true);
        HtmlTextInput textInput = (HtmlTextInput) page.getElementById("input");
        String message = "" + System.currentTimeMillis();
        textInput.setValueAttribute(message);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();
        HtmlElement value = (HtmlElement) page.getElementById("response");
        
        assertEquals(message, value.asText());
        
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        boolean assertionValue = false;
        for (int i = 0; i < 3; i++) {
            page = webClient.getPage(webUrl + "faces/issue2973/page1.xhtml") ;
            button = (HtmlSubmitInput) page.getElementById("restart");
            page = button.click();
            Thread.sleep(3000);
            
            textInput = (HtmlTextInput) page.getElementById("input");
            message = "" + System.currentTimeMillis();
            textInput.setValueAttribute(message);
            button = (HtmlSubmitInput) page.getElementById("button");
            page = button.click();
            value = (HtmlElement) page.getElementById("response");
        
            if (null != value) {
                assertionValue = message.equals(value.asText());
            }
            if (assertionValue) {
                break;
            }
        }
        assertTrue(assertionValue);   
        
    }
}
