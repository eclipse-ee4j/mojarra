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

package com.sun.faces.test.servlet30.renderkit;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.junit.*;
import static org.junit.Assert.*;

public class Issue2767IT {

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
    public void testPassThroughAttributes() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/issue2767.xhtml");
        String pageMarkup = page.getBody().asXml();
        assertTrue(pageMarkup.contains("elname=\"/issue2767.xhtml\""));
        assertTrue(pageMarkup.contains("literalname=\"literalValue\""));
        assertTrue(!pageMarkup.contains("xmlns:p=\"http://xmlns.jcp.org/jsf/passthrough\""));
        assertTrue(pageMarkup.contains("foo=\"bar\""));
        assertTrue(page.asText().contains("This should show up"));
        assertTrue(!page.asText().contains("And this should not"));

    }
}
