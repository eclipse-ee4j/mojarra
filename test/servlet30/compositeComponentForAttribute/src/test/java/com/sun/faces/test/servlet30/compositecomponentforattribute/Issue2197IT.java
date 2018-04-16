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

package com.sun.faces.test.servlet30.compositecomponentforattribute;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import javax.faces.component.NamingContainer;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class Issue2197IT {

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
    public void testConvertNumber() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/convertNumber.xhtml");
        HtmlTextInput input = (HtmlTextInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "register"
                + NamingContainer.SEPARATOR_CHAR
                + "name");
        input.setValueAttribute("Foo");
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "button");
        page = (HtmlPage) submit.click();
        assertTrue(page.asText().contains("could not be understood as a percentage"));
    }

    @Test
    public void testConverter() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/converter.xhtml");
        HtmlTextInput input = (HtmlTextInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "register"
                + NamingContainer.SEPARATOR_CHAR
                + "name");
        input.setValueAttribute("Foo");
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "button");
        page = (HtmlPage) submit.click();
        assertTrue(page.asText().contains("must be a number consisting of one or more digits"));
    }

    @Test
    public void testConvertDateTime() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/convertDateTime.xhtml");
        HtmlTextInput input = (HtmlTextInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "register"
                + NamingContainer.SEPARATOR_CHAR
                + "name");
        input.setValueAttribute("Foo");
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "button");
        page = (HtmlPage) submit.click();
        assertTrue(page.asText().contains("could not be understood as a date"));
    }

    @Test
    public void testValidateLength() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/validateLength.xhtml");
        HtmlTextInput input = (HtmlTextInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "register"
                + NamingContainer.SEPARATOR_CHAR
                + "name");
        input.setValueAttribute("FooFoo");
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "button");
        page = (HtmlPage) submit.click();
        assertTrue(page.asText().contains("Length is greater than allowable maximum"));
    }

    @Test
    public void testActionListener() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/actionListener.xhtml");
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "mybutton"
                + NamingContainer.SEPARATOR_CHAR
                + "name");
        page = (HtmlPage) submit.click();
        assertTrue(page.asText().contains("name was pressed"));
    }

    @Test
    public void testValueChangeListener() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/valueChangeListener.xhtml");
        HtmlTextInput input = (HtmlTextInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "register"
                + NamingContainer.SEPARATOR_CHAR
                + "name");
        input.setValueAttribute("FooFoo");
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "button");
        page = (HtmlPage) submit.click();
        assertTrue(page.asText().contains("name value was changed"));
    }

    @Test
    public void testSetPropertyActionListener() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/setPropertyActionListener.xhtml");
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "mybutton"
                + NamingContainer.SEPARATOR_CHAR
                + "name");
        page = (HtmlPage) submit.click();
        assertTrue(page.asText().contains("foo"));
    }

    @Test
    public void testValidateDoubleRange() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/validateDoubleRange.xhtml");
        HtmlTextInput input = (HtmlTextInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "register"
                + NamingContainer.SEPARATOR_CHAR
                + "name");
        input.setValueAttribute("123456");
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "button");
        page = (HtmlPage) submit.click();
        assertTrue(page.asText().contains("Specified attribute is not between the expected values of 2 and 5"));
    }

    @Test
    public void testValidateLongRange() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/validateLongRange.xhtml");
        HtmlTextInput input = (HtmlTextInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "register"
                + NamingContainer.SEPARATOR_CHAR
                + "name");
        input.setValueAttribute("123456");
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "button");
        page = (HtmlPage) submit.click();
        assertTrue(page.asText().contains("Specified attribute is not between the expected values of 2 and 5"));
    }

    @Test
    public void testValidateRequired() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/validateRequired.xhtml");
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "button");
        page = (HtmlPage) submit.click();
        assertTrue(page.asText().contains("Value is required"));
    }

    @Test
    public void testValidator() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/validator.xhtml");
        HtmlTextInput input = (HtmlTextInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "register"
                + NamingContainer.SEPARATOR_CHAR
                + "name");
        input.setValueAttribute("123456");
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "button");
        page = (HtmlPage) submit.click();
        assertTrue(page.asText().contains("name was validated"));
    }

    @Test
    public void testValidateRegex() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/validateRegex.xhtml");
        HtmlTextInput input = (HtmlTextInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "register"
                + NamingContainer.SEPARATOR_CHAR
                + "name");
        input.setValueAttribute("$$$$$$$$$$$");
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getElementById("form"
                + NamingContainer.SEPARATOR_CHAR
                + "button");
        page = (HtmlPage) submit.click();
        assertTrue(page.asText().contains("Regex Pattern not matched"));
    }
}
