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

public class VerbatimIT {

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
    public void testVerbatimTest() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/verbatim_test.jsp");
        assertTrue(Pattern.matches("(?s).*\\s*<html>\\s*<head>\\s*<title>\\s*Test\\s*of\\s*the\\s*Verbatim\\s*Tag\\s*</title>\\s*</head>\\s*<body>\\s*<h1>\\s*Test\\s*of\\s*the\\s*Verbatim\\s*Tag\\s*</h1>\\s*<p>\\s*\\[DEFAULT\\]\\s*This\\s*text\\s*<b>\\s*has\\s*angle\\s*brackets\\s*</b>\\s*.\\s*The\\s*angle\\s*brackets\\s*MUST\\s*NOT\\s*be\\s*escaped.\\s*</p>\\s*<p>\\s*\\[FALSE\\]\\s*This\\s*text\\s*<b>\\s*has\\s*angle\\s*brackets\\s*</b>\\s*.\\s*The\\s*angle\\s*brackets\\s*MUST\\s*NOT\\s*be\\s*escaped.\\s*</p>\\s*<p>\\s*\\[TRUE\\]\\s*This\\s*text\\s*&lt;b&gt;has\\s*angle\\s*brackets&lt;/b&gt;.\\s*The\\s*angle\\s*brackets\\s*MUST\\s*be\\s*escaped.\\s*</p>\\s*<p>\\s*This\\s*text\\s*is\\s*rendered.\\s*</p>\\s*</body>\\s*</html>.*", page.asXml()));
    }

    @Test
    public void testEscapeTest() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/escape_test.jsp");
        assertTrue(Pattern.matches("(?s).*<html>\\s*<head>\\s*<title>\\s*Test\\s*of\\s*outputText\\s*Escaping\\s*</title>\\s*</head>\\s*<body>\\s*<h1>\\s*Test\\s*of\\s*outputText\\s*Escaping\\s*</h1>\\s*<p>\\s*\\[DEFAULT\\]\\s*This\\s*text\\s*&lt;b&gt;has\\s*angle\\s*brackets&lt;/b&gt;.\\s*The\\s*angle\\s*brackets\\s*MUST\\s*be\\s*escaped.\\s*</p>\\s*<p>\\s*\\[FALSE\\]\\s*This\\s*text\\s*<b>\\s*has\\s*angle\\s*brackets\\s*</b>\\s*.\\s*The\\s*angle\\s*brackets\\s*MUST\\s*NOT\\s*be\\s*escaped.\\s*</p>\\s*<p>\\s*\\[TRUE\\]\\s*This\\s*text\\s*&lt;b&gt;has\\s*angle\\s*brackets&lt;/b&gt;.\\s*The\\s*angle\\s*brackets\\s*MUST\\s*be\\s*escaped.\\s*</p>\\s*</body>.*</html>.*", page.asXml()));
    }
}
