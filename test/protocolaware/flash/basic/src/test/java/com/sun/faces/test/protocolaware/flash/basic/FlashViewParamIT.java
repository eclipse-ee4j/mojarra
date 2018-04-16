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

package com.sun.faces.test.protocolaware.flash.basic;

import com.gargoylesoftware.htmlunit.util.Cookie;
import com.sun.faces.test.util.HttpUtils;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
  *
 */
public class FlashViewParamIT {

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


    // ------------------------------------------------------------ Test Methods


    /**
     * Added for issue 904.
     */
    @Test
    public void testBooleanCheckboxSubmittedValue() throws Exception {

        doTestBooleanCheckboxSubmittedValue(webUrl);
    }
    
    @Test 
    public void testSecure() throws Exception {
        webClient.getCookieManager().clearCookies();
        webClient.getOptions().setUseInsecureSSL(true);
        String httpsUrl = System.getProperty("integration.url.https");

        doTestBooleanCheckboxSubmittedValue(httpsUrl);
        
        HtmlPage page = webClient.getPage(httpsUrl + "/faces/flash01.xhtml");
        Cookie flashCookie = webClient.getCookieManager().getCookie("csfcfc");
        assertTrue(flashCookie.isSecure());
        
    }
    
    
    public void doTestBooleanCheckboxSubmittedValue(String url) throws Exception {

        HtmlPage page = webClient.getPage(url + "/faces/flash01.xhtml");
        HtmlButtonInput button = (HtmlButtonInput) page.getElementById("nextButton");
        page = button.click();
        assertTrue(page.asText().contains("foo = bar"));

        page = webClient.getPage(url + "/faces/flash01.xhtml");
        HtmlAnchor link = (HtmlAnchor) page.getElementById("nextLink");
        page = link.click();
        assertTrue(page.asText().contains("foo = bar"));

        page = webClient.getPage(url + "/faces/flash01.xhtml");
        link = (HtmlAnchor) page.getElementById("nextCommandLink");
        page = link.click();
        assertTrue(page.asText().contains("foo = bar"));

        page = webClient.getPage(url + "/faces/flash01.xhtml");
        HtmlSubmitInput submitButton = (HtmlSubmitInput) page.getElementById("nextCommandButton");
        page = submitButton.click();
        assertTrue(page.asText().contains("foo = bar"));

    }
    
}
