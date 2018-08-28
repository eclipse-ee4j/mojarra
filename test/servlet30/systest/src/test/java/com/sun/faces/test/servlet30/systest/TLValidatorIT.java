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

package com.sun.faces.test.servlet30.systest;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_1_3;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_1_4;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_2_1;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;
import static com.sun.faces.test.junit.JsfVersion.JSF_2_2_0;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class TLValidatorIT {

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
    public void testHtmlBasicValidatorFail() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/TestHtmlBasicValidatorFail.jsp");
        assertEquals(500, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testHtmlBasicValidatorFail2() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "TestHtmlBasicValidatorFail.jsp");
        assertEquals(500, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testHtmlBasicValidatorSucceed() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/TestHtmlBasicValidatorSucceed.jsp");
        assertEquals(200, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testCoreValidatorFail() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/TestCoreValidatorFail.jsp");
        assertEquals(500, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testCoreValidatorFail2() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "TestCoreValidatorFail.jsp");
        assertEquals(500, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testCoreValidatorSucceed() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/TestCoreValidatorSucceed.jsp");
        assertEquals(200, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testCoreValidatorIfFail() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/TestCoreValidatorIfFail.jsp");
        assertEquals(200, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testCoreValidatorIfSucceed() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/TestCoreValidatorIfSucceed.jsp");
        assertEquals(200, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    /*
     * Because of differences in the JSP engine between Glassfish and Weblogic and this issue not being
     * raised as a bug by any user we exclude this test on the following servers: WLS 12.1.3 and WLS
     * 12.1.4
     *
     * 20140903 - edburns, mriem
     */
    @JsfTest(value = JSF_2_2_0, excludes = { WEBLOGIC_12_1_3, WEBLOGIC_12_1_4, WEBLOGIC_12_2_1 })
    @Test
    public void testElValidatorActionRefFail() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/TestElValidatorActionRefFail.jsp");
        assertEquals(500, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    /*
     * Because of differences in the JSP engine between Glassfish and Weblogic and this issue not being
     * raised as a bug by any user we exclude this test on the following servers: WLS 12.1.3 and WLS
     * 12.1.4
     *
     * 20140903 - edburns, mriem
     */
    @JsfTest(value = JSF_2_2_0, excludes = { WEBLOGIC_12_1_3, WEBLOGIC_12_1_4, WEBLOGIC_12_2_1 })
    @Test
    public void testElValidatorComponentFail() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/TestElValidatorComponentFail.jsp");
        assertEquals(500, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testElValidatorIdFail() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/TestElValidatorIdFail.jsp");
        assertEquals(500, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testElValidatorValueRefFail() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/TestElValidatorValueRefFail.jsp");
        assertEquals(500, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testElValidatorActionRefSucceed() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/TestElValidatorActionRefSucceed.jsp");
        assertEquals(200, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testElValidatorComponentSucceed() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/TestElValidatorComponentSucceed.jsp");
        assertEquals(200, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testElValidatorIdSucceed() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/TestElValidatorIdSucceed.jsp");
        assertEquals(200, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testElValidatorValueRefSucceed() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/TestElValidatorValueRefSucceed.jsp");
        assertEquals(200, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testTlvTest01() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/jsp/tlvTest01.jsp");
        assertEquals(200, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }
}
