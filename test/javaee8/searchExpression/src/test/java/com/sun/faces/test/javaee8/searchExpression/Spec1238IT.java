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

package com.sun.faces.test.javaee8.searchExpression;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlLabel;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.sun.faces.test.htmlunit.IgnoringIncorrectnessListener;
import com.sun.faces.test.junit.JsfTestRunner;
import org.junit.Assert;

@RunWith(JsfTestRunner.class)
public class Spec1238IT {

    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(120000);
    }

    @Test
    public void test() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());

        testSearchExpression();
    }

    public void testSearchExpression() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "spec1238.xhtml");
        webClient.waitForBackgroundJavaScript(60000);

        HtmlLabel label = (HtmlLabel) page.getHtmlElementById("label");
        HtmlTextInput input = (HtmlTextInput) page.getHtmlElementById("input");
        
        Assert.assertEquals(label.getAttribute("for"), input.getId());
        
        Assert.assertTrue(input.getAttribute("onchange").contains("@this input2"));
    }

    @After
    public void tearDown() {
        webClient.close();
    }

}
