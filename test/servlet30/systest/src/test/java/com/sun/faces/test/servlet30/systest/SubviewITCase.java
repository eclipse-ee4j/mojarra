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

public class SubviewITCase extends HtmlUnitFacesITCase {

    public SubviewITCase(String name) {
        super(name);
    }

    public static Test suite() {
        return (new TestSuite(SubviewITCase.class));
    }

    public void test01() throws Exception {

        HtmlPage page = getPage("/faces/subview01.jsp");
        assertTrue(page.asXml().matches(
                "(?s).*<body>\\s*Begin\\s*test\\s*&lt;c:import&gt;\\s*with\\s*subview\\s*tag\\s*in\\s*imported\\s*page\\s*<p>\\s*foo01\\s*</p>\\s*<p>\\s*subview01\\s*</p>\\s*<p>\\s*bar01\\s*</p>\\s*<p>\\s*End\\s*test\\s*&lt;c:import&gt;\\s*with\\s*subview\\s*tag\\s*in\\s*imported\\s*page\\s*</p>\\s*</body>.*"));
    }

    public void test02() throws Exception {

        HtmlPage page = getPage("/faces/subview02.jsp");
        assertTrue(page.asXml().matches(
                "(?s).*<body>\\s*<p>\\s*Begin\\s*test\\s*&lt;c:import&gt;\\s*with\\s*subview\\s*tag\\s*in\\s*importing\\s*page\\s*</p>\\s*<p>\\s*foo02\\s*</p>\\s*<p>\\s*subview02\\s*</p>\\s*<p>\\s*bar02\\s*</p>\\s*<p>\\s*End\\s*test\\s*&lt;c:import&gt;\\s*with\\s*subview\\s*tag\\s*in\\s*importing\\s*page\\s*</p>\\s*</body>.*"));
    }

    public void test03() throws Exception {

        HtmlPage page = getPage("/faces/subview03.jsp");
        assertTrue(page.asXml().matches(
                "(?s).*<body>\\s*<p>\\s*Begin\\s*test\\s*&lt;c:include&gt;\\s*with\\s*subview\\s*tag\\s*in\\s*included\\s*page\\s*</p>\\s*<p>\\s*foo01\\s*</p>\\s*<p>\\s*subview03\\s*</p>\\s*<p>\\s*bar01\\s*</p>\\s*<p>\\s*End\\s*test\\s*&lt;c:include&gt;\\s*with\\s*subview\\s*tag\\s*in\\s*included\\s*page\\s*</p>\\s*</body>.*"));
    }

    public void test04() throws Exception {

        HtmlPage page = getPage("/faces/subview04.jsp");
        assertTrue(page.asXml().matches(
                "(?s).*\\s*<body>\\s*<p>\\s*\\[A\\]\\s*</p>\\s*<p>\\s*Begin\\s*test\\s*&lt;c:include&gt;\\s*with\\s*subview\\s*tag\\s*in\\s*including\\s*page\\s*</p>\\s*<p>\\s*bar01\\s*</p>\\s*<p>\\s*subview04\\s*</p>\\s*<p>\\s*bar02\\s*</p>\\s*<p>\\s*End\\s*test\\s*&lt;c:include&gt;\\s*with\\s*subview\\s*tag\\s*in\\s*including\\s*page\\s*</p>\\s*</body>.*"));
    }

    public void test05() throws Exception {
//
//        HtmlPage page = getPage("/faces/subview05.jsp");
//        assertTrue(page.asXml().matches("(?s).*<body>\\s*<p>\\s*Begin\\s*test\\s*jsp:include\\s*with\\s*subview\\s*and\\s*iterator\\s*tag\\s*in\\s*included\\s*page\\s*</p>\\s*<br/>\\s*<p>\\s*<br/>\\s*Array\\[0\\]:\\s*This\\s*component\\s*has\\s*no\\s*ID\\s*<br/>\\s*<input\\s*type=\"text\"\\s*name=\"subviewInner:.*\"\\s*value=\"This\\s*component\\s*has\\s*no\\s*ID\\s*\"/>\\s*<br/>\\s*Array\\[1\\]:\\s*This\\s*component\\s*has\\s*no\\s*ID\\s*<br/>\\s*<input\\s*type=\"text\"\\s*name=\"subviewInner:.*\"\\s*value=\"This\\s*component\\s*has\\s*no\\s*ID\\s*\"/>\\s*<br/>\\s*Array\\[2\\]:\\s*This\\s*component\\s*has\\s*no\\s*ID\\s*<br/>\\s*<input\\s*type=\"text\"\\s*name=\"subviewInner:.*\"\\s*value=\"This\\s*component\\s*has\\s*no\\s*ID\\s*\"/>\\s*<br/>\\s*Array\\[3\\]:\\s*This\\s*component\\s*has\\s*no\\s*ID\\s*<br/>\\s*<input\\s*type=\"text\"\\s*name=\"subviewInner:.*\"\\s*value=\"This\\s*component\\s*has\\s*no\\s*ID\\s*\"/>\\s*<br/>\\s*</p>\\s*<p>\\s*Text\\s*from\\s*subview05.jsp\\s*</p>\\s*<p/>\\s*End\\s*test\\s*jsp:include\\s*with\\s*subview\\s*and\\s*iterator\\s*tag\\s*in\\s*included\\s*page\\s*<p/>\\s*</body>.*"));
    }

    public void test06() throws Exception {

        HtmlPage page = getPage("/faces/subview06.jsp");
        assertTrue(page.asXml().matches(
                "(?s).*<body>\\s*<p>\\s*Begin\\s*test\\s*&lt;c:import&gt;\\s*with\\s*iterator\\s*tag\\s*in\\s*imported\\s*page\\s*</p>\\s*<br/>\\s*<p>\\s*<br/>\\s*Array\\[0\\]:\\s*This\\s*component\\s*has\\s*no\\s*ID\\s*<br/>\\s*<input\\s*type=\"text\"\\s*name=\"subviewOuter:subviewInner:.*\"\\s*value=\"This\\s*component\\s*has\\s*no\\s*ID\\s*\"/>\\s*<br/>\\s*Array\\[1\\]:\\s*This\\s*component\\s*has\\s*no\\s*ID\\s*<br/>\\s*<input\\s*type=\"text\"\\s*name=\"subviewOuter:subviewInner:.*\"\\s*value=\"This\\s*component\\s*has\\s*no\\s*ID\\s*\"/>\\s*<br/>\\s*Array\\[2\\]:\\s*This\\s*component\\s*has\\s*no\\s*ID\\s*<br/>\\s*<input\\s*type=\"text\"\\s*name=\"subviewOuter:subviewInner:.*\"\\s*value=\"This\\s*component\\s*has\\s*no\\s*ID\\s*\"/>\\s*<br/>\\s*Array\\[3\\]:\\s*This\\s*component\\s*has\\s*no\\s*ID\\s*<br/>\\s*<input\\s*type=\"text\"\\s*name=\"subviewOuter:subviewInner:.*\"\\s*value=\"This\\s*component\\s*has\\s*no\\s*ID\\s*\"/>\\s*<br/>\\s*</p>\\s*<p>\\s*Text\\s*from\\s*subview06.jsp\\s*</p>\\s*<p>\\s*End\\s*test\\s*&lt;c:import&gt;\\s*with\\s*iterator\\s*tag\\s*in\\s*imported\\s*page\\s*</p>.*"));
    }
}
