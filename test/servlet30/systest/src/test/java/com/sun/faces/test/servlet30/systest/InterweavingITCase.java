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

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import junit.framework.Test;
import junit.framework.TestSuite;

public class InterweavingITCase extends HtmlUnitFacesITCase {

    public InterweavingITCase(String name) {
        super(name);
        addExclusion(Container.WLS_10_3_4_NO_CLUSTER, "test07");
        addExclusion(Container.WLS_10_3_4_NO_CLUSTER, "test13");
        addExclusion(Container.WLS_12_1_1_NO_CLUSTER, "test07");
        addExclusion(Container.WLS_12_1_1_NO_CLUSTER, "test13");
    }

    public static Test suite() {
        return (new TestSuite(InterweavingITCase.class));
    }

    public void test01() throws Exception {

        HtmlPage page = getPage("/faces/interweaving01.jsp");
        assertTrue(page.asText().matches(
                "(?s).*Begin\\s*test\\s*jsp include without verbatim\\s*interweaving\\s*works\\s*well!!\\s*End\\s*test\\s*jsp include without verbatim.*"));
    }

    public void test02() throws Exception {

        HtmlPage page = getPage("/faces/interweaving02.jsp");
        assertTrue(page.asText().matches(
                "(?s).*Begin\\s*test\\s*jstl import without verbatim\\s*interweaving\\s*works\\s*well!!\\s*End\\s*test\\s*jstl import without verbatim.*"));
    }

    public void test03() throws Exception {

        HtmlPage page = getPage("/faces/interweaving03.jsp");
        assertTrue(page.asXml().matches(
                "(?s).*<table>\\s*<tbody>\\s*<tr>\\s*<td>\\s*Row 1\\s*</td>\\s*</tr>\\s*<tr>\\s*<td>\\s*Row 2\\s*</td>\\s*</tr>\\s*<tr>\\s*<td>\\s*Row 3\\s*</td>\\s*</tr>\\s*</tbody>\\s*</table>\\s*.*"));
    }

    public void test04() throws Exception {

        HtmlPage page = getPage("/faces/interweaving04.jsp");
        assertTrue(page.asXml().matches("(?s).*\\[First\\]\\[Second\\]\\[Third\\].*"));
    }

    public void test05() throws Exception {

        HtmlPage page = getPage("/faces/interweaving05.jsp");
        assertTrue(
                page.asText().matches("(?s).*Begin jstl-choose test without id\\[FIRST\\]\\[SECOND\\]End jstl-choose test without id.*"));
    }

    public void test06() throws Exception {
//
//        HtmlPage page = getPage("/faces/interweaving06.jsp");
//        assertTrue(page.asXml().matches("(?s).*<body>\\s*<p>\\s*Begin\\s*test\\s*jsp:include\\s*without\\s*subview\\s*and\\s*iterator\\s*tag\\s*in\\s*included\\s*page\\s*</p>\\s*<br/>\\s*<p>\\s*<br/>\\s*Array\\[0\\]:\\s*This\\s*component\\s*has\\s*no\\s*ID\\s*<br/>\\s*<input\\s*type=\"text\"\\s*name=\".*\"\\s*value=\"This\\s*component\\s*has\\s*no\\s*ID\\s*\"/>\\s*<br/>\\s*Array\\[1\\]:\\s*This\\s*component\\s*has\\s*no\\s*ID\\s*<br/>\\s*<input\\s*type=\"text\"\\s*name=\".*\"\\s*value=\"This\\s*component\\s*has\\s*no\\s*ID\\s*\"/>\\s*<br/>\\s*Array\\[2\\]:\\s*This\\s*component\\s*has\\s*no\\s*ID\\s*<br/>\\s*<input\\s*type=\"text\"\\s*name=\".*\"\\s*value=\"This\\s*component\\s*has\\s*no\\s*ID\\s*\"/>\\s*<br/>\\s*Array\\[3\\]:\\s*This\\s*component\\s*has\\s*no\\s*ID\\s*<br/>\\s*<input\\s*type=\"text\"\\s*name=\".*\"\\s*value=\"This\\s*component\\s*has\\s*no\\s*ID\\s*\"/>\\s*<br/>\\s*</p>\\s*<p>\\s*Text\\s*from\\s*interweaving06.jsp\\s*</p>\\s*<p/>\\s*End\\s*test\\s*jsp:include\\s*without\\s*subview\\s*and\\s*iterator\\s*tag\\s*in\\s*included\\s*page\\s*<p/>\\s*</body>.*"));
    }

    public void test07() throws Exception {

//        HtmlPage page = getPage("/faces/interweaving07.jsp");
//        assertTrue(page.asXml().matches("(?s).*\\s*<body>\\s*<p>\\s*Begin\\s*test\\s*&lt;c:import&gt;\\s*with\\s*iterator\\s*tag\\s*in\\s*imported\\s*page\\s*</p>\\s*<br/>\\s*<p>\\s*<br/>\\s*Array\\[0\\]:\\s*This\\s*component\\s*has\\s*no\\s*ID\\s*<br/>\\s*<input\\s*type=\"text\"\\s*name=\".*\"\\s*value=\"This\\s*component\\s*has\\s*no\\s*ID\\s*\"/>\\s*<br/>\\s*Array\\[1\\]:\\s*This\\s*component\\s*has\\s*no\\s*ID\\s*<br/>\\s*<input\\s*type=\"text\"\\s*name=\".*\"\\s*value=\"This\\s*component\\s*has\\s*no\\s*ID\\s*\"/>\\s*<br/>\\s*Array\\[2\\]:\\s*This\\s*component\\s*has\\s*no\\s*ID\\s*<br/>\\s*<input\\s*type=\"text\"\\s*name=\".*\"\\s*value=\"This\\s*component\\s*has\\s*no\\s*ID\\s*\"/>\\s*<br/>\\s*Array\\[3\\]:\\s*This\\s*component\\s*has\\s*no\\s*ID\\s*<br/>\\s*<input\\s*type=\"text\"\\s*name=\".*\"\\s*value=\"This\\s*component\\s*has\\s*no\\s*ID\\s*\"/>\\s*<br/>\\s*</p>\\s*<p>\\s*Text\\s*from\\s*interweaving07.jsp\\s*</p>\\s*<p>\\s*End\\s*test\\s*&lt;c:import&gt;\\s*with\\s*iterator\\s*tag\\s*in\\s*imported\\s*page\\s*</p>\\s*</body>.*"));
    }

    public void test08() throws Exception {

        // Make multiple requests to the same page and ensure the response is 200

        HtmlPage page1 = getPage("/faces/interweaving08.jsp");
        assertTrue(page1.asXml().matches(
                "(?s).*\\s*<body>\\s*<form\\s*id=\"form\".*<input\\s*type=\"hidden\"\\s*name=\"form\".*<input\\s*type=\"hidden\"\\s*name=\"javax.faces.ViewState\".*<table>\\s*<tbody>\\s*<tr>\\s*<td>\\s*ciao\\s*Value\\s*</td>\\s*</tr>\\s*</tbody>\\s*</table>\\s*</form>\\s*</body>.*"));

        HtmlPage page2 = getPage("/faces/interweaving08.jsp");
        assertTrue(page2.asXml().matches(
                "(?s).*\\s*<body>\\s*<form\\s*id=\"form\".*<input\\s*type=\"hidden\"\\s*name=\"form\".*<input\\s*type=\"hidden\"\\s*name=\"javax.faces.ViewState\".*<table>\\s*<tbody>\\s*<tr>\\s*<td>\\s*ciao\\s*Value\\s*</td>\\s*</tr>\\s*</tbody>\\s*</table>\\s*</form>\\s*</body>.*"));

        HtmlPage page3 = getPage("/faces/interweaving08.jsp");
        assertTrue(page3.asXml().matches(
                "(?s).*\\s*<body>\\s*<form\\s*id=\"form\".*<input\\s*type=\"hidden\"\\s*name=\"form\".*<input\\s*type=\"hidden\"\\s*name=\"javax.faces.ViewState\".*<table>\\s*<tbody>\\s*<tr>\\s*<td>\\s*ciao\\s*Value\\s*</td>\\s*</tr>\\s*</tbody>\\s*</table>\\s*</form>\\s*</body>.*"));
    }

    public void test09() throws Exception {

        // Make multiple requests to the same page and ensure the response is 200

        HtmlPage page1 = getPage("/faces/interweaving09.jsp");
        assertTrue(page1.asXml().matches(
                "(?s).*\\s*<body>\\s*<form\\s*id=\"form\".*<input\\s*type=\"hidden\"\\s*name=\"form\".*<input\\s*type=\"hidden\"\\s*name=\"javax.faces.ViewState\".*<table>\\s*<tbody>\\s*<tr>\\s*<td>\\s*Value\\s*ciao\\s*Value\\s*</td>\\s*</tr>\\s*</tbody>\\s*</table>\\s*</form>\\s*</body>.*"));

        HtmlPage page2 = getPage("/faces/interweaving09.jsp");
        assertTrue(page2.asXml().matches(
                "(?s).*\\s*<body>\\s*<form\\s*id=\"form\".*<input\\s*type=\"hidden\"\\s*name=\"form\".*<input\\s*type=\"hidden\"\\s*name=\"javax.faces.ViewState\".*<table>\\s*<tbody>\\s*<tr>\\s*<td>\\s*Value\\s*ciao\\s*Value\\s*</td>\\s*</tr>\\s*</tbody>\\s*</table>\\s*</form>\\s*</body>.*"));

        HtmlPage page3 = getPage("/faces/interweaving09.jsp");
        assertTrue(page3.asXml().matches(
                "(?s).*\\s*<body>\\s*<form\\s*id=\"form\".*<input\\s*type=\"hidden\"\\s*name=\"form\".*<input\\s*type=\"hidden\"\\s*name=\"javax.faces.ViewState\".*<table>\\s*<tbody>\\s*<tr>\\s*<td>\\s*Value\\s*ciao\\s*Value\\s*</td>\\s*</tr>\\s*</tbody>\\s*</table>\\s*</form>\\s*</body>.*"));
    }

    public void test10() throws Exception {

        // Make multiple requests to the same page and ensure the response is 200

        HtmlPage page1 = getPage("/faces/interweaving10.jsp");
        assertTrue(page1.asXml().matches(
                "(?s).*\\s*<body>\\s*<form\\s*id=\"form\".*<input\\s*type=\"hidden\"\\s*name=\"form\".*<input\\s*type=\"hidden\"\\s*name=\"javax.faces.ViewState\".*<table>\\s*<tbody>\\s*<tr>\\s*<td>\\s*Value\\s*ciao\\s*</td>\\s*</tr>\\s*</tbody>\\s*</table>\\s*</form>\\s*</body>.*"));

        HtmlPage page2 = getPage("/faces/interweaving10.jsp");
        assertTrue(page2.asXml().matches(
                "(?s).*\\s*<body>\\s*<form\\s*id=\"form\".*<input\\s*type=\"hidden\"\\s*name=\"form\".*<input\\s*type=\"hidden\"\\s*name=\"javax.faces.ViewState\".*<table>\\s*<tbody>\\s*<tr>\\s*<td>\\s*Value\\s*ciao\\s*</td>\\s*</tr>\\s*</tbody>\\s*</table>\\s*</form>\\s*</body>.*"));

        HtmlPage page3 = getPage("/faces/interweaving10.jsp");
        assertTrue(page3.asXml().matches(
                "(?s).*\\s*<body>\\s*<form\\s*id=\"form\".*<input\\s*type=\"hidden\"\\s*name=\"form\".*<input\\s*type=\"hidden\"\\s*name=\"javax.faces.ViewState\".*<table>\\s*<tbody>\\s*<tr>\\s*<td>\\s*Value\\s*ciao\\s*</td>\\s*</tr>\\s*</tbody>\\s*</table>\\s*</form>\\s*</body>.*"));
    }

    public void test11() throws Exception {
//
//        HtmlPage page = getPage("/faces/interweaving11.jsp");
//        assertTrue(page.asXml().matches("(?s).*\\s*<body>\\s*<form.*<input\\s*type=\"hidden\".*<input\\s*type=\"hidden\"\\s*name=\"javax.faces.ViewState\".*<script.*jsf.js.*<a\\s*href.*>\\s*one\\s*</a>\\s*<a\\s*href.*>\\s*two\\s*</a>\\s*<a\\s*href.*>\\s*three\\s*</a>\\s*</form>\\s*</body>.*"));
    }

    public void test12() throws Exception {

        HtmlPage page = getPage("/faces/interweaving12.jsp");
        assertTrue(page.asXml().matches(
                "(?s).*\\s*<body>\\s*<form.*<input\\s*type=\"hidden\".*<input\\s*type=\"hidden\"\\s*name=\"javax.faces.ViewState\".*this should be before the button\\s*<input.*type=\"submit\".*value=\"commandButton 1\"\\s*/>\\s*</form>\\s*</body>.*"));
    }

}
