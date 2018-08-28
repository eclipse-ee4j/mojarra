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

package com.sun.faces.test.servlet30.passthrough;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.StatusHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButton;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlEmailInput;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlResetInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTelInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.sun.faces.test.htmlunit.IgnoringIncorrectnessListener;

public class Issue1111IT {

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
    public void testInputMarkup() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());
        HtmlPage page = webClient.getPage(webUrl + "faces/input.xhtml");
        assertInputDefaults(page);
    }

    private void assertInputDefaults(HtmlPage page) {
        assertInput(page, "inputText", "type", "text", "value", "text1");
        assertInput(page, "inputText2", "type", "text", "value", "text2");
        assertInput(page, "textField", "type", "text", "value", "text1");
        assertInput(page, "emailField", "type", "email", "value", "anybody@example.com");
        assertInput(page, "numberField", "type", "number", "value", "10", "pattern", "[0-9]*");
        assertInput(page, "checkBox", "type", "checkbox");
        HtmlCheckBoxInput checkBoxInput = (HtmlCheckBoxInput) page.getElementById("checkBox");
        assertFalse(checkBoxInput.isChecked());
    }

    @Test
    public void testInputPostback() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());
        HtmlPage page = webClient.getPage(webUrl + "faces/input.xhtml");

        setValue(page, "inputText2", "new text2");
        setValue(page, "textField", "new text1");
        setValue(page, "emailField", "nobody@example.com");
        setValue(page, "numberField", "12");
        HtmlCheckBoxInput checkBoxInput = (HtmlCheckBoxInput) page.getElementById("checkBox");
        checkBoxInput.setChecked(true);

        HtmlResetInput resetButton = (HtmlResetInput) page.getElementById("resetButton");
        page = resetButton.click();

        assertInputDefaults(page);

        setValue(page, "inputText2", "new text2");
        setValue(page, "textField", "new text1");
        setValue(page, "emailField", "nobody@example.com");
        setValue(page, "numberField", "12");
        checkBoxInput.setChecked(true);

        HtmlSubmitInput submitButton = (HtmlSubmitInput) page.getElementById("submitButton");
        page = submitButton.click();

        assertInput(page, "inputText", "type", "text", "value", "new text1");
        assertInput(page, "inputText2", "type", "text", "value", "new text2");
        assertInput(page, "textField", "type", "text", "value", "new text1");
        assertInput(page, "emailField", "type", "email", "value", "nobody@example.com");
        assertInput(page, "numberField", "type", "number", "value", "12", "pattern", "[0-9]*");

        checkBoxInput = (HtmlCheckBoxInput) page.getElementById("checkBox");
        assertTrue(checkBoxInput.isChecked());
    }

    private void setValue(HtmlPage page, String id, String value) {
        DomElement input = page.getElementById(id);
        input.setAttribute("value", value);
    }

    private void assertInput(HtmlPage page, String id, String... attrs) {
        assertFormElement(page, "input", id, attrs);
    }

    private void assertSelect(HtmlPage page, String id, String... attrs) {
        assertFormElement(page, "select", id, attrs);
    }

    private void assertFormElement(HtmlPage page, String elementName, String id, String... attrs) {
        HtmlElement input = page.getHtmlElementById(id);
        String xml = input.asXml();

        assertTrue(xml.contains("<" + elementName));
        assertTrue(xml.contains("id=\"" + id + "\""));
        assertTrue(xml.contains("name=\"" + id + "\""));

        if (attrs == null) {
            return;
        }
        for (int i = 0; i < attrs.length; i++) {
            String name = attrs[i];
            String value = attrs[++i];
            assertTrue(xml.contains(name + "=\"" + value + "\""));
        }
    }

    @Test
    public void testCauseError() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = webClient.getPage(webUrl + "faces/causeError.xhtml");
        String xml = page.getBody().asXml();
        assertTrue(xml.contains("FaceletException"));
        webClient.getOptions().setThrowExceptionOnFailingStatusCode(true);
    }

    @Test
    public void testSelectMarkup() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());
        HtmlPage page = webClient.getPage(webUrl + "faces/select.xhtml");
        assertSelectAttributes(page);
        assertDefaultSelections(page);
    }

    private void assertDefaultSelections(HtmlPage page) {
        assertSelection(page, "selectOne", "2");
        assertSelection(page, "selectOneSize2", "3");
        assertSelection(page, "selectMany", "4", "6");
    }

    private void assertSelection(HtmlPage page, String id, String... values) {
        HtmlSelect select = (HtmlSelect) page.getElementById(id);
        List<String> valuesAsList = Arrays.asList(values);
        for (HtmlOption option : select.getOptions()) {
            boolean shouldBeSelected = valuesAsList.contains(option.getValueAttribute());

            if (option.isSelected()) {
                assertTrue(shouldBeSelected);
            } else {
                assertFalse(shouldBeSelected);
            }
        }
    }

    private void assertSelectAttributes(HtmlPage page) {
        assertSelect(page, "selectOne", "size", "1");
        assertSelect(page, "selectOneSize2", "size", "2");
        assertSelect(page, "selectMany", "size", "7", "multiple", "multiple");
    }

    @Test
    public void testSelectPostback() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());
        HtmlPage page = webClient.getPage(webUrl + "faces/select.xhtml");

        select(page, "selectOne", "3");
        select(page, "selectOneSize2", "5");
        select(page, "selectMany", "1", "2");

        HtmlResetInput resetButton = (HtmlResetInput) page.getElementById("resetButton");
        page = resetButton.click();

        assertSelectAttributes(page);
        assertDefaultSelections(page);

        select(page, "selectOne", "3");
        select(page, "selectOneSize2", "5");
        select(page, "selectMany", "1", "2");

        HtmlSubmitInput submitButton = (HtmlSubmitInput) page.getElementById("submitButton");
        page = submitButton.click();

        assertSelectAttributes(page);

        assertSelection(page, "selectOne", "3");
        assertSelection(page, "selectOneSize2", "5");
        assertSelection(page, "selectMany", "1", "2");
    }

    private void select(HtmlPage page, String id, String... values) {
        HtmlSelect select = (HtmlSelect) page.getElementById(id);
        List<String> valuesAsList = Arrays.asList(values);

        for (HtmlOption option : select.getOptions()) {
            option.setSelected(valuesAsList.contains(option.getValueAttribute()));
        }
    }

    @Test
    public void testTextareaMarkup() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());
        HtmlPage page = webClient.getPage(webUrl + "faces/textarea.xhtml");
        assertFormElement(page, "textarea", "textarea", "autofocus", "autofocus");
        HtmlTextArea textarea = (HtmlTextArea) page.getElementById("textarea");
        assertEquals(textarea.getText(), "Long text");
    }

    @Test
    public void testTextareaPostback() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());
        HtmlPage page = webClient.getPage(webUrl + "faces/textarea.xhtml");
        HtmlTextArea textarea = (HtmlTextArea) page.getElementById("textarea");
        textarea.setText("Very long text");

        HtmlSubmitInput submitButton = (HtmlSubmitInput) page.getElementById("submitButton");
        page = submitButton.click();

        assertFormElement(page, "textarea", "textarea", "autofocus", "autofocus");

        textarea = (HtmlTextArea) page.getElementById("textarea");
        assertEquals("Very long text", textarea.getText());
    }

    @Test
    public void testButton() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());
        HtmlPage page = webClient.getPage(webUrl + "faces/button.xhtml");
        assertFormElement(page, "button", "fancyButton1");
        assertFormElement(page, "button", "fancyButton2");
        String lastAction = page.getElementById("lastAction").getTextContent();
        assertEquals("", lastAction);

        page = page.getHtmlElementById("fancyButton1").click();

        lastAction = page.getElementById("lastAction").getTextContent();
        assertEquals("action1", lastAction);

        page = page.getHtmlElementById("fancyButton2").click();

        lastAction = page.getElementById("lastAction").getTextContent();
        assertEquals("action2", lastAction);

        page = webClient.getPage(webUrl + "faces/button.xhtml");
        HtmlButton button = (HtmlButton) page.getElementById("outcomeButton");
        page = button.click();
        String outcome = page.getElementById("lastOutcome").getTextContent();
        assertEquals("outcome1", outcome);
    }

    @Test
    public void testLinks() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());
        HtmlPage page = webClient.getPage(webUrl + "faces/links.xhtml");

        HtmlAnchor link = (HtmlAnchor) page.getElementById("action1");
        page = link.click();

        String lastStr = page.getElementById("lastAction").getTextContent();
        assertEquals("action1", lastStr);

        link = (HtmlAnchor) page.getElementById("action2");
        page = link.click();

        lastStr = page.getElementById("lastAction").getTextContent();
        assertEquals("action2", lastStr);

        link = (HtmlAnchor) page.getElementById("outcome1");
        page = link.click();
        lastStr = page.getElementById("lastOutcome").getTextContent();
        assertEquals("outcome1", lastStr);

    }

    @Test
    public void testComplex() throws Exception {
        webClient.setIncorrectnessListener(new IgnoringIncorrectnessListener());
        HtmlPage page = webClient.getPage(webUrl + "faces/complex.xhtml");

        AjaxWaiter waiter = new AjaxWaiter();
        webClient.setStatusHandler(waiter);

        HtmlTextInput name = (HtmlTextInput) page.getElementById("name");
        name.focus();
        name.setText("Horst");

        HtmlTelInput tel = (HtmlTelInput) page.getElementById("tel");
        tel.focus();

        waiter.waitForSuccess();
        waiter.clear();

        assertEquals("1", page.getElementById("progress").getAttribute("value"));

        tel.setText("4711");

        HtmlEmailInput email = (HtmlEmailInput) page.getElementById("email");
        email.focus();

        waiter.waitForSuccess();
        waiter.clear();

        System.out.println(page.asXml());

        assertEquals("2", page.getElementById("progress").getAttribute("value"));

        email.setText("horst@example.com");
        email.blur();

        waiter.waitForSuccess();

        assertEquals("3", page.getElementById("progress").getAttribute("value"));
    }

    private static class AjaxWaiter implements StatusHandler {

        private String lastMessage;

        private int sleepTime = 10;

        private int maxWaitTime = 60000;

        @Override
        public void statusMessageChanged(Page page, String message) {
            this.lastMessage = message;
        }

        private void waitForSuccess() {
            int diff = 0;
            while (!"success".equals(lastMessage)) {
                if (diff >= maxWaitTime) {
                    fail("waited " + diff + "ms for ajax success");
                }
                try {
                    diff += sleepTime;
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }

        private void clear() {
            lastMessage = null;
        }
    }
}
