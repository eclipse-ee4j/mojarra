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

public class RegressionsIT {

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
    public void testAreaTextRowsAttrTest() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/regression/AreaTextRowsAttrTest.jsp");
        assertTrue(Pattern.matches("(?s).*<html>\\s*<head>\\s*<title>\\s*Text\\s*Area\\s*Row\\s*Attribute\\s*Regression\\s*Test\\s*</title>\\s*</head>\\s*<body>\\s*<textarea\\s*name=\".*\"\\s*rows=\"30\">\\s*</textarea>\\s*</body>\\s*</html>.*", page.asXml()));
    }

    @Test
    public void testSelectOneManySizeAttrTest() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/regression/SelectOneManySizeAttrTest.jsp");
        assertTrue(Pattern.matches("(?s).*<html>\\s*<head>\\s*<title>\\s*Select.One,Many.ListBox\\s*Size\\s*Attribute\\s*Test\\s*</title>\\s*</head>\\s*<body>\\s*<select\\s*name=\".*\"\\s*size=\"5\">\\s*<option\\s*value=\"val1\">\\s*val1\\s*</option>\\s*</select>\\s*<select\\s*name=\".*\"\\s*multiple=\"multiple\"\\s*size=\"5\">\\s*<option\\s*value=\"val1\">\\s*val1\\s*</option>\\s*</select>\\s*</body>\\s*</html>.*", page.asXml()));
    }
}
