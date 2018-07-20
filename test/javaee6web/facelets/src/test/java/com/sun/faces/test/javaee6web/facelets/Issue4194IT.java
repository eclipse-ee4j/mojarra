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

package com.sun.faces.test.javaee6web.facelets;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Issue4194IT {

    private String webUrl;
    private WebClient webClient;
    private List<String> receivedAlerts;
    private List<String> expectedAlerts;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient(BrowserVersion.INTERNET_EXPLORER);
        receivedAlerts = new ArrayList<>();
        webClient.setAlertHandler(new CollectingAlertHandler(receivedAlerts));
        expectedAlerts = new ArrayList<>();
        expectedAlerts.add("static onSubmit triggered");
        expectedAlerts.add("static onSubmit triggered");
        expectedAlerts.add("dynamic onSubmit triggered");
        expectedAlerts.add("dynamic onSubmit triggered");
    }

    @Test
    public void testIssue4194() throws Exception {

        HtmlPage page = webClient.getPage(webUrl + "faces/issue4194.xhtml");
        HtmlElement he = page.getHtmlElementById("staticOnSubmitForm:AAA");
        he.click();

        page = webClient.getPage(webUrl + "faces/issue4194.xhtml");
        he = page.getHtmlElementById("staticOnSubmitForm:BBB");
        he.click();

        page = webClient.getPage(webUrl + "faces/issue4194.xhtml");
        he = page.getHtmlElementById("dynamicOnSubmitForm:AAA");
        he.click();

        page = webClient.getPage(webUrl + "faces/issue4194.xhtml");
        he = page.getHtmlElementById("dynamicOnSubmitForm:BBB");
        he.click();

        assertEquals(expectedAlerts, receivedAlerts);
    }

    @After
    public void tearDown() {
        webClient.close();
    }

}
