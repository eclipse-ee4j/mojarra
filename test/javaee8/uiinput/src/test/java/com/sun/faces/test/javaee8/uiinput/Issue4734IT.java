/*
 * Copyright (c) 1997, 2019 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Contributors to the Eclipse Foundation.
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
package com.sun.faces.test.javaee8.uiinput;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.sun.faces.test.htmlunit.IgnoringIncorrectnessListener;
import com.sun.faces.test.junit.JsfTestRunner;

@RunWith(JsfTestRunner.class)
public class Issue4734IT {

    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(60000);
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());
    }

    @Test
    public void testIssue4734() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "issue4734.xhtml");
        HtmlSubmitInput submit = page.getHtmlElementById("form:submit");

        submit.click(); // The first click is expected to work fine.

        submit.click(); // Before the fix, the second click failed with com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException: 500 Internal Server Error

        submit.click(); // A third one, just to be sure it keeps working! ;)
    }

    @After
    public void tearDown() {
        webClient.close();
    }

}