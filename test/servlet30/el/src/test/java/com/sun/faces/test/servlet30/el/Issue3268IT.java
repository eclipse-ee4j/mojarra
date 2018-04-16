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

package com.sun.faces.test.servlet30.el;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Issue3268IT {

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
    public void testImplicitObjects() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/getFeatureDescriptors.xhtml");
        String pageXml = page.asXml();
        assertTrue(pageXml.indexOf("application") != -1);
        assertTrue(pageXml.indexOf("applicationScope") != -1);
        assertTrue(pageXml.indexOf("cc") != -1);
        assertTrue(pageXml.indexOf("component") != -1);
        assertTrue(pageXml.indexOf("cookie") != -1);
        assertTrue(pageXml.indexOf("facesContext") != -1);
        assertTrue(pageXml.indexOf("flash") != -1);
        assertTrue(pageXml.indexOf("flowScope") != -1);
        assertTrue(pageXml.indexOf("view") != -1);
        assertTrue(pageXml.indexOf("header") != -1);
        assertTrue(pageXml.indexOf("headerValues") != -1);
        assertTrue(pageXml.indexOf("initParam") != -1);
        assertTrue(pageXml.indexOf("param") != -1);
        assertTrue(pageXml.indexOf("paramValues") != -1);
        assertTrue(pageXml.indexOf("request") != -1);
        assertTrue(pageXml.indexOf("requestScope") != -1);
        assertTrue(pageXml.indexOf("resource") != -1);
        assertTrue(pageXml.indexOf("session") != -1);
        assertTrue(pageXml.indexOf("sessionScope") != -1);
        assertTrue(pageXml.indexOf("viewScope") != -1);
    }
}
