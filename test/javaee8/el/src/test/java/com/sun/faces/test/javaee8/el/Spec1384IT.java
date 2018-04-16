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

package com.sun.faces.test.javaee8.el;

import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_1_4;
import static com.sun.faces.test.junit.JsfVersion.JSF_2_3_0_M03;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;

/**
 * Tests both build time and render time availability of the correct #{component}
 * value.
 *
 * Expected rendered output:
 *
 *       id1-1:component1
 *       id2-1:componentA
 *       id1-2:component1
 *       
 *       id1-3:component2
 *       id2-2:componentA
 *       id1-4:component2

 * component1 wraps componentA, so this tests that inside component1
 * but before componentA, #{component} refers to component1. The implicitly
 * created Facelets component (UIInstructions) are known not to alter #{component}.
 * Inside componentA, #{component} needs to change to componentA, and directly outside
 * componentA but still inside component1 it needs to change back to component1.
 * 
 * This therefore tests that #{component} is able to change to deeper stacked components
 * and able to move back when that stack unwinds.
 *
 * The test is repeated to test that the same changes are possible at the top level.
 *
 */
@RunWith(JsfTestRunner.class)
public class Spec1384IT {

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
    @JsfTest(value = JSF_2_3_0_M03, excludes = { WEBLOGIC_12_1_4 })
    public void testCompositeComponentFacelets() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/component.xhtml");
        
        String pageAsXml = page.asXml();
        
        int id11 = pageAsXml.indexOf("id1-1:component1");
        int id21 = pageAsXml.indexOf("id2-1:componentA");
        int id12 = pageAsXml.indexOf("id1-2:component1");
        
        int id13 = pageAsXml.indexOf("id1-3:component2");
        int id22 = pageAsXml.indexOf("id2-2:componentA");
        int id14 = pageAsXml.indexOf("id1-4:component2");
        
        assertTrue(
            id11 > -1 &&
            id21 > id11 &&
            id12 > id21 &&
            id13 > id12 &&
            id22 > id13 &&
            id14 > id22
         );
    }

}
