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

package com.sun.faces.test.servlet30.customstatemanager;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

@Ignore("Needs to be updated for EL Resolver and JSP")
public class ReplaceStateManagerIT {

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
    public void testReplaceStateManager() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/test.jsp");
        assertTrue(-1 != page.asText().indexOf("New String Value"));
        assertTrue(-1 != page.asText().indexOf("com.sun.faces.test.servlet30.customstatemanager.NewStateManager"));
        assertTrue(-1 != page.asText().indexOf("com.sun.faces.test.servlet30.customstatemanager.NewViewHandler"));
        assertTrue(-1 != page.asText().indexOf("com.sun.faces.test.servlet30.customstatemanager.NewApplication"));
    }
}
