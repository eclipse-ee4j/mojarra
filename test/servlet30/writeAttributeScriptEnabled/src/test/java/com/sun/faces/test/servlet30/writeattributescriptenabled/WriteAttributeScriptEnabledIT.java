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

package com.sun.faces.test.servlet30.writeattributescriptenabled;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class WriteAttributeScriptEnabledIT {

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
    public void testWriteAttributeDisabled() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);

        // HACK: The first request to the page will result in the value
        // having jsessionid encoded in the link value. Making a second
        // request to the page means we've joined the session and the value
        // will no longer include the jsessionid (at least when cookies are enabled)
        // and clicking the link will not produce JS errors.
        HtmlPage page = webClient.getPage(webUrl + "faces/test.jsp");
        page = webClient.getPage(webUrl + "faces/test.jsp");

        HtmlAnchor link = page.getAnchors().get(0);

        HtmlPage errorPage = (HtmlPage) link.click();
        assertTrue(errorPage.asText().indexOf("new value!") >= 0);
    }
}
