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

package com.sun.faces.test.servlet30.customlifecycle;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>
 * Replace the LifecycleFactory with an impl that knows about a new LifecycleId
 * that is passed to the app as a ServletContext init parameter. Make sure that
 * a phaseListener still gets called with a replaced lifecycle id.</p>
 */
public class ReplaceLifecycleIT {

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
    public void testReplaceLifecycle() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/test.jsp");
        String pageText = page.asText();
        assertTrue(-1 != pageText.indexOf("beforePhase"));
        // Ensure the phaseListener is only called once.
        assertTrue(!pageText.matches("(?s).*beforePhase.*beforePhase.*"));

    }

    @Test
    public void testAlternateLifecycle() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "alternate/test2.jsp");
        assertTrue(-1 != page.asText().indexOf("beforePhase"));
        assertTrue(-1 != page.asText().indexOf("AlternateLifecycle"));
        page = webClient.getPage(webUrl + "faces/test2.jsp");
        assertTrue(-1 != page.asText().indexOf("beforePhase"));
        assertTrue(-1 != page.asText().indexOf("NewLifecycle"));
    }
}
