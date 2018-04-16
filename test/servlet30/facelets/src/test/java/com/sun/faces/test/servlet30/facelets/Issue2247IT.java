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
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Issue2247IT {

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
    public void testNoELEvaluation() throws Exception {

        HtmlPage page = webClient.getPage(webUrl + "faces/viewParam.xhtml");

        HtmlTextInput textField = (HtmlTextInput) page.getElementById("input");
        textField.setValueAttribute("#{4+5}");
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("implicitNavigationButton");
        page = button.click();
        
        assertTrue(!page.asText().contains("9"));

        textField = (HtmlTextInput) page.getElementById("input");
        textField.setValueAttribute("#{12+1}");
        button = (HtmlSubmitInput) page.getElementById("explicitNavigationButton");
        page = button.click();
        
        String text = page.asText();
        assertTrue(!text.contains("13"));
        assertTrue(text.contains("6"));
    }
}
