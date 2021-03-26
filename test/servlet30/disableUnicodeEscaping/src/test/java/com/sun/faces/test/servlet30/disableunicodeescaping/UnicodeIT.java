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

package com.sun.faces.test.servlet30.disableunicodeescaping;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_1_4;
import static com.sun.faces.test.junit.JsfServerExclude.WEBLOGIC_12_2_1;
import com.sun.faces.test.junit.JsfTest;
import com.sun.faces.test.junit.JsfTestRunner;
import static com.sun.faces.test.junit.JsfVersion.JSF_2_3_0_M03;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JsfTestRunner.class)
public class UnicodeIT {

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

    @JsfTest(value = JSF_2_3_0_M03, excludes = {WEBLOGIC_12_1_4, WEBLOGIC_12_2_1})
    @Test
    public void testUnicodeEscapingTrue() throws Exception {
        webClient = new WebClient();
        webClient.addRequestHeader("Accept-Encoding", "UTF-8");
        HtmlPage page = webClient.getPage(webUrl + "faces/indexUTF.jsp?escape=true");
        assertTrue(
                "Title should contain the unicode characters '\u1234' and '\u00c4'.",
                page.getWebResponse().getContentAsString().contains("a\u1234a")
                && !page.getWebResponse().getContentAsString().contains("a&#4660;a")
                && page.getWebResponse().getContentAsString().contains("b\u00c4b")
                && !page.getWebResponse().getContentAsString().contains("b&Auml;b"));

        webClient = new WebClient();
        webClient.addRequestHeader("Accept-Encoding", "US-ASCII");
        page = webClient.getPage(webUrl + "faces/indexUSASCII.jsp?escape=true");
        assertTrue(
                "Title should contain the unicode characters replaced by ?.",
                !page.getWebResponse().getContentAsString().contains("a\u1234a")
                && page.getWebResponse().getContentAsString().contains("a?a")
                && !page.getWebResponse().getContentAsString().contains("b\u00c4b")
                && page.getWebResponse().getContentAsString().contains("b?b"));

        webClient = new WebClient();
        webClient.addRequestHeader("Accept-Encoding", "ISO-8859-1");
        page = webClient.getPage(webUrl + "faces/indexISO8859_1.jsp?escape=true");
        assertTrue(
                "Title should contain the unicode character replaced by ? but the correct iso character.",
                !page.getWebResponse().getContentAsString().contains("a\u1234a")
                && page.getWebResponse().getContentAsString().contains("a?a")
                && page.getWebResponse().getContentAsString().contains("b\u00c4b")
                && !page.getWebResponse().getContentAsString().contains("b&Auml;b"));
    }

    @Test
    public void testUnicodeEscapingFalse() throws Exception {
        webClient = new WebClient();
        webClient.addRequestHeader("Accept-Encoding", "UTF-8");
        HtmlPage page = webClient.getPage(webUrl + "faces/indexUTF.jsp?escape=false");
        assertTrue(
                "Title should contain the escaped unicode characters only.",
                !page.getWebResponse().getContentAsString().contains("a\u1234a")
                && page.getWebResponse().getContentAsString().contains("a&#4660;a")
                && !page.getWebResponse().getContentAsString().contains("b\u00c4b")
                && page.getWebResponse().getContentAsString().contains("b&Auml;b"));

        webClient = new WebClient();
        webClient.addRequestHeader("Accept-Encoding", "US-ASCII");
        page = webClient.getPage(webUrl + "faces/indexUSASCII.jsp?escape=false");
        assertTrue(
                "Title should contain the escaped unicode characters only.",
                !page.getWebResponse().getContentAsString().contains("a\u1234a")
                && page.getWebResponse().getContentAsString().contains("a&#4660;a")
                && !page.getWebResponse().getContentAsString().contains("b\u00c4b")
                && page.getWebResponse().getContentAsString().contains("b&Auml;b"));

        webClient = new WebClient();
        webClient.addRequestHeader("Accept-Encoding", "ISO-8859-1");
        page = webClient.getPage(webUrl + "faces/indexISO8859_1.jsp?escape=false");
        assertTrue(
                "Title should contain the escaped unicode characters only.",
                !page.getWebResponse().getContentAsString().contains("a\u1234a")
                && page.getWebResponse().getContentAsString().contains("a&#4660;a")
                && !page.getWebResponse().getContentAsString().contains("b\u00c4b")
                && page.getWebResponse().getContentAsString().contains("b&Auml;b"));
    }

    @JsfTest(value = JSF_2_3_0_M03, excludes = {WEBLOGIC_12_1_4, WEBLOGIC_12_2_1})
    @Test
    public void testUnicodeEscapingAuto() throws Exception {
        webClient = new WebClient();
        webClient.addRequestHeader("Accept-Encoding", "UTF-8");
        HtmlPage page = webClient.getPage(webUrl + "faces/indexUTF.jsp?escape=auto");
        assertTrue(
                "Title should contain the unicode characters '\u1234' and '\u00c4'.",
                page.getWebResponse().getContentAsString().contains("a\u1234a")
                && !page.getWebResponse().getContentAsString().contains("a&#4660;a")
                && page.getWebResponse().getContentAsString().contains("b\u00c4b")
                && !page.getWebResponse().getContentAsString().contains("b&Auml;b"));

        webClient = new WebClient();
        webClient.addRequestHeader("Accept-Encoding", "US-ASCII");
        page = webClient.getPage(webUrl + "faces/indexUSASCII.jsp?escape=auto");
        assertTrue(
                "Title should contain the escaped entity '&#4660;' and the escaped umlaut a.",
                !page.getWebResponse().getContentAsString().contains("a\u1234a")
                && page.getWebResponse().getContentAsString().contains("a&#4660;a")
                && !page.getWebResponse().getContentAsString().contains("b\u00c4b")
                && page.getWebResponse().getContentAsString().contains("b&Auml;b"));

        webClient = new WebClient();
        webClient.addRequestHeader("Accept-Encoding", "ISO-8859-1");
        page = webClient.getPage(webUrl + "faces/indexISO8859_1.jsp?escape=auto");
        assertTrue(
                "Title should contain the escaped entity '&#4660;' and the correct iso character.",
                !page.getWebResponse().getContentAsString().contains("a\u1234a")
                && page.getWebResponse().getContentAsString().contains("a&#4660;a")
                && page.getWebResponse().getContentAsString().contains("b\u00c4b")
                && !page.getWebResponse().getContentAsString().contains("b&Auml;b"));
    }
}
