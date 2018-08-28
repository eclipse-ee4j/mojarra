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

package com.sun.faces.test.servlet30.renderkit;

import com.gargoylesoftware.htmlunit.html.HtmlOption;
import java.util.List;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.*;
import static org.junit.Assert.*;

public class Issue1089IT {

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
    public void testDataAttributes() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/data-attributes.xhtml");
        String pageMarkup = page.getBody().asXml();
        assertTrue(pageMarkup.contains("data-development=\"/data-attributes.xhtml\""));
        assertTrue(pageMarkup.contains("data-name=\"value\""));
        assertTrue(pageMarkup.contains("data-outer-inner=\"innerValue\""));
        assertTrue(pageMarkup.contains("data-a-b-c-d=\"e\""));
        assertTrue(pageMarkup.contains("data-a-b-c-f=\"g\""));
        assertTrue(pageMarkup.contains("data-a-b-c-h=\"i\""));
        assertTrue(pageMarkup.contains("data-a-b-c-j-k-l-m=\"n\""));

    }

    @Test
    public void testPassThroughAttributes() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/passThroughAttributes.xhtml");
        String pageMarkup = page.getBody().asXml();
        assertTrue(pageMarkup.contains("elname=\"/passThroughAttributes.xhtml\""));
        assertTrue(pageMarkup.contains("literalname=\"literalValue\""));
        assertTrue(!pageMarkup.contains("xmlns:p=\"http://java.sun.com/jsf/passthrough\""));
        assertTrue(pageMarkup.contains("foo=\"bar\""));

    }

    @Test
    public void testAttributes() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/attributes.xhtml");
        String pageMarkup = page.getBody().asXml();
        assertTrue(pageMarkup.contains("class=\"a b c\""));
        assertTrue(pageMarkup.contains("size=\"1\""));

    }

    @Test
    public void testSelectPassThroughAttributesMarkup() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/passThroughAttributesSelect.xhtml");

        HtmlSelect select = (HtmlSelect) page.getElementById("form_selectOne:selectOne_selectItem_passThroughAttribute");
        String xml = select.asXml();

        assertTrue(xml.contains("data-apple=\"apple-data\""));
        assertTrue(xml.contains("data-apple=\"apple-data\""));
        assertTrue(xml.contains("data-orange=\"orange-data\""));
        assertTrue(xml.contains("data-pear=\"pear-data\""));

        select = (HtmlSelect) page.getElementById("form_selectOne:selectOne_selectItem_passThroughAttributes_stringArray");
        xml = select.asXml();
        assertTrue(xml.contains("data-array=\"array-data\""));

        select = (HtmlSelect) page.getElementById("form_selectOne:selectOne_selectItem_passThroughAttribute_beanCollection");
        xml = select.asXml();
        assertTrue(xml.contains("data-collection=\"collection-data\""));

        select = (HtmlSelect) page.getElementById("form_selectOne:selectOne_selectItem_selectItemGrouped");
        xml = select.asXml();
        assertTrue(xml.matches("(?s).*select.*optgroup.*option.*optgroup.*optgroup.*option.*"));

        select = (HtmlSelect) page.getElementById("form_selectMany:selectMany_selectItem_passThroughAttributes_stringArray");
        xml = select.asXml();
        assertTrue(xml.contains("data-array=\"array-data\""));

        select = (HtmlSelect) page.getElementById("form_selectMany:selectMany_selectItem_passThroughAttribute_beanCollection");
        xml = select.asXml();
        assertTrue(xml.contains("data-collection=\"collection-data\""));

        select = (HtmlSelect) page.getElementById("form_selectMany:selectMany_selectItem_selectItemGrouped");
        xml = select.asXml();
        assertTrue(xml.contains("data-grouped=\"grouped-data\""));
        assertTrue(xml.matches(
                "(?s).*select.*optgroup.*data-grouped=\"grouped-data\".*option.*option.*option.*option.*option.*option.*optgroup.*optgroup.*option.*option.*option.*option.*optgroup.*select.*"));

    }

    @Test
    public void testSelectOnePassThroughAttributesBehavior() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/passThroughAttributesSelect.xhtml");

        HtmlSelect select = (HtmlSelect) page.getElementById("form_selectOne:selectOne_selectItem_passThroughAttribute");
        List<HtmlOption> options = select.getOptions();
        HtmlOption last = options.get(options.size() - 1);
        select.setSelectedAttribute(last, true);

        select = (HtmlSelect) page.getElementById("form_selectOne:selectOne_selectItem_passThroughAttributes_stringArray");
        options = select.getOptions();
        last = options.get(options.size() - 1);
        select.setSelectedAttribute(last, true);

        select = (HtmlSelect) page.getElementById("form_selectOne:selectOne_selectItem_passThroughAttribute_beanCollection");
        options = select.getOptions();
        last = options.get(options.size() - 1);
        select.setSelectedAttribute(last, true);

        select = (HtmlSelect) page.getElementById("form_selectOne:selectOne_selectItem_selectItemGrouped");
        options = select.getOptions();
        last = options.get(options.size() - 1);
        select.setSelectedAttribute(last, true);

        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("form_selectOne:button");

        page = button.click();
        String text = page.asText();

        assertTrue(text.contains("Current fruitValue: Pear"));
        assertTrue(text.contains("Current nameValue: Brent"));
        assertTrue(text.contains("Current hobbitValue: Pippin"));
        assertTrue(text.contains("Current groupedNameValue: Billy"));
    }

    @Test
    public void testSelectManyPassThroughAttributesBehavior() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/passThroughAttributesSelect.xhtml");

        HtmlSelect select = (HtmlSelect) page.getElementById("form_selectMany:selectMany_selectItem_passThroughAttributes_stringArray");
        List<HtmlOption> options = select.getOptions();
        HtmlOption nextToLast = options.get(options.size() - 2), last = options.get(options.size() - 1);
        select.setSelectedAttribute(nextToLast, true);
        select.setSelectedAttribute(last, true);

        select = (HtmlSelect) page.getElementById("form_selectMany:selectMany_selectItem_passThroughAttribute_beanCollection");
        options = select.getOptions();
        nextToLast = options.get(options.size() - 2);
        last = options.get(options.size() - 1);
        select.setSelectedAttribute(nextToLast, true);
        select.setSelectedAttribute(last, true);

        select = (HtmlSelect) page.getElementById("form_selectMany:selectMany_selectItem_selectItemGrouped");
        options = select.getOptions();
        nextToLast = options.get(options.size() - 2);
        last = options.get(options.size() - 1);
        select.setSelectedAttribute(nextToLast, true);
        select.setSelectedAttribute(last, true);

        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("form_selectMany:button");
        page = button.click();

        String text = page.asText();

        assertTrue(text.contains("Current nameValueList: Billy Brent"));
        assertTrue(text.contains("Current hobbitList: Merry Pippin"));
        assertTrue(text.contains("Current groupedItems: Mickey Billy"));

    }

}
