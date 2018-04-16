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

package com.sun.faces.test.cluster.javaee6web.viewScoped;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import static org.junit.Assert.assertTrue;

public class Issue3319IT {
    /**
     * Stores the web URL.
     */
    private String webUrl;
    /**
     * Stores the web client.
     */
    private WebClient webClient;

    /**
     * Setup before testing.
     * 
     * @throws Exception when a serious error occurs.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    /**
     * Cleanup after testing.
     * 
     * @throws Exception when a serious error occurs.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Setup before testing.
     */
    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
    }

    /**
     * Tear down after testing.
     */
    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testViewScopeAndViewStateIsReplicated() throws Exception {
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        HtmlPage page = webClient.getPage(webUrl);

        HtmlTextInput myText = (HtmlTextInput) page.getElementById("text");
        String value = "" + System.currentTimeMillis();
        myText.setValueAttribute(value);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("reload");
        page = button.click();
        
        myText = (HtmlTextInput) page.getElementById("text");
        assertTrue(myText.getValueAttribute().equals(value));
        
        HtmlButton switchPort = (HtmlButton) page.getElementById("switchPort");
        page = switchPort.click();
        
        Thread.sleep(10000);
        
        button = (HtmlSubmitInput) page.getElementById("reload");
        page = button.click();
        
        myText = (HtmlTextInput) page.getElementById("text");
        assertTrue(myText.getValueAttribute().equals(value));        
        
    }
    
    @Test
    public void testViewScopeAndViewStateIsClearedAndReplicated() throws Exception {
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
        HtmlPage page = webClient.getPage(webUrl);

        HtmlTextInput myText = (HtmlTextInput) page.getElementById("text");
        String value = "" + System.currentTimeMillis();
        myText.setValueAttribute(value);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("reload");
        page = button.click();
        
        myText = (HtmlTextInput) page.getElementById("text");
        assertTrue(myText.getValueAttribute().equals(value));
        
        button = (HtmlSubmitInput) page.getElementById("page2");
        page = button.click();
        button = (HtmlSubmitInput) page.getElementById("back");
        page = button.click();

        myText = (HtmlTextInput) page.getElementById("text");
        assertTrue(!myText.getValueAttribute().equals(value));
        
        value = "" + System.currentTimeMillis();
        myText.setValueAttribute(value);
        button = (HtmlSubmitInput) page.getElementById("reload");
        page = button.click();
        
        myText = (HtmlTextInput) page.getElementById("text");
        assertTrue(myText.getValueAttribute().equals(value));
        
        HtmlButton switchPort = (HtmlButton) page.getElementById("switchPort");
        page = switchPort.click();
        
        Thread.sleep(10000);
        
        button = (HtmlSubmitInput) page.getElementById("reload");
        page = button.click();
        
        myText = (HtmlTextInput) page.getElementById("text");
        assertTrue(myText.getValueAttribute().equals(value));        
        
        button = (HtmlSubmitInput) page.getElementById("page2");
        page = button.click();
        button = (HtmlSubmitInput) page.getElementById("back");
        page = button.click();
        myText = (HtmlTextInput) page.getElementById("text");
        assertTrue(!myText.getValueAttribute().equals(value));
        
    }
    
}
