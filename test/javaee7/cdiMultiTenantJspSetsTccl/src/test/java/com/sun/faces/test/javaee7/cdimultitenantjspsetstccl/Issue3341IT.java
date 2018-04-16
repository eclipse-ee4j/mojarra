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

package com.sun.faces.test.javaee7.cdimultitenantjspsetstccl;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import static com.sun.faces.test.junit.JsfServerExclude.GLASSFISH_3_1_2_2;
import static com.sun.faces.test.junit.JsfServerExclude.GLASSFISH_4_0;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_1_1;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_1_2;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_1_3;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;
import static com.sun.faces.test.junit.JsfVersion.JSF_2_2_0;
import static org.junit.Assert.assertTrue;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
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

    @JsfTest(value=JSF_2_2_0, excludes={GLASSFISH_3_1_2_2, GLASSFISH_4_0, WEBLOGIC_12_1_1, WEBLOGIC_12_1_2, WEBLOGIC_12_1_3})
    @Test
    public void testCDIWithTCCL() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);

        assertTrue(page.getBody().asText().indexOf("SUCCESS") != -1);
        
    }
}
