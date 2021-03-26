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

package com.sun.faces.test.javaee6.resource.cacheLastMod.wartest;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.Page;
import static org.junit.Assert.*;

public class Issue2895IT {

    private String webUrl;
    private WebClient webClient;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

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
    public void testResourceCaching() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        
        Page initialPage = webClient.getPage(webUrl + "faces/index.xhtml");
        if (initialPage.getWebResponse().getStatusCode() == 200) {
            String cssUrl = webUrl + "faces/jakarta.faces.resource/styles.css";
            Page cssPage = webClient.getPage(cssUrl);
            assertEquals(200, cssPage.getWebResponse().getStatusCode());

            String ifModifiedSinceValue = cssPage.getWebResponse().getResponseHeaderValue("Last-Modified");
            if (ifModifiedSinceValue == null) {
                ifModifiedSinceValue = cssPage.getWebResponse().getResponseHeaderValue("Date");
            }

            webClient.getCache().clear();
            webClient.addRequestHeader("If-Modified-Since", ifModifiedSinceValue);
            webClient.addRequestHeader(("Cache-Control"), "max-age=0");
            cssPage = webClient.getPage(cssUrl);
            assertEquals(304, cssPage.getWebResponse().getStatusCode());
        }
    }
}
