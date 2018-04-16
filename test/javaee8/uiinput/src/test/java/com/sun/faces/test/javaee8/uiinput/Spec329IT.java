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

package com.sun.faces.test.javaee8.uiinput;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.sun.faces.test.htmlunit.IgnoringIncorrectnessListener;
import com.sun.faces.test.junit.JsfTestRunner;

@RunWith(JsfTestRunner.class)
public class Spec329IT {

    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(120000);
    }

    @Test
    public void testSpec329() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());

        HtmlPage page = webClient.getPage(webUrl + "spec329.xhtml");
        assertTrue(page.getHtmlElementById("messages").asText().isEmpty());
        assertTrue(page.getHtmlElementById("inDataTableWithEntityList:selectedItem").asText().isEmpty());
        assertTrue(page.getHtmlElementById("inRepeatWithSelectItemList:selectedItem").asText().isEmpty());
        assertTrue(page.getHtmlElementById("multipleRadioButtonsWithStaticItemsInFirstRadio:selectedItem").asText().isEmpty());
        assertTrue(page.getHtmlElementById("multipleRadioButtonsWithStaticItemsInEachRadio:selectedItem").asText().isEmpty());
        assertTrue(page.getHtmlElementById("multipleRadioButtonsWithSelectItemList:selectedItem").asText().isEmpty());

        page = ((HtmlSubmitInput) page.getHtmlElementById("inDataTableWithEntityList:button")).click();
        assertEquals("required", page.getHtmlElementById("messages").asText()); // It should appear only once!
        
        HtmlRadioButtonInput inDataTableWithEntityListRadio = (HtmlRadioButtonInput) page.getHtmlElementById("inDataTableWithEntityList:table:1:radio");
        inDataTableWithEntityListRadio.setChecked(true);
        page = ((HtmlSubmitInput) page.getHtmlElementById("inDataTableWithEntityList:button")).click();
        
        System.out.println("\n\n***** " + page.getHtmlElementById("messages").asText() + "\n\n\n");
        
        assertTrue(page.getHtmlElementById("messages").asText().isEmpty());
        assertEquals("two", page.getHtmlElementById("inDataTableWithEntityList:selectedItem").asText());

        page = ((HtmlSubmitInput) page.getHtmlElementById("inRepeatWithSelectItemList:button")).click();
        assertEquals("required", page.getHtmlElementById("messages").asText()); // It should appear only once!

        HtmlRadioButtonInput inRepeatWithSelectItemListRadio = (HtmlRadioButtonInput) page.getHtmlElementById("inRepeatWithSelectItemList:repeat:1:radio");
        inRepeatWithSelectItemListRadio.setChecked(true);
        page = ((HtmlSubmitInput) page.getHtmlElementById("inRepeatWithSelectItemList:button")).click();
        assertTrue(page.getHtmlElementById("messages").asText().isEmpty());
        assertEquals("value2", page.getHtmlElementById("inRepeatWithSelectItemList:selectedItem").asText());

        page = ((HtmlSubmitInput) page.getHtmlElementById("multipleRadioButtonsWithStaticItemsInFirstRadio:button")).click();
        assertEquals("required1", page.getHtmlElementById("messages").asText()); // It should appear only once for first component!

        HtmlRadioButtonInput multipleRadioButtonsWithStaticItemsInFirstRadio = (HtmlRadioButtonInput) page.getHtmlElementById("multipleRadioButtonsWithStaticItemsInFirstRadio:radio2");
        multipleRadioButtonsWithStaticItemsInFirstRadio.setChecked(true);
        page = ((HtmlSubmitInput) page.getHtmlElementById("multipleRadioButtonsWithStaticItemsInFirstRadio:button")).click();
        assertTrue(page.getHtmlElementById("messages").asText().isEmpty());
        assertEquals("two", page.getHtmlElementById("multipleRadioButtonsWithStaticItemsInFirstRadio:selectedItem").asText());

        page = ((HtmlSubmitInput) page.getHtmlElementById("multipleRadioButtonsWithStaticItemsInEachRadio:button")).click();
        assertEquals("required1", page.getHtmlElementById("messages").asText()); // It should appear only once for first component!

        HtmlRadioButtonInput multipleRadioButtonsWithStaticItemsInEachRadio = (HtmlRadioButtonInput) page.getHtmlElementById("multipleRadioButtonsWithStaticItemsInEachRadio:radio2");
        multipleRadioButtonsWithStaticItemsInEachRadio.setChecked(true);
        page = ((HtmlSubmitInput) page.getHtmlElementById("multipleRadioButtonsWithStaticItemsInEachRadio:button")).click();
        assertTrue(page.getHtmlElementById("messages").asText().isEmpty());
        assertEquals("two", page.getHtmlElementById("multipleRadioButtonsWithStaticItemsInEachRadio:selectedItem").asText());

        page = ((HtmlSubmitInput) page.getHtmlElementById("multipleRadioButtonsWithSelectItemList:button")).click();
        assertEquals("required1", page.getHtmlElementById("messages").asText()); // It should appear only once for first component!

        HtmlRadioButtonInput multipleRadioButtonsWithSelectItemListRadio = (HtmlRadioButtonInput) page.getHtmlElementById("multipleRadioButtonsWithSelectItemList:radio2");
        multipleRadioButtonsWithSelectItemListRadio.setChecked(true);
        page = ((HtmlSubmitInput) page.getHtmlElementById("multipleRadioButtonsWithSelectItemList:button")).click();
        assertTrue(page.getHtmlElementById("messages").asText().isEmpty());
        assertEquals("value2", page.getHtmlElementById("multipleRadioButtonsWithSelectItemList:selectedItem").asText());
    }

    @After
    public void tearDown() {
        webClient.close();
    }

}
