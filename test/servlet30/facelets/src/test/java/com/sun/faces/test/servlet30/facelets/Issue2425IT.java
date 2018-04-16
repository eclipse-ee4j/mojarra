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
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class Issue2425IT {

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
    public void testSize() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/repeatSize.xhtml");
        assertTrue(page.asText().contains("lstr0 lstr1 lstr2 lstr3 lstr4"));
        assertTrue(page.asText().contains("vstr0 vstr1 vstr2 vstr3 vstr4"));
        assertTrue(page.asText().contains("pstr0 pstr1 pstr2 pstr3 pstr4"));
        assertTrue(page.asText().contains("mstr0 mstr1 mstr2 mstr3 mstr4 mstr5 mstr6 mstr7 mstr8 mstr9"));
        assertTrue(page.asText().contains("ostr2 ostr3 ostr4"));
    }
}
