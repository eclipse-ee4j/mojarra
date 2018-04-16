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

package com.sun.faces.test.servlet30.composite;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Integration tests verifying the workings of cc:renderFacet.
 *
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
public class RenderFacetIT {

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
    public void testRenderFacet1() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/renderFacet/renderFacet1.xhtml");
        assertTrue(page.getBody().asText().indexOf("This came from a rendered facet") != -1);
    }

    @Test
    public void testRenderFacet2() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/renderFacet/renderFacet2.xhtml");
        assertTrue(page.getBody().asText().indexOf("myFacet Text") != -1);
    }
}
