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

public class Issue2731IT {

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
        HtmlPage page = webClient.getPage(webUrl + "faces/viewTransient.xhtml");
        assertTrue(page.asXml().indexOf("\"stateless\"") != -1);
        
        HtmlElement button = page.getHtmlElementById("form:button");
        page = button.click();
        
        assertTrue(page.asXml().indexOf("\"stateless\"") != -1);
    }


    /**
     * Test stateless AJAX page.
     *
     * @throws Exception
     */
    @Test
    public void testStatelessAjax() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/viewTransientAjax.xhtml");
        assertTrue(page.asXml().indexOf("\"stateless\"") != -1);
        assertTrue(page.asXml().indexOf("[]") != -1);
        
        HtmlElement button = page.getHtmlElementById("form:ajaxButton");
        page = button.click();
        webClient.waitForBackgroundJavaScript(60000);
        
        assertTrue(page.asXml().indexOf("\"stateless\"") != -1);
        assertTrue(page.asXml().indexOf("[ajax]") != -1);

        button = page.getHtmlElementById("form:submitButton");
        page = button.click();

        assertTrue(page.asXml().indexOf("\"stateless\"") != -1);
        assertTrue(page.asXml().indexOf("[non-ajax]") != -1);
    }
    
    /**
     * Test default Stateful page.
     *
     * @throws Exception
     */
    @Test
    public void testDefaultStateful() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/viewTransientFalse.xhtml");
        assertTrue(page.asXml().indexOf("com.sun.faces.StatelessPostback") == -1);
        
        HtmlElement button = page.getHtmlElementById("form:button");
        page = button.click();
        
        assertTrue(page.asXml().indexOf("com.sun.faces.StatelessPostback") == -1);
    }
}
