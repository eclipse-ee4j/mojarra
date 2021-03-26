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

package com.sun.faces.test.servlet31.faceletsemptyasnull;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import static com.sun.faces.test.junit.JsfServerExclude.GLASSFISH_4_0;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;
import static com.sun.faces.test.junit.JsfVersion.JSF_2_2_0;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class Issue2827IT {

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
     * Test to verify empty as null.
     *
     * Note: this test is excluded on Tomcat because the included EL parser
     * requires a System property for this test to work, which would cause
     * problems with other tests. See
     * http://tomcat.apache.org/tomcat-7.0-doc/config/systemprops.html and look
     * for COERCE_TO_ZERO
     * 
     * @throws Exception
     */
    @JsfTest(value=JSF_2_2_0)
    @Test
    public void testValidateEmptyFields() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");
        if (page.getWebResponse().getResponseHeaderValue("Server") == null
                || !page.getWebResponse().getResponseHeaderValue("Server").startsWith("Apache-Coyote")) {
            page = webClient.getPage(webUrl + "faces/verifyEmptyAsNull.xhtml");

            HtmlInput stringInput = page.getHtmlElementById("form:string");
            HtmlInput integerInput = page.getHtmlElementById("form:integer");

            assertNotNull(stringInput);
            assertNotNull(integerInput);
            assertEquals(stringInput.getValueAttribute(), "");
            assertEquals(integerInput.getValueAttribute(), "");

            String pageAsText = page.asText();
            assertTrue(pageAsText.contains("VC1 Fired: false"));
            assertTrue(pageAsText.contains("VC2 Fired: false"));
            assertTrue(pageAsText.contains("String model set with null: false"));
            assertTrue(pageAsText.contains("Integer model set with null: false"));

            HtmlSubmitInput submit = (HtmlSubmitInput) page.getHtmlElementById("form:command");
            assertNotNull(submit);

            stringInput.setValueAttribute("11");
            integerInput.setValueAttribute("11");

            page = (HtmlPage) submit.click();

            stringInput = page.getHtmlElementById("form:string");
            integerInput = page.getHtmlElementById("form:integer");

            assertNotNull(stringInput);
            assertNotNull(integerInput);
            assertEquals(stringInput.getValueAttribute(), "11");
            assertEquals(integerInput.getValueAttribute(), "11");

            pageAsText = page.asText();
            assertTrue(pageAsText.contains("VC1 Fired: true"));
            assertTrue(pageAsText.contains("VC2 Fired: true"));
            assertTrue(pageAsText.contains("String model set with null: false"));
            assertTrue(pageAsText.contains("Integer model set with null: false"));

            submit = (HtmlSubmitInput) page.getHtmlElementById("form:command");
            assertNotNull(submit);

            stringInput.setValueAttribute("");
            integerInput.setValueAttribute("");

            page = (HtmlPage) submit.click();

            stringInput = page.getHtmlElementById("form:string");
            integerInput = page.getHtmlElementById("form:integer");

            assertNotNull(stringInput);
            assertNotNull(integerInput);
            assertEquals(stringInput.getValueAttribute(), "");
            assertEquals(integerInput.getValueAttribute(), "");

            pageAsText = page.asText();
            assertTrue(pageAsText.contains("VC1 Fired: true"));
            assertTrue(pageAsText.contains("VC2 Fired: true"));
            assertTrue(pageAsText.contains("String model set with null: true"));
            assertTrue(pageAsText.contains("Integer model set with null: true"));

            submit = (HtmlSubmitInput) page.getHtmlElementById("form:command");
            assertNotNull(submit);

            stringInput.setValueAttribute("");
            integerInput.setValueAttribute("");

            page = (HtmlPage) submit.click();

            stringInput = page.getHtmlElementById("form:string");
            integerInput = page.getHtmlElementById("form:integer");

            assertNotNull(stringInput);
            assertNotNull(integerInput);
            assertEquals(stringInput.getValueAttribute(), "");
            assertEquals(integerInput.getValueAttribute(), "");

            pageAsText = page.asText();
            assertTrue(pageAsText.contains("VC1 Fired: false"));
            assertTrue(pageAsText.contains("VC2 Fired: false"));
            assertTrue(pageAsText.contains("String model set with null: true"));
            assertTrue(pageAsText.contains("Integer model set with null: true"));
        }
    }
}
