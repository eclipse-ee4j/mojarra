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

package com.sun.faces.test.servlet30.facelets;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Issue2209IT {

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
    public void testOffset() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/repeatOffset.xhtml");
        assertTrue(page.asText().contains("Offset VE:str2 Offset VE:str3 Offset VE:str4 Offset VE:str5 Offset VE:str6 Offset VE:str7 Offset VE:str8 Offset VE:str9"));
    }

    @Test
    public void testOffset2() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/repeatOffset.xhtml");
        assertTrue(page.asText().contains("Offset Literal:str2 Offset Literal:str3 Offset Literal:str4 Offset Literal:str5 Offset Literal:str6 Offset Literal:str7 Offset Literal:str8 Offset Literal:str9"));
    }
}
