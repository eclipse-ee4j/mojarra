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

package com.sun.faces.test.servlet30.faceletsemptyasnull;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;
import com.sun.faces.test.junit.JsfVersion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class Issue1508IT {

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

    /**
     * Test to validate empty fields.
     *
     * Note: this test is excluded on Tomcat because the included EL parser
     * requires a System property for this test to work, which would cause
     * problems with other tests. See
     * http://tomcat.apache.org/tomcat-7.0-doc/config/systemprops.html and look
     * for COERCE_TO_ZERO
     * 
     * @throws Exception
     */
    @JsfTest(JsfVersion.JSF_2_2_1)
    @Test
    public void testValidateEmptyFields() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");
        if (page.getWebResponse().getResponseHeaderValue("Server") == null
                || !page.getWebResponse().getResponseHeaderValue("Server").startsWith("Apache-Coyote")) {
            page = webClient.getPage(webUrl + "faces/validateEmptyFields.xhtml");
            HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("form:submitButton");
            page = button.click();
            assertTrue(page.asXml().contains("We got called!"));
        }
    }
}
