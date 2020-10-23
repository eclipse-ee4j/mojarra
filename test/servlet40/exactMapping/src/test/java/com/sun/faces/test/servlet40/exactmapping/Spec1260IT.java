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

package com.sun.faces.test.servlet40.exactmapping;

import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_1_4;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_2_1;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_3_1;
import static com.sun.faces.test.junit.JsfVersion.JSF_2_3_0_M10;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;

@RunWith(JsfTestRunner.class)
public class Spec1260IT {

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
    @JsfTest(value = JSF_2_3_0_M10, excludes = {WEBLOGIC_12_1_4, WEBLOGIC_12_2_1, WEBLOGIC_12_3_1})
    public void testExactMappedViewLoads() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "foo");
        String content = page.asXml();
        
        // Basic test that if the FacesServlet is mapped to /foo, the right view "foo.xhtml" is loaded.
        assertTrue(content.contains("This is page foo"));
    }
    
    @Test
    @JsfTest(value = JSF_2_3_0_M10, excludes = {WEBLOGIC_12_1_4, WEBLOGIC_12_2_1, WEBLOGIC_12_3_1})
    public void testPostBackToExactMappedView() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "foo");
        
        page = page.getHtmlElementById("form:commandButton").click();
        
        String content = page.asXml();
        
        assertTrue(content.contains("foo method invoked"));
        
        // If page /foo postbacks to itself, the new URL should be /foo again
        assertTrue(page.getUrl().getPath().endsWith("/foo"));
    }
    
    @Test
    @JsfTest(value = JSF_2_3_0_M10, excludes = {WEBLOGIC_12_1_4, WEBLOGIC_12_2_1, WEBLOGIC_12_3_1})
    public void testLinkToNonExactMappedView() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "foo");
        
        assertTrue(page.asXml().contains("This is page foo"));
        
        page = page.getHtmlElementById("form:button").click();
        
        String content = page.asXml();
        
        assertTrue(content.contains("This is page bar"));
        
        // view "bar" is not exact mapped, so should be loaded via the suffix
        // or prefix the FacesServlet is mapped to when coming from /foo
        
        String path = page.getUrl().getPath();
        
        assertTrue(path.endsWith("/bar.jsf") || path.endsWith("/faces/bar"));
    }
    
    @Test
    @JsfTest(value = JSF_2_3_0_M10, excludes = {WEBLOGIC_12_1_4, WEBLOGIC_12_2_1, WEBLOGIC_12_3_1})
    public void testPostBackOnLinkedNonExactMappedView() throws Exception {
        
        // Navigate from /foo to /bar.jsf
        HtmlPage page = webClient.getPage(webUrl + "foo");
        page = page.getHtmlElementById("form:button").click();
        
        // After navigating to a non-exact mapped view, a postback should stil work 
        page = page.getHtmlElementById("form:commandButton").click();
        assertTrue(page.asXml().contains("foo method invoked"));
        
        // Check we're indeed on bar.jsf or faces/bar
        String path = page.getUrl().getPath();
        assertTrue(path.endsWith("/bar.jsf") || path.endsWith("/faces/bar"));
    }
    
    
    @Test
    @JsfTest(value = JSF_2_3_0_M10, excludes = {WEBLOGIC_12_1_4, WEBLOGIC_12_2_1, WEBLOGIC_12_3_1})
    public void testResourceReferenceFromExactMappedView() throws Exception {
        
        HtmlPage page = webClient.getPage(webUrl + "foo");
        
        String content = page.asXml();
        
        // Runtime must have found out the mappings of the FacesServlet and used one of the prefix or suffix
        // mappings to render the reference to "jsf.js", which is not exactly mapped.
        assertTrue(content.contains("jakarta.faces.resource/jsf.js.jsf") || content.contains("jakarta.faces.resource/faces/jsf.js") );
    }
    
    @Test
    @JsfTest(value = JSF_2_3_0_M10, excludes = {WEBLOGIC_12_1_4, WEBLOGIC_12_2_1, WEBLOGIC_12_3_1})
    public void testAjaxFromExactMappedView() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "foo");
        
        page = page.getHtmlElementById("form:commandButtonAjax").click();
        webClient.waitForBackgroundJavaScript(6000);
        
        String content = page.asXml();
        
        // AJAX from an exact-mapped view should work
        assertTrue(content.contains("partial request = true"));
        
        // Part of page not updated via AJAX so should not show
        assertTrue(!content.contains("should not see this"));
    }

    
   
}
