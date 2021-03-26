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

package com.sun.faces.test.servlet30.ajaxnamespace;

import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.javascript.host.event.Event;
import com.sun.faces.test.htmlunit.DebugHelper;
import com.gargoylesoftware.htmlunit.html.HtmlElement;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import static com.gargoylesoftware.htmlunit.javascript.host.event.Event.TYPE_CHANGE;
import static org.junit.Assert.assertTrue;

public class Issue3031IT {
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
        webClient.setAjaxController(new NicelyResynchronizingAjaxController());
    }

    /**
     * Tear down after testing.
     */
    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void testAjax() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        
        HtmlElement input = (HtmlElement) page.getElementById("MyNamingContainerj_id1:ajaxInput");
        assertTrue(null != input);
        assertTrue(input instanceof HtmlTextInput);
        
        HtmlTextInput textInput = (HtmlTextInput) input;
        textInput.setText("MyText");
        textInput.blur();
        textInput.fireEvent(TYPE_CHANGE);
        webClient.waitForBackgroundJavaScript(60000);
        
        HtmlElement output = (HtmlElement) page.getElementById("MyNamingContainerj_id1:ajaxOutput");
        assertTrue(output.asText().contains("MyText"));
    }
    
    @Test
    public void testAjaxWithParams() throws Exception {
    	HtmlPage page = webClient.getPage(webUrl);
        
        HtmlElement input = (HtmlElement) page.getElementById("MyNamingContainerj_id1:ajaxInputParams");
        assertTrue(null != input);
        assertTrue(input instanceof HtmlTextInput);
        HtmlTextInput textInput = (HtmlTextInput) input;
        textInput.setText("MyText");
        
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("MyNamingContainerj_id1:ajaxSubmitParams");
        page = button.click();
        webClient.waitForBackgroundJavaScript(60000);

        HtmlElement output = (HtmlElement) page.getElementById("MyNamingContainerj_id1:ajaxOutputParams");
        assertTrue(output.asText().contains("MyText value"));


    }

    @Test
    public void testNonAjax() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        
        HtmlElement input = (HtmlElement) page.getElementById("MyNamingContainerj_id1:nonAjaxInput");
        assertTrue(null != input);
        assertTrue(input instanceof HtmlTextInput);
        HtmlTextInput textInput = (HtmlTextInput) input;
        textInput.setText("MyNonAjaxText");
        
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("MyNamingContainerj_id1:nonAjaxSubmit");
        page = button.click();
        
        HtmlElement output = (HtmlElement) page.getElementById("MyNamingContainerj_id1:nonAjaxOutput");
        assertTrue(output.asText().contains("MyNonAjaxText"));
 
 
    }

    @Test
    public void testNonAjaxWithParams() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        
        HtmlElement input = (HtmlElement) page.getElementById("MyNamingContainerj_id1:nonAjaxInputParams");
        assertTrue(null != input);
        assertTrue(input instanceof HtmlTextInput);
        HtmlTextInput textInput = (HtmlTextInput) input;
        textInput.setText("MyNonAjaxText");
        
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("MyNamingContainerj_id1:nonAjaxSubmitParams");
        page = button.click();
        
        HtmlElement output = (HtmlElement) page.getElementById("MyNamingContainerj_id1:nonAjaxOutputParams");
        assertTrue(output.asText().contains("MyNonAjaxText value"));


    }
}
