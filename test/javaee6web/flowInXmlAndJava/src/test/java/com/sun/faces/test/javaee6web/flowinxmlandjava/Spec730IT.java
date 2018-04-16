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

package com.sun.faces.test.javaee6web.flowinxmlandjava;

import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Spec730IT {
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
    public void testFacesFlowScopeXml() throws Exception {
        performCustomerUpgradeTest("maintain-customer-record");
        performCustomerUpgradeTest("maintain-customer-record");
        performInFlowExplicitNavigationTest("maintain-customer-record");
        performInFlowExplicitNavigationTest("maintain-customer-record");
        
    }
    
    @Test
    public void testFacesFlowScopeJava() throws Exception {
        performCustomerUpgradeTest("maintain-customer-record-java");
        performCustomerUpgradeTest("maintain-customer-record-java");
        performInFlowExplicitNavigationTest("maintain-customer-record-java");
        performInFlowExplicitNavigationTest("maintain-customer-record-java");
        
    }
    private void performCustomerUpgradeTest(String startButton) throws Exception {
        quickEnterExit();
        
        HtmlPage page = webClient.getPage(webUrl);
        
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById(startButton);
        
        page = button.click();
        
        button = (HtmlSubmitInput) page.getElementById("createCustomer");
        page = button.click();
        String pageText = page.asText();
        Pattern pattern = Pattern.compile("(?s).*Customer Id:\\s+([0-9])+.*");
        Matcher matcher = pattern.matcher(pageText);
        assertTrue(matcher.matches());
        String customerId = matcher.group(1);
        assertTrue(pageText.matches("(?s).*Customer is upgraded:\\s+false.*"));
        
        button = (HtmlSubmitInput) page.getElementById("upgrade");
        page = button.click();
        pageText = page.asText();
        matcher = pattern.matcher(pageText);
        assertTrue(matcher.matches());
        String sameCustomerId = matcher.group(1);
        assertTrue(pageText.matches("(?s).*Customer is upgraded:\\s+true.*"));
        assertEquals(customerId, sameCustomerId);
        
        button = (HtmlSubmitInput) page.getElementById("exit");
        page = button.click();
        
        pageText = page.getBody().asText();
        
        assertTrue(pageText.contains("return page"));
        assertTrue(pageText.contains("Finalizer called"));
        
        button = (HtmlSubmitInput) page.getElementById("home");
        page = button.click();

        button = (HtmlSubmitInput) page.getElementById(startButton);
        assertNotNull(button);
        
        
    }
    
    private void performInFlowExplicitNavigationTest(String startButton) throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getElementById(startButton);
        page = submit.click();
        submit = (HtmlSubmitInput) page.getElementById("createCustomer");
        page = submit.click();
        
        submit = (HtmlSubmitInput) page.getElementById("pageA");
        page = submit.click();
        String pageText = page.asText();
        assertTrue(pageText.contains("explicit in flow nav 01"));
        
        submit = (HtmlSubmitInput) page.getElementById("pageB");
        page = submit.click();
        
        pageText = page.asText();
        assertTrue(pageText.contains("explicit in flow nav 02"));
        
        submit = (HtmlSubmitInput) page.getElementById("pageC_true");
        page = submit.click();
        
        pageText = page.asText();
        assertTrue(pageText.contains("explicit in flow nav 03: if true"));
        
        submit = (HtmlSubmitInput) page.getElementById("pageB");
        page = submit.click();
        
        submit = (HtmlSubmitInput) page.getElementById("pageC_false");
        page = submit.click();
        
        pageText = page.asText();
        assertTrue(pageText.contains("explicit in flow nav 03: if false"));
        
        submit = (HtmlSubmitInput) page.getElementById("pageB");
        page = submit.click();
        
        submit = (HtmlSubmitInput) page.getElementById("pageD_redirect");
        page = submit.click();
        
        pageText = page.asText();
        assertTrue(pageText.contains("explicit in flow nav 04: no params"));

        submit = (HtmlSubmitInput) page.getElementById("pageB");
        page = submit.click();
        
        HtmlButtonInput button = (HtmlButtonInput) page.getElementById("pageD_redirect_params");
        page = button.click();
        
        pageText = page.asText();
        assertTrue(pageText.contains("explicit in flow nav 05: params"));
        assertTrue(pageText.contains("id param: foo"));
        assertTrue(pageText.contains("baz param: bar"));
        
        
    }
    
    
    private void quickEnterExit() throws Exception {
        HtmlPage page = webClient.getPage(webUrl);
        
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("maintain-customer-record");
        
        page = button.click();
        
        String pageText = page.getBody().asText();
        
        assertTrue(pageText.contains("Create customer page"));
        assertTrue(pageText.contains("Initializer called"));
        
        button = (HtmlSubmitInput) page.getElementById("return");
        
        page = button.click();
        
        pageText = page.getBody().asText();
        
        assertTrue(pageText.contains("return page"));
        assertTrue(pageText.contains("Finalizer called"));
        
    }
}
