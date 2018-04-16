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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class PrefixSecurityIT {

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

    /*
     * Ensure a prefix mapped FacesServlet doesn't allow access to WEB-INF.
     */
    @Test
    public void testPrefixSecurity1() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/WEB-INF/test.jsp");
        assertEquals(404, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testPrefixSecurity2() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/WEB-INF");
        assertEquals(404, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testPrefixSecurity3() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/WEB-INF/");
        assertEquals(404, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testPrefixSecurity4() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/META-INF/MANIFEST.MF");
        assertEquals(404, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testPrefixSecurity5() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/META-INF");
        assertEquals(404, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testPrefixSecurity6() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/META-INF/");
        assertEquals(404, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testPrefixSecurity7() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/web-Inf/test.jsp");
        assertEquals(404, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testPrefixSecurity8() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/mEtA-InF/MANIFEST.MF");
        assertEquals(404, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }
}
