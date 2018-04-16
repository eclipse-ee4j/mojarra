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

package com.sun.faces.test.weblogic.elbackwardcompatible;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_1_1;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_1_2;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_1_3;
import static org.junit.Assert.assertTrue;
import static com.sun.faces.test.junit.JsfVersion.JSF_2_3_0_M01;
import com.sun.faces.test.junit.JsfTest;

public class Issue3454IT {

    private String webUrl;
    private WebClient webClient;

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

    @Test
    @JsfTest(value = JSF_2_3_0_M01, excludes = { 
        WEBLOGIC_12_1_1,
        WEBLOGIC_12_1_2,
        WEBLOGIC_12_1_3
    })
    public void testBackwardCompatibleFlag() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
        HtmlPage page = webClient.getPage(webUrl + "faces/elBackwardCompatible.jspx");
        String pageXml = page.asXml();

        assertTrue(pageXml.contains("table:0:input"));
        assertTrue(pageXml.contains("table:1:input"));
        assertTrue(pageXml.contains("table:2:input"));        

        page = webClient.getPage(webUrl + "faces/elBackwardCompatible.jspx?isBackwardCompatible22=true&forceSetFlag=true");
        pageXml = page.asXml();

        assertTrue(pageXml.contains("table:0:input"));
        assertTrue(pageXml.contains("table:1:input"));
        assertTrue(pageXml.contains("table:2:input"));        
        
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        page = webClient.getPage(webUrl + "faces/elBackwardCompatible.jspx?isBackwardCompatible22=false&forceSetFlag=true");
        pageXml = page.asXml();

        assertTrue(!pageXml.contains("table:0:input"));
        assertTrue(!pageXml.contains("table:1:input"));
        assertTrue(!pageXml.contains("table:2:input"));               
    }
}
