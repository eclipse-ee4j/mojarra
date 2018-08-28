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

package com.sun.faces.test.servlet30.stateviewexpiredexceptionmissing;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlHiddenInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlInput;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class Issue3359IT {
    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testIssue3359Fixed() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);

        String savingMethod = ((HtmlInput) page.getElementById("helloForm:stateSavingMethod")).getValueAttribute();
        String origStateId = ((HtmlHiddenInput) page.getElementByName("javax.faces.ViewState")).getValueAttribute();

        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("helloForm:button");
        page = button.click();

        HtmlAnchor anchor = (HtmlAnchor) page.getElementById("link");
        page = anchor.click();

        button = (HtmlSubmitInput) page.getElementById("helloForm:button");
        HtmlHiddenInput hi = (HtmlHiddenInput) (page.getElementByName("javax.faces.ViewState"));
        hi.setValueAttribute(origStateId);
        page = button.click();

        if ("client".equalsIgnoreCase(savingMethod)) {
            assertTrue(page.asXml().contains("Go back to the index page"));
        } else {
            assertTrue(page.asXml().contains("ViewExpiredException"));
        }
    }

}
