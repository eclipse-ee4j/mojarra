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

package com.sun.faces.test.servlet30.facelets;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Issue3168IT {

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
    public void testGraphicImageDirectContract() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/graphicImageDirectContract.xhtml");
        HtmlImage image = (HtmlImage) page.getHtmlElementById("image");
        assertTrue(image.getSrcAttribute().equals("RES_NOT_FOUND"));
    }
    
    @Test
    public void testGraphicImageDirectContract2() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/graphicImageDirectContract2.xhtml");
        HtmlImage image = (HtmlImage) page.getHtmlElementById("image");
        assertTrue(image.getSrcAttribute().equals("RES_NOT_FOUND"));
    }
    
    @Test
    public void testOutputStylesheetDirectContract() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/outputStylesheetDirectContract.xhtml");
        assertTrue(page.asXml().indexOf("RES_NOT_FOUND") != -1);
    }
    
    @Test
    public void testOutputScriptDirectContract() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/outputScriptDirectContract.xhtml");
        assertTrue(page.asXml().indexOf("RES_NOT_FOUND") != -1);
    }
}
