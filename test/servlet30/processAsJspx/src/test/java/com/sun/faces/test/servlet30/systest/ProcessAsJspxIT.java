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

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.regex.Pattern;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;

public class ProcessAsJspxIT {

    private final static Pattern XmlDeclaration = Pattern.compile("(?s)^<\\?xml(\\s)*version=.*\\?>.*");
    private final static Pattern XmlDoctype = Pattern.compile("(?s).*<!DOCTYPE.*>.*");
    private final static Pattern XmlPI = Pattern.compile("(?s).*<\\?xml-stylesheet.*\\?>.*");
    private final static Pattern CDATASection = Pattern.compile("(?s).*<!\\[CDATA\\[ .*\\]\\]>.*");
    private final static Pattern Comment = Pattern.compile("(?s).*<!--.*-->.*");
    private final static Pattern EscapedText = Pattern.compile("(?s).*&amp;lt;context-param&amp;gt;.*");
    private final static Pattern NotEscapedText = Pattern.compile("(?s).*&lt;context-param&gt;.*");

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

    private String getRawMarkup(String path) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                webClient.getPage(path).getWebResponse().getContentAsStream()));
        StringBuilder builder = new StringBuilder();
        String cur;
        while (null != (cur = reader.readLine())) {
            builder.append(cur);
        }

        String xml = builder.toString();
        return xml;
    }

    public void testProcessAsXhtml() throws Exception {
        String xml = getRawMarkup("/faces/xhtmlview.xhtml");
        assertTrue(XmlDoctype.matcher(xml).matches());
        assertTrue(XmlDeclaration.matcher(xml).matches());
        assertTrue(XmlPI.matcher(xml).matches());
        assertTrue(CDATASection.matcher(xml).matches());
        assertTrue(EscapedText.matcher(xml).matches());
        assertTrue(Comment.matcher(xml).matches());
    }

    public void testProcessAsXml() throws Exception {

        String xml = getRawMarkup("/faces/xmlview.view.xml");
        assertFalse(XmlDoctype.matcher(xml).matches());
        assertFalse(XmlDeclaration.matcher(xml).matches());
        assertFalse(XmlPI.matcher(xml).matches());
        assertFalse(CDATASection.matcher(xml).matches());
        assertTrue(EscapedText.matcher(xml).matches());
        assertFalse(Comment.matcher(xml).matches());
    }

    public void testProcessAsXmlWithDifferentXmlnsCases() throws Exception {

        String xml = getRawMarkup("/faces/xmlviewWithHtmlRoot.view.xml");
        assertTrue(xml.matches("(?s).*<html.*xmlns=\"http://www.w3.org/1999/xhtml\">.*"));
        xml = getRawMarkup("/faces/xmlviewWithHtmlRootAndXmlnsOnHeadAndBody.view.xml");
        assertTrue(xml.matches("(?s).*<html>.*<head.*xmlns=\"http://www.w3.org/1999/xhtml\">.*<body.*xmlns=\"http://www.w3.org/1999/xhtml\">.*"));
    }

    public void testProcessAsXmlWithDoctype() throws Exception {

        String xml = getRawMarkup("/faces/xmlviewWithDoctype.view.xml");
        assertTrue(xml.matches("(?s).*<!DOCTYPE.*html.*PUBLIC.*\"-//W3C//DTD.*XHTML.*1.0.*Transitional//EN\".*\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html.*xmlns=\"http://www.w3.org/1999/xhtml\".*"));
    }

    public void testProcessAsXmlMathML() throws Exception {

        String xml = getRawMarkup("/faces/mathmlview.view.xml");
        assertTrue(xml.matches("(?s).*<html.*xmlns=\"http://www.w3.org/1999/xhtml\">.*<head>.*<title>.*Raw.*XML.*View.*with.*MathML</title>.*</head>.*<body>.*<p>.*<math.*xmlns=\"http://www.w3.org/1998/Math/MathML\">.*<msup>.*<msqrt>.*<mrow>.*<mi>.*a</mi>.*<mo>.*\\+</mo>.*<mi>.*b</mi>.*</mrow>.*</msqrt>.*<mn>.*27</mn>.*</msup>.*</math>.*</p>.*</body>.*</html>.*"));

        Page page = webClient.getPage(webUrl + "faces/mathmlview.view.xml");
        WebResponse response = page.getWebResponse();
        assertEquals("Content-type should be text/xml", "text/xml", response.getContentType());

    }

    public void testProcessAsJspx() throws Exception {

        String xml = getRawMarkup("/faces/jspxview.jspx");
        assertFalse(XmlDoctype.matcher(xml).matches());
        assertFalse(XmlDeclaration.matcher(xml).matches());
        assertFalse(XmlPI.matcher(xml).matches());
        assertFalse(CDATASection.matcher(xml).matches());
        assertTrue(xml.matches("(?s).*<p>This\\s+is\\s+CDATA\\s+content</p>.*"));
        assertTrue(NotEscapedText.matcher(xml).matches());
        assertFalse(Comment.matcher(xml).matches());
    }
}
