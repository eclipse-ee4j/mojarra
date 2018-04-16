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
import static org.junit.Assert.assertTrue;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class Issue2862IT {

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

    @JsfTest(JsfVersion.JSF_2_2_5)
    @Test
    public void testDropFlashCookie() throws Exception {

        HtmlPage page = webClient.getPage(webUrl + "faces/flashDropCookie.xhtml") ;
        webClient.getOptions().setRedirectEnabled(true);
        HtmlTextInput textInput = (HtmlTextInput) page.getHtmlElementById("input");
        textInput.setValueAttribute("test");
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("submit");
       
        int currentSize = webClient.getCookieManager().getCookies().size();
        
        page = button.click();
        HtmlElement element = page.getHtmlElementById("link");
        page = element.click();
        
        int newSize = webClient.getCookieManager().getCookies().size();
        
        assertTrue( newSize < currentSize );
    }
}
