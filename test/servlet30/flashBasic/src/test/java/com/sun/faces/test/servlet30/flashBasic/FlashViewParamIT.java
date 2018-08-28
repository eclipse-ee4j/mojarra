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

import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;
import com.sun.faces.test.junit.JsfVersion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
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

    /**
     * Added for issue 904.
     */
    @JsfTest(JsfVersion.JSF_2_2_1)
    @Test
    public void testBooleanCheckboxSubmittedValue() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/flash01.xhtml");
        HtmlButtonInput button = (HtmlButtonInput) page.getElementById("nextButton");
        page = button.click();
        assertTrue(page.asText().contains("foo = bar"));

        Cookie cookie = webClient.getCookieManager().getCookie("csfcfc");
        assertTrue(cookie.isHttpOnly());

        page = webClient.getPage(webUrl + "/faces/flash01.xhtml");
        HtmlAnchor link = (HtmlAnchor) page.getElementById("nextLink");
        page = link.click();
        assertTrue(page.asText().contains("foo = bar"));

        cookie = webClient.getCookieManager().getCookie("csfcfc");
        assertTrue(cookie.isHttpOnly());

        page = webClient.getPage(webUrl + "/faces/flash01.xhtml");
        link = (HtmlAnchor) page.getElementById("nextCommandLink");
        page = link.click();
        assertTrue(page.asText().contains("foo = bar"));

        cookie = webClient.getCookieManager().getCookie("csfcfc");
        assertTrue(cookie.isHttpOnly());

        page = webClient.getPage(webUrl + "/faces/flash01.xhtml");
        HtmlSubmitInput submitButton = (HtmlSubmitInput) page.getElementById("nextCommandButton");
        page = submitButton.click();
        assertTrue(page.asText().contains("foo = bar"));

        cookie = webClient.getCookieManager().getCookie("csfcfc");
        assertTrue(cookie.isHttpOnly());
    }
}
