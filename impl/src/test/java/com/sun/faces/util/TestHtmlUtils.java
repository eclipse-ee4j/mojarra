/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

// TestHtmlUtils.java
package com.sun.faces.util;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;

/**
 * <B>TestHtmlUtils</B> is a class ...
 */
public class TestHtmlUtils {

    @Test
    public void testWriteURL() throws IOException {
        //Test url with no params
        testURLEncoding("http://www.google.com",
                "http://www.google.com",
                "http://www.google.com");
        //Test URL with one param
        testURLEncoding("http://www.google.com?joe=10",
                "http://www.google.com?joe=10",
                "http://www.google.com?joe=10");
        //Test URL with two params
        testURLEncoding("http://www.google.com?joe=10&fred=20",
                "http://www.google.com?joe=10&amp;fred=20",
                "http://www.google.com?joe=10&amp;fred=20");
        //Test URL with & entity encoded
        testURLEncoding("/index.jsf?joe=10&amp;fred=20",
                "/index.jsf?joe=10&amp;fred=20",
                "/index.jsf?joe=10&amp;fred=20");
        //Test URL with two params and second & close to end of string
        testURLEncoding("/index.jsf?joe=10&f=20",
                "/index.jsf?joe=10&amp;f=20",
                "/index.jsf?joe=10&amp;f=20");
        //Test URL with misplaced & expected behavior but not necissarily right.
        testURLEncoding("/index.jsf?joe=10&f=20&",
                "/index.jsf?joe=10&amp;f=20&amp;",
                "/index.jsf?joe=10&amp;f=20&amp;");
        //Test URL with encoded entity at end of URL expected behavior but not necissarily right.
        testURLEncoding("/index.jsf?joe=10&f=20&amp;",
                "/index.jsf?joe=10&amp;f=20&amp;",
                "/index.jsf?joe=10&amp;f=20&amp;");
        //Test URL with non-ASCII characters in URI.
        testURLEncoding("/\u03b5\u03bb/\u05d9\u05ea/\u043a\u0438/\u064a\u0629\u064f\u200e\u200e/\ud55c\uae00/index.jsf",
        		"/%CE%B5%CE%BB/%D7%99%D7%AA/%D0%BA%D0%B8/%D9%8A%D8%A9%D9%8F%E2%80%8E%E2%80%8E/%ED%95%9C%EA%B8%80/index.jsf",
        		"/%CE%B5%CE%BB/%D7%99%D7%AA/%D0%BA%D0%B8/%D9%8A%D8%A9%D9%8F%E2%80%8E%E2%80%8E/%ED%95%9C%EA%B8%80/index.jsf");
        //Test URL with non-ASCII characters in query string.
        testURLEncoding("/index.jsf?greek=\u03b5\u03bb&cyrillic=\u043a\u0438&hebrew=\u05d9\u05ea&arabic=\u064a\u0629\u064f\u200e\u200e&korean=\ud55c\uae00",
        		"/index.jsf?greek=%CE%B5%CE%BB&amp;cyrillic=%D0%BA%D0%B8&amp;hebrew=%D7%99%D7%AA&amp;arabic=%D9%8A%D8%A9%D9%8F%E2%80%8E%E2%80%8E&amp;korean=%ED%95%9C%EA%B8%80",
        		"/index.jsf?greek=%CE%B5%CE%BB&amp;cyrillic=%D0%BA%D0%B8&amp;hebrew=%D7%99%D7%AA&amp;arabic=%D9%8A%D8%A9%D9%8F%E2%80%8E%E2%80%8E&amp;korean=%ED%95%9C%EA%B8%80");
    }

    @Test
    public void testControlCharacters() throws IOException {

        final char[] controlCharacters = new char[32];
        for (int i = 0; i < 32; i++) {
            controlCharacters[i] = (char) i;
        }

        String[] stringValues = new String[32];
        for (int i = 0; i < 32; i++) {
            stringValues[i] = "b" + controlCharacters[i] + "b";
        }

        final String[] largeStringValues = new String[32];
        for (int i = 0; i < 32; i++) {
            largeStringValues[i] = (stringValues[i] + "bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb");
        }

        for (int i = 0; i < 32; i++) {
            char[] textBuffer = new char[1024];
            char[] buffer = new char[1024];
            StringWriter writer = new StringWriter();
            HtmlUtils.writeAttribute(writer, false, false, buffer, stringValues[i], textBuffer, false, false);
            if (i == 9 || i == 10 || i == 12 || i == 13) {
                assertTrue(writer.toString().length() == 3);
            } else {
                assertTrue(writer.toString().length() == 2);
            }
        }

        for (int i = 0; i < 32; i++) {
            char[] textBuffer = new char[1024];
            char[] buffer = new char[1024];
            StringWriter writer = new StringWriter();
            HtmlUtils.writeAttribute(writer, false, false, buffer, largeStringValues[i], textBuffer, false, false);
            if (i == 9 || i == 10 || i == 12 || i == 13) {
                assertTrue(writer.toString().length() == 34);
            } else {
                assertTrue(writer.toString().length() == 33);
            }
        }

        for (int i = 0; i < 32; i++) {
            char[] textBuffer = new char[1024];
            char[] buffer = new char[1024];
            StringWriter writer = new StringWriter();
            HtmlUtils.writeText(writer, false, false, buffer, stringValues[i], textBuffer, false);
            if (i == 9 || i == 10 || i == 12 || i == 13) {
                assertTrue(writer.toString().length() == 3);
            } else {
                assertTrue(writer.toString().length() == 2);
            }
        }

        for (int i = 0; i < 32; i++) {
            char[] textBuffer = new char[1024];
            char[] buffer = new char[1024];
            StringWriter writer = new StringWriter();
            HtmlUtils.writeText(writer, false, false, buffer, largeStringValues[i], textBuffer, false);
            if (i == 9 || i == 10 || i == 12 || i == 13) {
                assertTrue(writer.toString().length() == 34);
            } else {
                assertTrue(writer.toString().length() == 33);
            }
        }
    }

    private void testURLEncoding(String urlToEncode, String expectedHTML, String expectedXML)
            throws IOException {
        char[] textBuffer = new char[1024];
        StringWriter xmlWriter = new StringWriter();
        HtmlUtils.writeURL(xmlWriter, urlToEncode, textBuffer, "UTF-8");
        System.out.println("XML: " + xmlWriter.toString());
        assertEquals(xmlWriter.toString(), expectedXML);
        StringWriter htmlWriter = new StringWriter();
        HtmlUtils.writeURL(htmlWriter, urlToEncode, textBuffer, "UTF-8");
        System.out.println("HTML: " + htmlWriter.toString());
        assertEquals(htmlWriter.toString(), expectedHTML);
    }
}
