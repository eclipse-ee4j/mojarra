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

package com.sun.faces.test.servlet30.flashChunkRedirect;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Integration tests for issue #2332
 *
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
public class Issue2332IT {

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
    public void testFlashChunkingLink1() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");
        HtmlElement link = page.getHtmlElementById("form:link1");
        assertNotNull("Unable to find form:link1 element", link);
        page = link.click();
        webClient.waitForBackgroundJavaScript(60000);
        assertTrue(page.getBody().asText().indexOf("== Flash value LINK1 ==") != -1);
    }

    @Test
    public void testFlashChunkingLink2() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");
        HtmlElement link = page.getHtmlElementById("form:link2");
        assertNotNull("Unable to find form:link2 element", link);
        page = link.click();
        assertTrue(page.getBody().asText().indexOf("== Flash value LINK2 ==") != -1);
    }
}
