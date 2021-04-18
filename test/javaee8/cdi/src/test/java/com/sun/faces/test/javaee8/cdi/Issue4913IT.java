/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation.
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
package com.sun.faces.test.javaee8.cdi;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Issue4913IT {

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
    public void test() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/issue4913.xhtml");
        webClient.waitForBackgroundJavaScript(3000);
        validateMarkup(page);

        // Refresh page 
        page = webClient.getPage(webUrl + "faces/issue4913.xhtml");
        webClient.waitForBackgroundJavaScript(3000);
        validateMarkup(page);
    }
    
    private static void validateMarkup(HtmlPage page) {
        DomElement issue4913Converter = page.getElementById("issue4913Converter");
        assertEquals("Converter is invoked", "value is successfully converted in a managed converter", issue4913Converter.asText());

        DomElement issue4913ResourceDependency = page.getElementById("issue4913ResourceDependency");
        assertEquals("Resource dependency is injected", "resource dependency is successfully injected via a managed converter", issue4913ResourceDependency.asText());
    }
}
