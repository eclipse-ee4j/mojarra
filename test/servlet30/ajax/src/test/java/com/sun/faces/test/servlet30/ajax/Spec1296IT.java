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

package com.sun.faces.test.servlet30.ajax;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Spec1296IT {

    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(60000);
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testPartialResponseWriterOutsideFacesServlet() throws Exception {
        Page page = webClient.getPage(webUrl + "BeforeFilter");
        String pageXml = page.getWebResponse().getContentAsString("UTF-8");
        assertTrue(pageXml.matches(
                "(?s).*<\\?xml\\s+version=\\'1\\.0\\'\\s+encoding=\\'UTF-8\\'\\?>\\s*<partial-response>\\s*<changes>\\s*<update\\s+id=\\\"foo\\\">\\s*<\\!\\[CDATA\\[\\s*\\]]>\\s*</update>\\s*</changes>\\s*</partial-response>.*"));
    }
}
