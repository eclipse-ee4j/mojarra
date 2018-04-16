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

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;
import com.sun.faces.test.junit.JsfVersion;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class Spec758IT {

    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
    }

    @Test
    public void testRedirect1a() throws Exception {
        webClient.getOptions().setRedirectEnabled(false);
        HtmlPage page;
        boolean exceptionThrown = false;

        try {
            webClient.getPage(webUrl + "faces/viewActionRedirect1a.xhtml");
        } catch (FailingHttpStatusCodeException ex) {
            assertEquals(302, ex.getStatusCode());
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        webClient.getOptions().setRedirectEnabled(true);
        page = webClient.getPage(webUrl + "faces/viewActionRedirect1a.xhtml");
        assertTrue(page.asText().contains("Result page"));
    }

    @JsfTest(JsfVersion.JSF_2_2_1)
    @Test
    public void testRedirect1b() throws Exception {
        webClient.getOptions().setRedirectEnabled(false);
        HtmlPage page;
        boolean exceptionThrown = false;

        try {
            webClient.getPage(webUrl + "faces/viewActionRedirect1b.xhtml");
        } catch (FailingHttpStatusCodeException ex) {
            assertEquals(302, ex.getStatusCode());
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        webClient.getOptions().setRedirectEnabled(true);
        page = webClient.getPage(webUrl + "faces/viewActionRedirect1b.xhtml");
        assertTrue(page.asText().contains("Result page"));
    }

    @JsfTest(JsfVersion.JSF_2_2_5)
    @Test
    public void testActionListener1a() throws Exception {
        webClient.getOptions().setRedirectEnabled(true);
        HtmlPage page;

        page = webClient.getPage(webUrl + "faces/viewActionActionListener1a.xhtml");
        DomElement e = page.getElementById("result");
        assertTrue(e.asText().contains("1 viewAction1 2 viewAction1"));
    }

    @JsfTest(JsfVersion.JSF_2_2_5)
    @Test
    public void testActionListener1b() throws Exception {
        webClient.getOptions().setRedirectEnabled(true);
        HtmlPage page;

        page = webClient.getPage(webUrl + "faces/viewActionActionListener1b.xhtml");
        DomElement e = page.getElementById("result");
        assertTrue(e.asText().contains("1 viewAction1 2 viewAction1"));
    }

    @JsfTest(JsfVersion.JSF_2_2_5)
    @Test
    public void testActionListener2a() throws Exception {
        webClient.getOptions().setRedirectEnabled(true);
        HtmlPage page;

        page = webClient.getPage(webUrl + "faces/viewActionActionListener2a.xhtml");
        DomElement e = page.getElementById("result");
        assertTrue(e.asText().contains("method viewAction1"));
    }

    @JsfTest(JsfVersion.JSF_2_2_5)
    @Test
    public void testActionListener2b() throws Exception {
        webClient.getOptions().setRedirectEnabled(true);
        HtmlPage page;

        page = webClient.getPage(webUrl + "faces/viewActionActionListener2b.xhtml");
        DomElement e = page.getElementById("result");
        assertTrue(e.asText().contains("method viewAction1"));
    }

    @Test
    public void testActionPageA() throws Exception {
        webClient.getOptions().setRedirectEnabled(false);
        HtmlPage page = null;
        boolean exceptionThrown = false;

        try {
            page = webClient.getPage(webUrl + "faces/viewActionActionPageA.xhtml");
        } catch (FailingHttpStatusCodeException ex) {
            assertEquals(302, ex.getStatusCode());
            exceptionThrown = true;
        }
        assertFalse(exceptionThrown);
        assertTrue(page.asText().contains("pageA action"));
    }

    @Test
    public void testActionEmpty() throws Exception {
        webClient.getOptions().setRedirectEnabled(false);
        HtmlPage page = null;
        boolean exceptionThrown = false;

        try {
            page = webClient.getPage(webUrl + "faces/viewActionActionEmpty.xhtml");
        } catch (FailingHttpStatusCodeException ex) {
            assertEquals(302, ex.getStatusCode());
            exceptionThrown = true;
        }
        assertFalse(exceptionThrown);
        assertTrue(page.asText().contains("pageA empty"));
    }

    @Test
    public void testActionNull() throws Exception {
        webClient.getOptions().setRedirectEnabled(false);
        HtmlPage page = null;
        boolean exceptionThrown = false;

        try {
            page = webClient.getPage(webUrl + "faces/viewActionActionNull.xhtml");
        } catch (FailingHttpStatusCodeException ex) {
            assertEquals(302, ex.getStatusCode());
            exceptionThrown = true;
        }
        assertFalse(exceptionThrown);
        assertTrue(page.asText().contains("pageA null"));
    }

    @Test
    public void testNegativeIntentionalInfiniteRedirect() throws Exception {
        webClient.getOptions().setRedirectEnabled(false);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
        boolean exceptionThrown = false;

        try {
            webClient.getPage(webUrl + "faces/viewActionActionExplicitRedirect.xhtml");
        } catch (FailingHttpStatusCodeException ex) {
            assertEquals(302, ex.getStatusCode());
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);

        webClient.getOptions().setRedirectEnabled(true);
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        try {
            webClient.getPage(webUrl + "faces/viewActionActionExplicitRedirect.xhtml");
        } catch (FailingHttpStatusCodeException ex) {
            assertEquals(302, ex.getStatusCode());
            exceptionThrown = true;
        }
        assertTrue(exceptionThrown);
    }
}
