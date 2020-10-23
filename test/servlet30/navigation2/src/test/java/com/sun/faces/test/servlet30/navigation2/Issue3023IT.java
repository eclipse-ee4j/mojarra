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

package com.sun.faces.test.servlet30.navigation2;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

public class Issue3023IT {
    
    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testOutcomeParameter1() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/outcomeParameterForm.xhtml");
        HtmlElement submit1 = page.getHtmlElementById("form:submit1");
        page = submit1.click();
        assertTrue(page.asText().indexOf("query=Laurel & Hardy") != -1);
    }

    @Test
    public void testOutcomeParameter2() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/outcomeParameterForm.xhtml");
        HtmlElement submit = page.getHtmlElementById("form:submit2");
        page = submit.click();
        assertTrue(page.asText().indexOf("query=Laurel & Hardy") == -1);
        assertTrue(page.asText().indexOf("query=Laurel") != -1);
    }

    @Test
    public void testOutcomeParameter3() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/outcomeParameterForm.xhtml");
        HtmlElement submit = page.getHtmlElementById("form:submit3");
        page = submit.click();
        assertTrue(page.asText().indexOf("query=Laurel & Hardy") != -1);
    }

    @Test
    public void testOutcomeParameter4() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/outcomeParameterForm.xhtml");
        HtmlElement submit = page.getHtmlElementById("form:submit4");
        page = submit.click();
        assertTrue(page.asText().indexOf("query=Laurel & Hardy") != -1);
    }

    @Test
    public void testOutcomeParameter5() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "/faces/outcomeParameterForm.xhtml");
        HtmlElement submit = page.getHtmlElementById("form:submit4");
        page = submit.click();
        assertTrue(page.asText().indexOf("query=Laurel & Hardy") != -1);
    }
}
