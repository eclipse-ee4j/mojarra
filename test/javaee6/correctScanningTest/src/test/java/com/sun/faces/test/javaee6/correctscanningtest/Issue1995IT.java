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

package com.sun.faces.test.javaee6.correctscanningtest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_1_4;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_2_1;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;
import static com.sun.faces.test.junit.JsfVersion.JSF_2_3_0;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class Issue1995IT {

    private String webUrl;
    private WebClient webClient;

    /*
     * The webUrl is http://localhost:8080/test-javaee6web-correctScanningWar/
     * and we strip off the / here so we can later use it to write the 2 tests
     * below to point to the 2 web applications in the EAR.
     */
    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webUrl = webUrl.substring(0, webUrl.length() - 1);
        webClient = new WebClient();
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    @JsfTest(value = JSF_2_3_0, excludes = {WEBLOGIC_12_1_4, WEBLOGIC_12_2_1})
    @Test
    public void testScanning1() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "1/faces/index.xhtml");
        assertTrue(page.asXml().contains("javax.faces.ViewState"));
        assertTrue(page.asText().matches("(?s).*.war_1\\s+bean:\\s+war1Bean\\s+war_2\\s+bean:\\s+bean:\\s+bar..*"));
    }

    @JsfTest(value = JSF_2_3_0, excludes = {WEBLOGIC_12_1_4, WEBLOGIC_12_2_1})
    @Test
    public void testScanning2() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "2/faces/index.xhtml");
        assertTrue(page.asXml().contains("javax.faces.ViewState"));
        assertTrue(page.asText().matches("(?s).*.war_1\\s+bean:\\s+war_2\\s+bean:\\s+war2Bean\\s+bean:\\s+bar..*"));
    }
}
