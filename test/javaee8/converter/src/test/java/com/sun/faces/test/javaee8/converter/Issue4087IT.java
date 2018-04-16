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
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class Issue4087IT {

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
        HtmlPage page = webClient.getPage(webUrl + "faces/issue4087.xhtml");
        
        HtmlTextInput input1 = (HtmlTextInput)page.getHtmlElementById("localDateTime1");
        input1.setValueAttribute("Sep 30, 2015 4:14:43 PM");
        
        HtmlTextInput input2 = (HtmlTextInput)page.getHtmlElementById("localDateTime2");
        input2.setValueAttribute("Sep 30, 2015 4:14:43 PM");

        HtmlTextInput input3 = (HtmlTextInput)page.getHtmlElementById("localTime1");
        input3.setValueAttribute("4:14:43 PM");
        
        HtmlTextInput input4 = (HtmlTextInput)page.getHtmlElementById("localTime2");
        input4.setValueAttribute("4:14:43 PM");
        
        HtmlSubmitInput submit = (HtmlSubmitInput)page.getHtmlElementById("submit");
        HtmlPage page1 = submit.click();
        
        HtmlSpan time1Output = (HtmlSpan)page1.getHtmlElementById("localDateTimeValue1");
        assertTrue(time1Output.getTextContent().contains("Sep 30, 2015 4:14 PM"));
        
        HtmlSpan time2Output = (HtmlSpan)page1.getHtmlElementById("localDateTimeValue2");
        assertTrue(time2Output.getTextContent().contains("Sep 30, 2015 4:14 PM"));

        HtmlSpan time3Output = (HtmlSpan)page1.getHtmlElementById("localTimeValue1");
        assertTrue(time3Output.getTextContent().contains("4:14:43 PM"));
        
        HtmlSpan time4Output = (HtmlSpan)page1.getHtmlElementById("localTimeValue2");
        assertTrue(time4Output.getTextContent().contains("4:14 PM"));
    }
    
}
