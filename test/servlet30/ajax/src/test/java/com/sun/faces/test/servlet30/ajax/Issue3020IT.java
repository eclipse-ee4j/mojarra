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

import com.gargoylesoftware.htmlunit.ScriptException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

public class Issue3020IT {

    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(60000);
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testDelayPositive() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/issue3020Positive.xhtml");

        HtmlTextInput in1 = (HtmlTextInput) page.getHtmlElementById("input");
        in1.type("a");

        webClient.waitForBackgroundJavaScript(60000);

        // Check that the ajax request succeeds
        assertTrue(page.getHtmlElementById("result").asText().contains("aaaaaaaaaa")); 
    }
    
    @Test
    public void testDelayNegative() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/issue3020Negative.xhtml");

        HtmlTextInput in1 = (HtmlTextInput) page.getHtmlElementById("input");
        boolean exceptionCaught = false;
        try {
            in1.type("a");
            
            webClient.waitForBackgroundJavaScript(60000);
        } catch (ScriptException se) {
            assertTrue(se.getMessage().contains("NaN"));
            exceptionCaught = true;
        }

        // Check that the ajax request does not succeed
        assertTrue(exceptionCaught);
    }
    

}
