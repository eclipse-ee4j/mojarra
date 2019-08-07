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

package com.sun.faces.test.javaee7.cdiinitdestroyevent;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;
import static com.sun.faces.test.junit.JsfVersion.JSF_2_2_0;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class Issue2948IT {

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

    @JsfTest(value = JSF_2_2_0)
    @Test
    public void testSessionLogging() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlElement e = (HtmlElement) page.getElementById("initMessage");
        long sessionInitTime = Long.valueOf(e.asText());
        HtmlSubmitInput invalidateButton = (HtmlSubmitInput) page.getElementById("invalidateSession");
        page = invalidateButton.click();
        e = (HtmlElement) page.getElementById("destroyMessage");
        long sessionDestroyTime = Long.valueOf(e.asText());
        assertTrue(sessionInitTime < sessionDestroyTime);
    }

    @JsfTest(value = JSF_2_2_0)
    @Test
    @Ignore
    public void testFlowLogging() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        HtmlSubmitInput enterFlow = (HtmlSubmitInput) page.getElementById("enterFlow");
        page = enterFlow.click();
        HtmlElement e = (HtmlElement) page.getElementById("initMessage");
        long flowInitTime = Long.valueOf(e.asText());
        HtmlSubmitInput next = (HtmlSubmitInput) page.getElementById("a");
        page = next.click();
        HtmlSubmitInput returnButton = (HtmlSubmitInput) page.getElementById("return");
        page = returnButton.click();
        e = (HtmlElement) page.getElementById("destroyMessage");
        long flowDestroyTime = Long.valueOf(e.asText());
        assertTrue(flowInitTime < flowDestroyTime);
    }

    @JsfTest(value = JSF_2_2_0)
    @Test
    @Ignore
    public void testViewScopedLogging() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/viewScoped01.xhtml");
        HtmlElement e = (HtmlElement) page.getElementById("initMessage");
        long flowInitTime = Long.valueOf(e.asText());
        HtmlSubmitInput returnButton = (HtmlSubmitInput) page.getElementById("viewScoped02");
        page = returnButton.click();
        e = (HtmlElement) page.getElementById("destroyMessage");
        long flowDestroyTime = Long.valueOf(e.asText());
        assertTrue(flowInitTime < flowDestroyTime);
    }
}
