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
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class Issue2718IT {

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

    /**
     * Test default Stateless page.
     *
     * @throws Exception
     */
    @Test
    public void testDefaultStateless() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/viewParam4.xhtml");
        
        HtmlElement link = page.getHtmlElementById("link");
        page = link.click();
        
        int firstIndex = page.asXml().indexOf("input1=1&amp;input2=2");
        int lastIndex = page.asXml().lastIndexOf("input1=1&amp;input2=2");
        assertTrue(firstIndex != -1);
        assertTrue(lastIndex != -1);
        assertTrue(firstIndex < lastIndex);
        
        HtmlElement button = page.getHtmlElementById("firstButton");
        page = button.click();
        
        firstIndex = page.asXml().indexOf("input1=1&amp;input2=2");
        lastIndex = page.asXml().lastIndexOf("input1=1&amp;input2=2");
        assertTrue(firstIndex != -1);
        assertTrue(lastIndex != -1);
        assertTrue(firstIndex < lastIndex);
    }
}
