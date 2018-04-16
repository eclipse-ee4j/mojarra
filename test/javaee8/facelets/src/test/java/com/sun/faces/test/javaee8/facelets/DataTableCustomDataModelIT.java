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

package com.sun.faces.test.javaee8.facelets;

import static com.sun.faces.test.junit.JsfVersion.JSF_2_3_0_M03;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_1_4;
import static java.util.regex.Pattern.matches;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_2_1;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;

@RunWith(JsfTestRunner.class)
public class DataTableCustomDataModelIT {

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
    @JsfTest(value = JSF_2_3_0_M03,
            excludes = {WEBLOGIC_12_1_4, WEBLOGIC_12_2_1})
    public void testExactClassMatch() throws Exception {

        // In this test a backing bean will return an object of type Child11.
        // There's a DataModel registered for exactly this class, which should
        // be picked up. 
        //
        // The (small) challenge is that there are also DataModels
        // registered for super classes of Child11 (e.g. Child1), which can also
        // handle a Child11, but these should NOT be picked up and the exact match
        // should be preferred.
        HtmlPage page = webClient.getPage(webUrl + "datatableCustomDataModel11.xhtml");
        assertTrue(matches("(?s).*START.*11-member 1.*11-member 2.*END.*", page.asXml()));
    }

    @Test
    @JsfTest(value = JSF_2_3_0_M03,
            excludes = {WEBLOGIC_12_1_4, WEBLOGIC_12_2_1})
    public void testClosestSuperClassMatch() throws Exception {

        // In this test a backing bean will return an object of type Child111.
        // There's NO DataModel registered for exactly this class. However, there
        // is a DataModel registered for several super classes, which can all
        // handle a Child111.
        // The challenge here is that the DataModel for the closest super class
        // should be chosen, which in this test is the DataModel that handles
        // a Child11.
        HtmlPage page = webClient.getPage(webUrl + "datatableCustomDataModel111.xhtml");
        assertTrue(matches("(?s).*START.*111-member 1.*111-member 2.*END.*", page.asXml()));
    }
}
