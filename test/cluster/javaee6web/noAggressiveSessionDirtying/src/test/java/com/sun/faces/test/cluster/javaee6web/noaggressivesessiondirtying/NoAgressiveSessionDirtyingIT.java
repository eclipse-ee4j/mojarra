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

package com.sun.faces.test.cluster.javaee6web.noaggressivesessiondirtying;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.sun.faces.test.util.ClusterUtils;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class NoAgressiveSessionDirtyingIT {

    private WebClient webClient;

    @Before
    public void setUp() {
        webClient = new WebClient();
    }

    @After
    public void tearDown() {
        webClient.close();
    }

    @Test
    @Ignore
    public void testSimpleObject() throws Exception {
        String[] webUrls = ClusterUtils.getRandomizedBaseUrls();

        HtmlPage page = webClient.getPage(webUrls[0] + "faces/session.xhtml");
        HtmlTextInput input = (HtmlTextInput) page.getElementById("input");
        String inputValue = "simple session value" + System.currentTimeMillis();
        input.setValueAttribute(inputValue);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");
        button.click();

        for (String webUrl : webUrls) {
            page = webClient.getPage(webUrl + "faces/session.xhtml");
            String text = page.asText();
            assertTrue(text.contains(inputValue));
        }
    }

    @Test
    @Ignore
    public void testComplexObject() throws Exception {
        String[] webUrls = ClusterUtils.getRandomizedBaseUrls();

        HtmlPage page = webClient.getPage(webUrls[0] + "faces/sessionComplex.xhtml");
        HtmlTextInput input = (HtmlTextInput) page.getElementById("input");
        String inputValue = "complex session value";
        input.setValueAttribute(inputValue);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");
        button.click();

        for (String webUrl : webUrls) {
            page = webClient.getPage(webUrl + "faces/sessionComplex.xhtml");
            input = (HtmlTextInput) page.getElementById("input");
            String value = input.getValueAttribute();
            assertTrue(value.contains(inputValue));
            assertTrue(inputValue.length() < value.length());
        }
    }
}
