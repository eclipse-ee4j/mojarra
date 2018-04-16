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

package com.sun.faces.test.javaee6web.flowcall;

import com.gargoylesoftware.htmlunit.html.HtmlInput;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class Spec730IT {

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
    public void testFacesFlowCallPostback() throws Exception {
        doTestFacesFlowCall("");
    }

    @Test
    public void testFacesFlowCallGet() throws Exception {
        doTestFacesFlowCall("_GET");
    }

    public void doTestFacesFlowCall(String flowInvocationSuffix) throws Exception {
        HtmlPage page = webClient.getPage(webUrl);

        assertTrue(page.getBody().asText().contains("Outside of flow"));

        HtmlInput button = (HtmlInput) page.getElementById("start_a" + flowInvocationSuffix);
        page = button.click();
        String pageText = page.asText();
        assertTrue(pageText.contains("Flow_a_Bean"));
        assertTrue(pageText.matches("(?s).*Has a flow:\\s+true\\..*"));

        String param1Value = page.getElementById("param1FromFlowB").getTextContent();
        assertEquals("", param1Value);
        String param2Value = page.getElementById("param2FromFlowB").getTextContent();
        assertEquals("", param2Value);

        button = (HtmlInput) page.getElementById("next_a");
        page = button.click();
        pageText = page.asText();
        assertTrue(pageText.contains("Second page in the flow"));

        HtmlTextInput input = (HtmlTextInput) page.getElementById("input");
        String value = "" + System.currentTimeMillis();
        input.setValueAttribute(value);

        button = (HtmlInput) page.getElementById("next");
        page = button.click();

        pageText = page.asText();
        assertTrue(pageText.contains(value));

        button = (HtmlInput) page.getElementById("callB" + flowInvocationSuffix);
        page = button.click();

        pageText = page.asText();
        assertTrue(pageText.contains("Flow_B_Bean"));
        assertTrue(!pageText.contains("Flow_A_Bean"));

        param1Value = page.getElementById("param1FromFlowA").getTextContent();
        assertEquals("param1Value", param1Value);
        param2Value = page.getElementById("param2FromFlowA").getTextContent();
        assertEquals("param2Value", param2Value);

        button = (HtmlInput) page.getElementById("next_a");
        page = button.click();
        pageText = page.asText();
        assertTrue(pageText.contains("Second page in the flow"));

        input = (HtmlTextInput) page.getElementById("input");
        value = "" + System.currentTimeMillis();
        input.setValueAttribute(value);

        button = (HtmlInput) page.getElementById("next");
        page = button.click();

        pageText = page.asText();
        assertTrue(pageText.contains(value));

        button = (HtmlInput) page.getElementById("callA" + flowInvocationSuffix);
        page = button.click();

        param1Value = page.getElementById("param1FromFlowB").getTextContent();
        assertEquals("param1Value", param1Value);
        param2Value = page.getElementById("param2FromFlowB").getTextContent();
        assertEquals("param2Value", param2Value);

        button = (HtmlInput) page.getElementById("next_a");
        page = button.click();
        pageText = page.asText();
        assertTrue(pageText.contains("Second page in the flow"));

        button = (HtmlInput) page.getElementById("next");
        page = button.click();

        button = (HtmlInput) page.getElementById("return" + flowInvocationSuffix);
        page = button.click();

        pageText = page.asText();
        assertTrue(pageText.contains("Flow bean name: Flow_B_Bean"));

        button = (HtmlInput) page.getElementById("next_a");
        page = button.click();

        button = (HtmlInput) page.getElementById("next");
        page = button.click();

        button = (HtmlInput) page.getElementById("return" + flowInvocationSuffix);
        page = button.click();

        pageText = page.asText();
        assertTrue(pageText.contains("Flow bean name: Flow_a_Bean"));

        button = (HtmlInput) page.getElementById("next_a");
        page = button.click();

        button = (HtmlInput) page.getElementById("next");
        page = button.click();

        button = (HtmlInput) page.getElementById("return" + flowInvocationSuffix);
        page = button.click();

        pageText = page.asText();
        assertTrue(pageText.matches("(?s).*flowScope value,\\s+should be empty:\\s+\\..*"));
        assertTrue(pageText.matches("(?s).*Has a flow:\\s+false\\..*"));
    }
}
