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

package com.sun.faces.test.servlet30.bogusrenderkitid;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class BogusRenderKitIdIT {

    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(60000);
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testPerViewRenderKitIdOverride() throws Exception {

        HtmlPage page = webClient.getPage(webUrl + "faces/use-basic-render-kit-id.xhtml");
        String text = page.asText();
        assertTrue("Expected: Configured render-kit-id: "
                + "org.apache.myfaces.trinidad.coreBAD. "
                + "UIViewRoot render-kit-id: HTML_BASIC."
                + " actual: " + text,
                text.matches("(?s).*Configured\\s*render-kit-id:\\s*org.apache.myfaces.trinidad.coreBAD.*UIViewRoot\\s*render-kit-id:\\s*HTML_BASIC.*"));
    }

    @Test
    public void testExceptionContainsConfiguredRenderKitId() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/use-configured-render-kit-id.xhtml");
        assertTrue(page.asText().contains("org.apache.myfaces.trinidad.coreBAD"));

        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
        webClient.getOptions().setPrintContentOnFailingStatusCode(false);
        boolean exceptionThrown = false;
        try {
            page = webClient.getPage(webUrl + "faces/use-configured-render-kit-id.xhtml");
        } catch (FailingHttpStatusCodeException e) {
            exceptionThrown = true;
            assertEquals(500, e.getStatusCode());
        }
        assertTrue(exceptionThrown);
    }
}
