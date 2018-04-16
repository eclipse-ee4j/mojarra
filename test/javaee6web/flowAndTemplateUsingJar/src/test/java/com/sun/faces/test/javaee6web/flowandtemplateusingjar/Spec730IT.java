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

package com.sun.faces.test.javaee6web.flowandtemplateusingjar;

import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import java.io.IOException;
import static org.junit.Assert.assertTrue;

public class Spec730IT {

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
    public void testDirectoryFlowWithTemplate() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        assertTrue(page.getBody().asText().contains("Page with link to flow entry"));
        doTest(page, "flow-a");
    }
    
    @Test
    public void testJarFlowWithTemplate() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        assertTrue(page.getBody().asText().contains("Page with link to flow entry"));
        doTest(page, "flow_with_template_in_jar");
    }
    
    public void doTest(HtmlPage page, String startButtonId) throws IOException {
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById(startButtonId);
        page = button.click();
        
        String pageText = page.getBody().asText();
        assertTrue(pageText.contains("top from template client flow-a"));
        assertTrue(pageText.contains("Bottom from template"));
        button = (HtmlSubmitInput) page.getElementById("next");
        page = button.click();
        
        pageText = page.getBody().asText();
        assertTrue(pageText.contains("Top from template"));
        assertTrue(pageText.contains("bottom from template client"));
        button = (HtmlSubmitInput) page.getElementById("return");
        page = button.click();
        
        pageText = page.getBody().asText();
        assertTrue(pageText.contains(startButtonId + "-return"));        
    }
}
