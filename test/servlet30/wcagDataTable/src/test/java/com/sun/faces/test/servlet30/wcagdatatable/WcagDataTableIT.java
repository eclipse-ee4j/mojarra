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

package com.sun.faces.test.servlet30.wcagdatatable;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class WcagDataTableIT {

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

    /*
     * <p>Verify that the bean is successfully resolved</p>
     */
    @Test
    public void testReplaceStateManager() throws Exception {
        HtmlPage page = webClient.getPage(webUrl + "faces/index.xhtml");
        String pageText = page.asXml();
        // System.out.println(pageText);
        // (?s) is an "embedded flag expression" for the "DOTALL" operator.
        // It says, "let . match any character including line terminators."
        // Because page.asXml() returns a big string with lots of \r\n chars
        // in it, we need (?s).
        // the page contains a table tag with a frame attribute whose value is hsides.
        assertTrue(pageText.matches("(?s).*<table.*frame=.hsides.*>.*"));
        // the page contains a table tag with a rules attribute whose value is groups.
        assertTrue(pageText.matches("(?s).*<table.*rules..groups.*>.*"));
        // the page contains a table tag with a summary attribute whose value is that string.
        assertTrue(pageText.matches("(?s).*<table.*summary..Code page support in different versions of MS Windows.*>.*"));
        // the page contains a table tag followed immediately by the caption element as follows.
        assertTrue(pageText.matches("(?sm).*<table.*>\\s*<caption>.*CODE-PAGE SUPPORT IN MICROSOFT WINDOWS.*</caption>.*"));
        // the page contains a close caption tag followed immediately by a three colgroup tags as follows.
        assertTrue(pageText.matches("(?sm).*</caption>\\s*" + "<colgroup align=.center.\\s*/>\\s*" + "<colgroup align=.left.\\s*/>\\s*"
                + "<colgroup align=.center.*span=.2.*/>\\s*" + "<colgroup align=.center.*span=.3.*/>.*"));

        // A table with a thead, with a tr with a th scope=col
        assertTrue(pageText.matches("(?sm).*<table.*>.*<thead>\\s*" + "<tr>\\s*" + "<th\\s*scope=.col.*"));

        // A table with a tbody, with a tr with a th scope=row
        assertTrue(pageText.matches("(?sm).*<table.*>.*<tbody>.*" + "<th\\s*scope=.row.*"));

        // A table with a tbody, with a tr with a th scope=row
        assertTrue(pageText.matches("(?sm).*<table.*>.*<tbody>.*" + "</tbody>.*<tbody>.*</tbody>.*</table>.*"));
    }
}
