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

public class ResourceBundleELResolverIT {

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
    public void testResourceBundle01() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/resourceBundle01.jsp");
        assertTrue(Pattern.matches("(?s).*Expression\\s*in\\s*raw\\s*JSP\\s*page\\s*is\\s*Value\\s*From\\s*ResourceBundle\\..*",
                page.asXml()));
    }

    @Test
    public void testResourceBundle02() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/resourceBundle02.jsp");
        assertTrue(
                Pattern.matches("(?s).*Value\\s*from\\s*Faces\\s*component\\s*is\\s*Value\\s*From\\s*ResourceBundle\\..*", page.asXml()));
    }

    @Test
    public void testResourceBundle03() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/resourceBundle03.jsp");
        assertTrue(Pattern.matches(
                "(?s).*Values\\s*from\\s*Components:.*resourceBundle01:.*Value\\s*From\\s*ResourceBundle.*non\\s*existing\\s*resourceBundle:\\s*.br/.\\s*resourceBundle03:\\s*Value\\s*from\\s*ResourceBundle03.*",
                page.asXml()));
    }
}
