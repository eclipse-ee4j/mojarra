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
import static junit.framework.TestCase.assertEquals;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class DuplicateIdIT {

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

    /*
     * Because of differences in the JSP engine between Glassfish and Weblogic and this issue not being
     * raised as a bug by any user we exclude this test on the following servers: WLS 12.1.3 and WLS
     * 12.1.4
     *
     * 20140829 - edburns, mriem
     */
    @JsfTest(value = JSF_2_2_0, excludes = { WEBLOGIC_12_1_3, WEBLOGIC_12_1_4, WEBLOGIC_12_2_1 })
    @Test
    public void testDuplicateIds01() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/duplicateIds01.jsp");
        assertEquals(500, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testDuplicateIds02() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/duplicateIds02.jsp");
        assertEquals(200, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    /*
     * Because of differences in the JSP engine between Glassfish and Weblogic and this issue not being
     * raised as a bug by any user we exclude this test on the following servers: WLS 12.1.3 and WLS
     * 12.1.4
     *
     * 20140829 - edburns, mriem
     */
    @JsfTest(value = JSF_2_2_0, excludes = { WEBLOGIC_12_1_3, WEBLOGIC_12_1_4, WEBLOGIC_12_2_1 })
    @Test
    public void testDuplicateIds03() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/duplicateIds03.jsp");
        assertEquals(500, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testDuplicateIds05() throws Exception {
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/duplicateIds05.jsp");
        assertEquals(200, page.getWebResponse().getStatusCode());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }
}
