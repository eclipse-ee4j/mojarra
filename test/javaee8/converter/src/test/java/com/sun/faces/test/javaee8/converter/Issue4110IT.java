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

package com.sun.faces.test.javaee8.converter;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class Issue4110IT {

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
    public void testJavaTimeTypes() throws Exception {
        
        doTestJavaTimeTypes("30-sep-2015", "localDate", "2015-09-30");
        doTestJavaTimeTypes("16:52:56", "localTime", "16:52:56");
        doTestJavaTimeTypes("30-sep-2015 16:14:43", "localDateTime", "2015-09-30T16:14:43");
    }
    
    private void doTestJavaTimeTypes(String value, String type, String expected) throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/issue4110.xhtml");
        
        HtmlTextInput input;
        HtmlSubmitInput submit;
        HtmlSpan output;

        input = page.getHtmlElementById("form:" + type + "Input");
        input.setValueAttribute(value);
        submit = page.getHtmlElementById("form:submit");
        page = submit.click();
        output = page.getHtmlElementById("form:" + type + "Output");
        assertEquals(expected, output.getTextContent());
    }
    
}
