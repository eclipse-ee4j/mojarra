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

package com.sun.faces.test.servlet30.suppressxmldeclaration;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.After;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class SuppressXmlDeclIT {

    private final static Pattern XmlDeclaration = Pattern.compile("^<\\?xml.+?version=['\"](.+?)['\"](.+?encoding=['\"]((.+?))['\"])?.*?\\?>");

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
    public void testSuppressXmlDecl() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");
        InputStreamReader reader = new InputStreamReader(page.getWebResponse().getContentAsStream());
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = bufferedReader.readLine();
        while (0 == line.length()) {
            line = bufferedReader.readLine();
        }
        Matcher m = XmlDeclaration.matcher(line);
        assertFalse(m.find());

        page = webClient.getPage(webUrl + "faces/index.xhtml");
        String xml = page.asXml();
        assertTrue(xml.contains("javax.faces.ViewState"));
    }
}
