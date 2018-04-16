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

package com.sun.faces.test.javaee8.cdi;

import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_1_4;
import static com.sun.faces.test.junit.JsfVersion.JSF_2_3_0_M03;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.faces.context.Flash;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_2_1;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;

/**
 * Tests the availability of The Flash via injection of a {@link Flash}
 * instance.
 *
 */
@RunWith(JsfTestRunner.class)
public class Spec1385IT {

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
    @JsfTest(value = JSF_2_3_0_M03, excludes = {WEBLOGIC_12_1_4, WEBLOGIC_12_2_1})
    public void testInjectFlash() throws Exception {

        // Renders nothing of interest, should cause cookie to be set
        webClient.getPage(webUrl + "faces/injectFlash.xhtml?setFlash=true");

        // Next request processes cookie, should render value put in Flash and set "deletion" cookie
        HtmlPage page = webClient.getPage(webUrl + "faces/injectFlash.xhtml?getFlash=true");

        assertTrue(page.asXml().contains("foo:bar"));

        // No cookie anymore and the value put in Flash previously should not be rendered anymore
        page = webClient.getPage(webUrl + "faces/injectFlash.xhtml?getFlash=true");

        assertFalse(page.asXml().contains("foo:bar"));
    }

}
