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

package com.sun.faces.test.javaee7.cdimultitenantsetstccl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import static org.junit.Assert.assertTrue;

public class Issue3341IT {
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
     * 
     * @throws Exception when a serious error occurs.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    /**
     * Cleanup after testing.
     * 
     * @throws Exception when a serious error occurs.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Setup before testing.
     */
    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
    }

    /**
     * Tear down after testing.
     */
    @After
    public void tearDown() {
        webClient.close();
    }

    // Enable when running against a GlassFish that has 19296686 fixed
    @Test
    public void testTCCLReplacementResilience() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);

        String pageText = page.getBody().asText();
        
        // If the BeforeFilter is configured to 
        if (pageText.matches("(?s).*SUCCESS.*")) {
            assertTrue(true);
        } else {        
            assertTrue(pageText.matches("(?s).*Duke.*submit.*"));
            assertTrue(pageText.matches("(?s).*First name:\\s*Duke.*"));
            assertTrue(pageText.matches("(?s).*BeforeServlet init found Lifecycle:\\s*TRUE.*"));
            assertTrue(pageText.matches("(?s).*BeforeServlet init found FacesContext:\\sTRUE.*"));
            assertTrue(pageText.matches("(?s).*BeforeServlet request found Lifecycle:\\s*TRUE.*"));
            // Yes, the FacesContext.getCurrentInstance() should not be found 
            // because this is in a Filter before the run of the FacesServlet.service().
            assertTrue(pageText.matches("(?s).*BeforeServlet request found FacesContext:\\s*FALSE.*"));        
        }
    }
}
