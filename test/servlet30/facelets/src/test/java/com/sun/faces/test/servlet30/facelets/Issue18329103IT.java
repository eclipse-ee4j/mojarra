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
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.sun.faces.test.junit.JsfTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class Issue18329103IT {

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
    public void testCifRendered() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/cifRenderedFalse.xhtml");
        String pageXml = page.asXml();
        assertTrue(!pageXml.contains("CustomComponentHandler.apply() called"));
        
        HtmlCheckBoxInput rendered = (HtmlCheckBoxInput) page.getElementById("checkbox");
        rendered.setChecked(true);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();
        
        pageXml = page.asXml();
        assertTrue(pageXml.contains("CustomComponentHandler.apply() called"));
        
        rendered = (HtmlCheckBoxInput) page.getElementById("checkbox");
        rendered.setChecked(false);
        
        button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();

        // We need an extra click if partialStateSaving=false because
        // the facelets are applied to build the view before restore
        // in that case.
        button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();
        
        pageXml = page.asXml();
        assertTrue(!pageXml.contains("CustomComponentHandler.apply() called"));
        
    }
}
