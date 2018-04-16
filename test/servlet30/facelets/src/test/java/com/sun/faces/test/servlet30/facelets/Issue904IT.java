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
import com.gargoylesoftware.htmlunit.html.HtmlImage;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Issue904IT {

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
    public void testSelectBooleanCheckboxSubmittedValue() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/selectBooleanCheckboxSubmittedValue.xhtml");
        HtmlCheckBoxInput box1 = (HtmlCheckBoxInput) page.getHtmlElementById("box1");
        assertNotNull(box1);
        page = (HtmlPage) box1.click();
        HtmlCheckBoxInput box2 = (HtmlCheckBoxInput) page.getHtmlElementById("box2");
        assertNotNull(box2);
        HtmlPage newPage = (HtmlPage) box2.click();
        box1 = (HtmlCheckBoxInput) newPage.getHtmlElementById("box1");
        assertTrue(box1.isChecked());
        box2 = (HtmlCheckBoxInput) newPage.getHtmlElementById("box2");
        assertTrue(box2.isChecked());
    }
}
