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

package com.sun.faces.test.servlet30.navigation2;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NavigationHandlerIT {

    /**
     * Stores the web URL.
     */
    private String webUrl;
    /**
     * Stores the web client.
     */
    private WebClient webClient;

    /**
     * Setup before testing.
     */
    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    /**
     * Tear down after testing.
     */
    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testNavigationHandler() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/navHandler.xhtml");
    }

    @Test
    public void testSimilarFromViewId() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/similarFromViewId.xhtml");
    }

    @Test
    public void testSeparateRule() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/separateRule.xhtml");
    }

    @Test
    public void testWrappedNavigationHandler() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/wrappedNavHandler.xhtml");

    }

    @Test
    public void testRedirectParameters() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/redirectParams.xhtml");
    }
}
