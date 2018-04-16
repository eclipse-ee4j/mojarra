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

package com.sun.faces.test.servlet30.nestedloadbundles;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import static java.util.Collections.list;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * <p>
 * Make sure loadBundle works as expected in JSF 1.2</p>
 */
public class NestedLoadBundlesIT {

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
    public void testNestedLoadBundles() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/index.jsp");
        HtmlSubmitInput button;

        String pageText = page.asText();
        assertNotNull(pageText);
        assertTrue(-1 != pageText.indexOf("Output 01 from bundle: Bundle A"));
        assertTrue(-1 != pageText.indexOf("Output 02 from bundle: Bundle B"));
        assertTrue(-1 != pageText.indexOf("Output 03 from bundle: Bundle C"));
        assertTrue(-1 != pageText.indexOf("Output 04 from bundle: Bundle C"));
        assertTrue(-1 != pageText.indexOf("Output 05 from bundle: Bundle D"));
        assertTrue(-1 != pageText.indexOf("Output 06 from bundle: Bundle E"));

        button = (HtmlSubmitInput) page.getHtmlElementById("button");
        assertTrue(0 == button.getValueAttribute().indexOf("Bundle E"));
        page = (HtmlPage) button.click();

        pageText = page.asText();
        assertNotNull(pageText);
        assertTrue(-1 != pageText.indexOf("Output 01 from bundle: Bundle A"));
        assertTrue(-1 != pageText.indexOf("Output 02 from bundle: Bundle B"));
        assertTrue(-1 != pageText.indexOf("Output 03 from bundle: Bundle C"));
        assertTrue(-1 != pageText.indexOf("Output 04 from bundle: Bundle C"));
        assertTrue(-1 != pageText.indexOf("Output 05 from bundle: Bundle D"));
        assertTrue(-1 != pageText.indexOf("Output 06 from bundle: Bundle E"));

        button = (HtmlSubmitInput) page.getHtmlElementById("button");
        assertTrue(0 == button.getValueAttribute().indexOf("Bundle E"));
        page = (HtmlPage) button.click();
    }
}
