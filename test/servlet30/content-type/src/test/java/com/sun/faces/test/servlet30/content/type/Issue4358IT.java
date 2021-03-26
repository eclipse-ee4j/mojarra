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

package com.sun.faces.test.servlet30.content.type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.*;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import org.junit.Ignore;

@Ignore
public class Issue4358IT {

    /**
     * Stores the web URL.
     */
    private String webUrl;
    /**
     * Stores the web client.
     */
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

    // ------------------------------------------------------------ Test Methods

    @Test
    public void testCorrectContentTypeSetBeforeGetResponseOutputWriterCalled() throws Exception {
        testCorrectContentTypeSetBeforeGetResponseOutputWriterCalled(null);
        testCorrectContentTypeSetBeforeGetResponseOutputWriterCalled("issue4358AjaxButton");
        testCorrectContentTypeSetBeforeGetResponseOutputWriterCalled("issue4358AjaxExecuteAllButton");
        testCorrectContentTypeSetBeforeGetResponseOutputWriterCalled("issue4358NonAjax");
    }

    private void testCorrectContentTypeSetBeforeGetResponseOutputWriterCalled(String buttonId) throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/issue4358.xhtml");

        if (buttonId != null) {

            HtmlInput issue4340Button = (HtmlInput) page.getElementById("form:" + buttonId);
            issue4340Button.click();

            webClient.waitForBackgroundJavaScript(60000);
        }

        DomElement result = page.getElementById("issue4358Result");
        assertNotNull(result);
        assertEquals("SUCCESS", result.asText());
    }
}