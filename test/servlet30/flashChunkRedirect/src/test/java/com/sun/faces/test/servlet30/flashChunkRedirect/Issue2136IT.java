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
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * Integration tests for issue #2136
 *
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
public class Issue2136IT {

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
        HtmlPage page = webClient.getPage(webUrl + "faces/issue2136/issue2136.xhtml");
        HtmlInput input = (HtmlInput) page.getElementById("form:input");
        input.type("12345");
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getElementById("form:submit");
        page = submit.click();
        assertTrue(page.getBody().asText().indexOf("12345") != -1);
    }
}
