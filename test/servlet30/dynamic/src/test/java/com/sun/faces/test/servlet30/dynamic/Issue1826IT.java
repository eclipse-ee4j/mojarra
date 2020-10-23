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

package com.sun.faces.test.servlet30.dynamic;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

public class Issue1826IT {

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
    public void testAddComponent() throws Exception {

        /*
         * Make sure the added component is contained within the outer component. 
         */
        HtmlPage page = webClient.getPage(webUrl + "faces/add.xhtml");
        assertTrue(page.asXml().indexOf("encodeBegin") < page.asXml().indexOf(" Dynamically added child"));
        assertTrue(page.asXml().indexOf(" Dynamically added child") < page.asXml().indexOf("encodeEnd"));

        /**
         * After clicking make sure the added component is still in its proper place.
         */
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();
        assertTrue(page.asXml().indexOf("encodeBegin") < page.asXml().indexOf(" Dynamically added child"));
        assertTrue(page.asXml().indexOf(" Dynamically added child") < page.asXml().indexOf("encodeEnd"));
    }

    @Test
    public void testStable() throws Exception {

         String inputValue1 = "value=" + '"' + "1" + '"';
         String inputValue2 = "value=" + '"' + "2" + '"';
         String idText3 = "id=" + '"' + "text3" + '"';

        /*
         * Make sure the three dynamically added input components are in their proper place. 
         */
        HtmlPage page = webClient.getPage(webUrl + "faces/stable.xhtml");
        assertTrue(page.asXml().indexOf("encodeBegin") < page.asXml().indexOf(inputValue1));
        assertTrue(page.asXml().indexOf(inputValue1) < page.asXml().indexOf(inputValue2));
        assertTrue(page.asXml().indexOf(inputValue2) < page.asXml().indexOf(idText3));
        assertTrue(page.asXml().indexOf("text3") < page.asXml().indexOf("encodeEnd"));

        /**
         * After clicking make sure the added component is still in its proper place.
         * Also verify the validation required error message appears as the third input
         * component has required attribute set to true.
         */
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();
        assertTrue(page.asXml().contains("text3: Validation Error: Value is required."));
        assertTrue(page.asXml().indexOf("encodeBegin") < page.asXml().indexOf(inputValue1));
        assertTrue(page.asXml().indexOf(inputValue1) < page.asXml().indexOf(inputValue2));
        assertTrue(page.asXml().indexOf(inputValue2) < page.asXml().indexOf(idText3));
        assertTrue(page.asXml().indexOf("text3") < page.asXml().indexOf("encodeEnd"));
    }

    @Test
    public void testTable() throws Exception {

        /*
         * Make sure the dynamically added table and table values are in place.
         */
        HtmlPage page = webClient.getPage(webUrl + "faces/table.xhtml");
        assertTrue(page.asXml().indexOf("encodeBegin") < page.asXml().indexOf("Foo"));
        assertTrue(page.asXml().indexOf("Foo") < page.asXml().indexOf("Bar"));
        assertTrue(page.asXml().indexOf("Bar") < page.asXml().indexOf("Baz"));
        assertTrue(page.asXml().indexOf("Baz") < page.asXml().indexOf("encodeEnd"));

        /**
         * After clicking make sure the added component is still in its proper place.
         * Also verify the validation required error message appears as the third input
         * component has required attribute set to true.
         */
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();
        assertTrue(page.asXml().indexOf("encodeBegin") < page.asXml().indexOf("Foo"));
        assertTrue(page.asXml().indexOf("Foo") < page.asXml().indexOf("Bar"));
        assertTrue(page.asXml().indexOf("Bar") < page.asXml().indexOf("Baz"));
        assertTrue(page.asXml().indexOf("Baz") < page.asXml().indexOf("encodeEnd"));
    }

    @Test
    public void testRecursive() throws Exception {
        /*
         * Make sure the added component and nested component is in the proper place. 
         */
        HtmlPage page = webClient.getPage(webUrl + "faces/recursive.xhtml");
        String text = page.asText();
        int first = text.indexOf("Dynamically");
        int next = text.indexOf("Dynamically", first + ("Dynamically").length());
        assertTrue(first < next);

        /**
         * After clicking make sure the added component and nested component is still in 
         * its proper place.
         */
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();
        text = page.asText();
        first = text.indexOf("Dynamically");
        next = text.indexOf("Dynamically", first + ("Dynamically").length());
        assertTrue(first < next);
    }


}
