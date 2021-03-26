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

package com.sun.faces.test.servlet30.charactercombat;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.After;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class CharacterCombatIT {

    private String webUrl;
    private WebClient webClient;

    @Before
    public void setUp() {
        webUrl = System.getProperty("integration.url");
        webClient = new WebClient();
        webClient.getOptions().setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(60000);
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    public void test01() throws Exception {

        HtmlPage page = webClient.getPage(webUrl + "/main.faces");
        HtmlSubmitInput nextButton = (HtmlSubmitInput) page.getElementById("wizard-buttons:next");
        page = nextButton.click();
        String text = page.asText();
        assertTrue(text.contains("Gandalf"));
        assertTrue(text.contains("Frodo"));
        assertTrue(text.contains("Legolas"));

        nextButton = (HtmlSubmitInput) page.getElementById("wizard-buttons:next");
        page = nextButton.click();

        text = page.asXml();
        assertFalse(text.contains("value=\"Gandalf\""));
        assertTrue(text.contains("Frodo"));
        assertTrue(text.contains("Legolas"));

        nextButton = (HtmlSubmitInput) page.getElementById("wizard-buttons:next");
        page = nextButton.click();

        text = page.asText();
        assertTrue(text.matches("(?s).*If\\s*[a-zA-Z]*\\s*and\\s*[a-zA-Z].*winner\\swould be.*[a-zA-Z]*.*"));
    }
}
