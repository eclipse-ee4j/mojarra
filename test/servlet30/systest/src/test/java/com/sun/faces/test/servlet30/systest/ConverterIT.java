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

package com.sun.faces.test.servlet30.systest;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.util.regex.Pattern;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConverterIT {

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

    /*
     * Test Converter creation
     */
    @Test
    public void testConverter() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/converter.jsp");
        assertTrue(Pattern.matches("(?s).*/converter.jsp PASSED.*", page.asXml()));
    }

    @Test
    public void testConverter01() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/converter01.jsp");
        assertTrue(Pattern.matches("(?s).*/converter01.jsp PASSED.*", page.asXml()));
    }

    @Test
    public void testConverter02() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/converter02.jsp");
        assertTrue(Pattern.matches("(?s).*<html>\\s*<head/>\\s*<body>\\s*<span\\s*id=\"id1\">\\s*\\$123\\.45\\s*</span>\\s*</body>\\s*</html>.*", page.asXml()));
    }

    @Test
    public void testConverter03() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/converter03.jsp");
        assertEquals(200, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testConverter04() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/converter04.jsp");
        assertEquals(200, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testEnumConverter1() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/enum-converter-1.jsp");
        assertTrue(Pattern.matches("(?s).*/enum-converter-1.jsp PASSED.*", page.asXml()));
    }
}
