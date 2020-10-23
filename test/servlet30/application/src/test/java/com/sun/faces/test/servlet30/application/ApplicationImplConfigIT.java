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

package com.sun.faces.test.servlet30.application;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ApplicationImplConfigIT {
    
    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testComponentPositive() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/appConfig.jsp");
    }

    @Test
    public void testComponentNegative() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/appConfig.jsp");
    }

    @Test
    public void testGetComponentTypes() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/appConfig.jsp");
    }

    @Test
    public void testConverterPositive() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/appConfig.jsp");
    }

    @Test
    public void testConverterNegative() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/appConfig.jsp");
    }

    @Test
    public void testGetConverterIds() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/appConfig.jsp");
    }

    @Test
    public void testValidatorPositive() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/appConfig.jsp");
    }

    @Test
    public void testValidatorNegative() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/appConfig.jsp");
    }

    @Test
    public void testGetValidatorIds() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/appConfig.jsp");
    }
}
