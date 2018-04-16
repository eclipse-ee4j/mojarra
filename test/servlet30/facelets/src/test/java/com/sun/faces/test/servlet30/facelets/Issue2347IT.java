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
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Issue2347IT {

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

    /**
     * Test the action listener with a param. This uses the EL expression that
     * is defined on the Facelet directly.
     *
     * @throws Exception
     */
    @Test
    public void testActionListener1() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/actionlistener.xhtml");
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("form:buttonParam");
        page = button.click();
        assertTrue(page.asText().contains("Listener invoked: true"));
    }

    /**
     * Test the action listener without a param. This is the main use case for
     * issue 2347, and depends on the new method expression that is being
     * created based on the one defined on the Facelet.
     *
     * @throws Exception
     */
    @Test
    public void testActionListener2() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/actionlistener.xhtml");
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("form:buttonNoParam");
        page = button.click();
        assertTrue(page.asText().contains("Listener invoked: true"));
    }
}
