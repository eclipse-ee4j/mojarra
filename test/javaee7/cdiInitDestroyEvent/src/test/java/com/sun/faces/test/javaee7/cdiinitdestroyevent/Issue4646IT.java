/*
 * Copyright (c) 1997, 2019 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.javaee7.cdiinitdestroyevent;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;
import static com.sun.faces.test.junit.JsfVersion.JSF_2_2_0;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class Issue4646IT {

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

    @JsfTest(value = JSF_2_2_0)
    @Test
    public void testPreDestroyEventIssue4646() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/issue4646.xhtml");
        HtmlElement e = (HtmlElement) page.getElementById("counterMessage");
        int currentCount = Integer.parseInt(e.asText());
        // +1
        page = webClient.getPage(webUrl + "faces/issue4646.xhtml");
        e = (HtmlElement) page.getElementById("counterMessage");
        Assert.assertEquals("+1 should be the objects created", currentCount + 1, Integer.parseInt(e.asText()));
        // +2
        page = webClient.getPage(webUrl + "faces/issue4646.xhtml");
        e = (HtmlElement) page.getElementById("counterMessage");
        Assert.assertEquals("+2 should be the objects created", currentCount + 2, Integer.parseInt(e.asText()));
        // invalidate
        HtmlSubmitInput invalidateButton = (HtmlSubmitInput) page.getElementById("invalidateSession");
        invalidateButton.click();
        // should be the initial count
        page = webClient.getPage(webUrl + "faces/issue4646.xhtml");
        e = (HtmlElement) page.getElementById("counterMessage");
        Assert.assertEquals("The initial count should be again", currentCount, Integer.parseInt(e.asText()));
        // invalidate again
        invalidateButton = (HtmlSubmitInput) page.getElementById("invalidateSession");
        invalidateButton.click();
    }
}
