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

package com.sun.faces.test.servlet30.dynamic;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

public class Issue2119IT {

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
    public void testAddComponent() throws Exception {

        /*
         * Make sure the added component is contained within the outer component. 
         */
        HtmlPage page = webClient.getPage(webUrl + "faces/add1.xhtml");
        String text = page.asText();
        assertTrue(text.matches("(?s).*Component::encodeBegin\\s*Component::encodeEnd.*"));

        /**
         * After clicking make sure the added component and its child are still in their proper place.
         */
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();
        text = page.asText();
        assertTrue(text.matches("(?s).*Component::encodeBegin\\s*NEW-OUTPUT\\s*Component::encodeEnd.*"));

        button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();
        text = page.asText();
        assertTrue(text.matches("(?s).*Component::encodeBegin\\s*NEW-OUTPUT\\s*Component::encodeEnd.*"));
    }
}
