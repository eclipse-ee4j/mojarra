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

package com.sun.faces.test.javaee6web.facelets;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class Issue4124IT {

    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(120000);
    }

    @Test
    public void testIssue4124() throws Exception {

        HtmlPage page = webClient.getPage(webUrl + "faces/issue4124.xhtml");
        
        
        DomNode tbody = getTbody(page.getHtmlElementById("duplicatesTable"));
        int count = getRows(tbody).size();
        
        assertTrue(
            "On Page \n" + page.asXml() +
            "\nDomNode " + tbody.asXml() + " should have 1 child, but has " + count,
            count == 1
        );
        
        page = page.getHtmlElementById("duplicatesTable:pSplitButton").click();
        tbody = getTbody(page.getHtmlElementById("duplicatesTable"));
        count = getRows(tbody).size();
        assertTrue(count == 1);

        page = page.getHtmlElementById("duplicatesTable:pSplitButton").click();
        tbody = getTbody(page.getHtmlElementById("duplicatesTable"));
        count = getRows(tbody).size();
        assertTrue(count == 2);

        page = page.getHtmlElementById("duplicatesTable:pSplitButton").click();
        tbody = getTbody(page.getHtmlElementById("duplicatesTable"));
        count = getRows(tbody).size();
        assertTrue(count == 3);
    }

    private DomNode getTbody(HtmlElement table) {
        for (DomNode tablechild : table.getChildNodes()) {
            if ("tbody".equals(tablechild.getLocalName())) {
                return tablechild;
            }
        }
        
        assertTrue("No tbody found", false);
        
        return null;
    }
    
    private List<DomNode> getRows(DomNode body) {
        List<DomNode> rows = new ArrayList<>(); 
                
        for (DomNode bodyChild : body.getChildNodes()) {
            if ("tr".equals(bodyChild.getLocalName())) {
                rows.add(bodyChild);
            }
        }
        
        return rows;
    }
    
    
    @After
    public void tearDown() {
        webClient.close();
    }

}
