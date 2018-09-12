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

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.DomNodeList;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlListItem;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.gargoylesoftware.htmlunit.html.HtmlUnorderedList;

public class CompositeComponentsIT {

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
    public void testNesting05() throws Exception {

        HtmlPage page = webClient.getPage(webUrl + "faces/composite/nesting05.xhtml");
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getHtmlElementById("nesting6:nesting7:form1:command");
        page = submit.click();
        assertTrue(page.asText().contains("Action invoked"));

        page = webClient.getPage(webUrl + "faces/composite/nesting05.xhtml");
        submit = (HtmlSubmitInput) page.getHtmlElementById("nesting6:nesting7:form2:command2");
        page = submit.click();
        assertTrue(page.asText().contains("ActionListener invoked"));

        page = webClient.getPage(webUrl + "faces/composite/nesting05.xhtml");
        submit = (HtmlSubmitInput) page.getHtmlElementById("nesting6:nesting7:form3:command3");
        page = submit.click();
        assertTrue(page.asText().contains("Custom action invoked"));

        page = webClient.getPage(webUrl + "faces/composite/nesting05.xhtml");
        submit = (HtmlSubmitInput) page.getHtmlElementById("nesting6:nesting7:form4:command");
        HtmlTextInput text = (HtmlTextInput) page.getHtmlElementById("nesting6:nesting7:form4:input");
        text.setValueAttribute("foo");
        page = submit.click();
        assertTrue(page.asText().contains("validator invoked"));

        page = webClient.getPage(webUrl + "faces/composite/nesting05.xhtml");
        submit = (HtmlSubmitInput) page.getHtmlElementById("nesting6:nesting7:form5:command");
        page = submit.click();
        assertTrue(page.asText().contains("ValueChange invoked"));
    }

    /**
     * Added for issue 1255.
     *
     * @throws Exception when an error occurs.
     */
    @Test
    public void testNesting08() throws Exception {

        HtmlPage page = webClient.getPage(webUrl + "faces/composite/nesting06.xhtml");
        HtmlSubmitInput submit = (HtmlSubmitInput) page.getHtmlElementById("nesting10:nesting6:nesting7:form1:command");
        page = submit.click();
        assertTrue(page.asText().contains("Action invoked"));

        page = webClient.getPage(webUrl + "faces/composite/nesting06.xhtml");
        submit = (HtmlSubmitInput) page.getHtmlElementById("nesting10:nesting6:nesting7:form2:command2");
        page = submit.click();
        assertTrue(page.asText().contains("ActionListener invoked"));

        page = webClient.getPage(webUrl + "faces/composite/nesting06.xhtml");
        submit = (HtmlSubmitInput) page.getHtmlElementById("nesting10:nesting6:nesting7:form3:command3");
        page = submit.click();
        assertTrue(page.asText().contains("Custom action invoked"));

        page = webClient.getPage(webUrl + "faces/composite/nesting06.xhtml");
        submit = (HtmlSubmitInput) page.getHtmlElementById("nesting10:nesting6:nesting7:form4:command");
        HtmlTextInput text = (HtmlTextInput) page.getHtmlElementById("nesting10:nesting6:nesting7:form4:input");
        text.setValueAttribute("foo");
        page = submit.click();
        assertTrue(page.asText().contains("validator invoked"));

        page = webClient.getPage(webUrl + "faces/composite/nesting06.xhtml");
        submit = (HtmlSubmitInput) page.getHtmlElementById("nesting10:nesting6:nesting7:form5:command");
        page = submit.click();
        assertTrue(page.asText().contains("ValueChange invoked"));
    }

    /**
     * <p>
     * Maps Validator to inputText within composite/validator1.xhtml using only the name attribute.
     * </p>
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testValidator1() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/preflight.xhtml");
        /*
         * When systest migrated this test was found not to be working on client side state saving and when
         * serializing the server state.
         */
        if (!page.asXml().contains("State Saving Method: client") && !page.asXml().contains("Serializing Server State: true")) {
            page = webClient.getPage(webUrl + "faces/composite/attachedvalidator.xhtml");
            validateValidatorMessagePresent(page, "form:s1", "form:validator1:input");
        }
    }

    private void validateValidatorMessagePresent(HtmlPage page, String commandId, String inputId) throws Exception {
        page = pushButton(page, commandId);
        validateMessage(page, "Validator Invoked", inputId);
    }

    private void validateMessage(HtmlPage page, String messagePrefix, String messageSuffix) {

        DomNodeList<HtmlElement> list = page.getBody().getElementsByTagName("ul");
        HtmlUnorderedList ulist = (HtmlUnorderedList) list.get(0);
        assertEquals("messages", ulist.getId());
        int count = 0;
        String message = (messagePrefix + " : " + messageSuffix);
        for (DomElement e : ulist.getChildElements()) {
            if (count > 1) {
                fail("Expected only one message to be displayed");
            }
            count++;
            assertTrue(e instanceof HtmlListItem);
            assertEquals(message, message, e.asText());
        }

        if (list.size() == 2) {
            ulist = (HtmlUnorderedList) list.get(1);
            for (DomElement e : ulist.getChildElements()) {
                fail("Messages have been redisplayed");
            }
        }
    }

    private HtmlPage pushButton(HtmlPage page, String commandId) throws Exception {
        HtmlSubmitInput input = (HtmlSubmitInput) page.getHtmlElementById(commandId);
        assertNotNull(input);
        return (HtmlPage) input.click();
    }

    /**
     * Added for issue 1298.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    @Ignore
    public void testMethodExpressionNesting() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/preflight.xhtml");
        /*
         * When systest migrated this test was found not to be working on client side state saving and when
         * serializing the server state.
         */
        if (!page.asXml().contains("State Saving Method: client") && !page.asXml().contains("Serializing Server State: true")) {
            page = webClient.getPage(webUrl + "faces/composite/nesting08.xhtml");
            HtmlForm form = page.getForms().get(0);
            HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("form:submit");
            assertNotNull(button);
            page = button.click();
            assertTrue(page.asText().contains("Action invoked"));
        }
    }

    // issue 1696
    @Test
    public void testForNoNPE() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/composite/simpleCompositeComponentUsingPage.xhtml");
        if (page.asXml().contains("Development")) {
            HtmlSubmitInput element = (HtmlSubmitInput) page.getHtmlElementById("form:submit");
            page = element.click();
            String pageAsText = page.asText();
            assertTrue(pageAsText.contains("Unable to find matching navigation case with from-view-id "
                    + "'/composite/simpleCompositeComponentUsingPage.xhtml' for action '#{hello.getNextAction}' "
                    + "with outcome '/submit.xhtml'"));
        }
    }

    /**
     * <p>
     * Maps ActionListener to commandButton within composite/actionSource1.xhtml using only the name
     * attribute.
     * </p>
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testActionSource1() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/preflight.xhtml");
        /*
         * When systest migrated this test was found not to be working on client side state saving and when
         * serializing the server state.
         */
        if (!page.asXml().contains("State Saving Method: client") && !page.asXml().contains("Serializing Server State: true")) {
            page = webClient.getPage(webUrl + "faces/composite/actionsource.xhtml");
            validateActionMessagePresent(page, "form:actionsource1:command");
        }
    }

    /**
     * <p>
     * Maps ActionListener to commandButton within composite/actionSource2.xhtml using name and target
     * attributes.
     * </p>
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testActionSource2() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/preflight.xhtml");
        /*
         * When systest migrated this test was found not to be working on client side state saving and when
         * serializing the server state.
         */
        if (!page.asXml().contains("State Saving Method: client") && !page.asXml().contains("Serializing Server State: true")) {
            page = webClient.getPage(webUrl + "faces/composite/actionsource.xhtml");
            validateActionMessagePresent(page, "form:actionsource2:ac2");
        }
    }

    /**
     * <p>
     * Maps ActionListener to a commandButton within a composite/actionSource1.xhtml which is nested
     * within composite/actionSource3.xhtml. Using the same ID in the nesting.
     * </p>
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testActionSource3() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/preflight.xhtml");
        /*
         * When systest migrated this test was found not to be working on client side state saving and when
         * serializing the server state.
         */
        if (!page.asXml().contains("State Saving Method: client") && !page.asXml().contains("Serializing Server State: true")) {
            page = webClient.getPage(webUrl + "faces/composite/actionsource.xhtml");
            validateActionMessagePresent(page, "form:actionsource3:command:command");
        }
    }

    /**
     * <p>
     * Ensure actionListeners are properly re-targeted when the target of the actionListener is nested
     * within another naming container. Note that the value of the 'for' attribute doesn't mimic the
     * NamingContainer hierarchy, that's handled by the 'targets' attribute within the
     * composite:implementation section of actionSource4.xhtml.
     * </p>
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testActionSource4() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/preflight.xhtml");
        /*
         * When systest migrated this test was found not to be working on client side state saving and when
         * serializing the server state.
         */
        if (!page.asXml().contains("State Saving Method: client") && !page.asXml().contains("Serializing Server State: true")) {
            page = webClient.getPage(webUrl + "faces/composite/actionsource.xhtml");
            validateActionMessagePresent(page, "form:actionsource4:naming:command");
        }
    }

    private void validateActionMessagePresent(HtmlPage page, String commandId) throws Exception {
        page = pushButton(page, commandId);
        validateMessage(page, "Action Invoked", commandId);
    }

    /**
     * <p>
     * Maps Validator to inputText within composite/validator2.xhtml using name and target attributes.
     * </p>
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testValidator2() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/preflight.xhtml");
        /*
         * When systest migrated this test was found not to be working on client side state saving and when
         * serializing the server state.
         */
        if (!page.asXml().contains("State Saving Method: client") && !page.asXml().contains("Serializing Server State: true")) {
            page = webClient.getPage(webUrl + "faces/composite/attachedvalidator.xhtml");
            validateValidatorMessagePresent(page, "form2:s2", "form2:validator2:it2");
        }
    }

    /**
     * <p>
     * Maps Validator to inputText within composite/validator2.xhtml using name and target attributes.
     * </p>
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testValidator3() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/preflight.xhtml");
        /*
         * When systest migrated this test was found not to be working on client side state saving and when
         * serializing the server state.
         */
        if (!page.asXml().contains("State Saving Method: client") && !page.asXml().contains("Serializing Server State: true")) {
            page = webClient.getPage(webUrl + "faces/composite/attachedvalidator.xhtml");
            validateValidatorMessagePresent(page, "form3:s3", "form3:validator3:input:input");
        }
    }

    /**
     * <p>
     * Ensure validators are properly re-targeted when the target of the validator is nested within
     * another naming container. Note that the value of the 'for' attribute doesn't mimic the
     * NamingContainer hierarchy, that's handled by the 'targets' attribute within the
     * composite:implementation section of validator4.xhtml.
     * </p>
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testValidator4() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/preflight.xhtml");
        /*
         * When systest migrated this test was found not to be working on client side state saving and when
         * serializing the server state.
         */
        if (!page.asXml().contains("State Saving Method: client") && !page.asXml().contains("Serializing Server State: true")) {
            page = webClient.getPage(webUrl + "faces/composite/attachedvalidator.xhtml");
            validateValidatorMessagePresent(page, "form4:s4", "form4:validator4:naming:input");
        }
    }

    /**
     * <p>
     * Maps Converter to inputText within composite/validator1.xhtml using only the name attribute.
     * </p>
     *
     * <p>
     * Maps Converter to inputText within composite/validator2.xhtml using name and target attributes.
     * </p>
     *
     * <p>
     * Maps Converter to inputText within composite/validator2.xhtml using name and target attributes.
     * </p>
     *
     * <p>
     * Ensure validators are properly re-targeted when the target of the validator is nested within
     * another naming container. Note that the value of the 'for' attribute doesn't mimic the
     * NamingContainer hierarchy, that's handled by the 'targets' attribute within the
     * composite:implementation section of validator4.xhtml.
     * </p>
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testConverters() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/preflight.xhtml");
        /*
         * When systest migrated this test was found not to be working on client side state saving and when
         * serializing the server state.
         */
        if (!page.asXml().contains("State Saving Method: client") && !page.asXml().contains("Serializing Server State: true")) {

            String[] messageSuffixes = new String[] { "form:converter1:input", "form2:converter2:it2", "form3:converter3:input:input",
                    "form4:converter4:naming:input" };

            page = webClient.getPage(webUrl + "faces/composite/attachedconverter.xhtml");
            validateConverterMessages(page, messageSuffixes);
            page = pushButton(page, "cf:clear");
            validateConverterMessages(page, messageSuffixes);
        }
    }

    private void validateConverterMessages(HtmlPage page, String[] messageSuffixes) {

        List<HtmlElement> list = page.getBody().getElementsByTagName("ul");

        HtmlUnorderedList ulist = (HtmlUnorderedList) list.get(0);
        assertEquals("messages", ulist.getId());
        int count = 0;

        for (DomElement e : ulist.getChildElements()) {
            if (count > messageSuffixes.length) {
                fail("Expected only four message to be displayed");
            }
            String message = ("Converter Invoked : " + messageSuffixes[count]);
            count++;
            assertTrue(e instanceof HtmlListItem);
            assertEquals(message, message, e.asText());
        }

        if (list.size() == 2) {
            ulist = (HtmlUnorderedList) list.get(1);
            for (DomElement e : ulist.getChildElements()) {
                fail("Messages have been redisplayed");
            }
        }
    }
}
