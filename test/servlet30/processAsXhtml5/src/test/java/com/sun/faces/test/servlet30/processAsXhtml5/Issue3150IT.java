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

package com.sun.faces.test.servlet30.processAsXhtml5;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.sun.faces.test.junit.JsfTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class Issue3150IT {

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
    public void testEscaping() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml?id4=%3Cscript%3Ealert%28%27hi%27%29;%3C/script%3E&id=myId&id2=\"myId2\"&id3=myId3");
        String pageMarkup = page.getWebResponse().getContentAsString();
        
        assertTrue(pageMarkup.contains("<span id=\"id\">myId</span>"));

        assertTrue(pageMarkup.contains("<input id=\"id\" type=\"hidden\" name=\"id\" value=\"myId\" />"));
        
        assertTrue(pageMarkup.contains("<input type=\"hidden\" name=\"id\" id=\"id\" value=\"myId\" />"));
        
        assertTrue(pageMarkup.contains("beforeScriptBlock: myId"));
        
        assertTrue(pageMarkup.contains("var paramId = \"myId2\";"));
        
        assertTrue(pageMarkup.contains("var paramIdd = \"myId3\";"));
        
        assertTrue(pageMarkup.contains("&lt;script&gt;alert('hi');&lt;/script&gt;"));


    }
}
