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

import junit.framework.Test;
import junit.framework.TestSuite;
import com.gargoylesoftware.htmlunit.html.*;

public class OutputScriptStyleITCase extends HtmlUnitFacesITCase {

    public OutputScriptStyleITCase(String name) {
        super(name);
    }

    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(OutputScriptStyleITCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    public void testOutputScriptStyle() throws Exception {
        HtmlPage page = getPage("/faces/render/outputScriptStyleNested.xhtml");

        String text = page.asXml();

        // case 1
        assertTrue(text.matches(
                "(?s).*<head>.*" + "<script type=\"text/javascript\".*src=\"/jsf-systest/faces/javax.faces.resource/case1.js.*\">.*"
                        + "</script>.*" + "</head>.*"));

        assertTrue(!text.matches("(?s).*alert\\(\"case1\"\\);.*"));

        // case 2
        assertTrue(text.matches(
                "(?s).*<body>.*" + "<script type=\"text/javascript\">.*" + "alert\\(\"case2\"\\);.*" + "</script>.*" + "</body>.*"));

        // case 3
        assertTrue(text.matches(
                "(?s).*<body>.*" + "<script type=\"text/javascript\".*src=\"/jsf-systest/faces/javax.faces.resource/case3.js.*\">.*"
                        + "</script>.*" + "</body>.*"));

        assertTrue(!text.matches("(?s).*alert\\(\"case3\"\\);.*"));

        // case 4
        assertTrue(text.matches(
                "(?s).*<head>.*" + "<script type=\"text/javascript\".*src=\"/jsf-systest/faces/javax.faces.resource/case4.js.*\">.*"
                        + "</script>.*" + "</head>.*"));

        // case 5, if not satisfied, would cause the page to fail.

        // case 6
        assertTrue(text.matches(
                "(?s).*<body>.*" + "<script type=\"text/javascript\".*src=\"/jsf-systest/faces/javax.faces.resource/case6.js.*\">.*"
                        + "</script>.*" + "</body>.*"));

        // case 7, if not satisfied, would cause the page to fail

        // case 8
        assertTrue(text.matches(
                "(?s).*<body>.*" + "<script type=\"text/javascript\">.*" + "alert\\(\"case8\"\\);.*" + "</script>.*" + "</body>.*"));

        // case 9
        assertTrue(text.matches("(?s).*<head>.*"
                + "<link.* type=\"text/css\".*rel=\"stylesheet\".* href=\"/jsf-systest/faces/javax.faces.resource/case9.css.*\"\\s*/>.*"
                + "</head>.*"));

        assertTrue(!text.matches("(?s).*\\.case9.*"));

        // case 10
        assertTrue(text.matches("(?s).*<head>.*" + "<style\\s*type=\"text/css\">.*" + "\\.case10\\s*\\{.*" + "color: blue;.*" + "\\}.*"
                + "</style>.*" + "</head>.*"));

        // case 11
        assertTrue(text.matches("(?s).*<head>.*"
                + "<link.* type=\"text/css\".*rel=\"stylesheet\".* href=\"/jsf-systest/faces/javax.faces.resource/case11.css.*\"\\s*/>.*"
                + "</head>.*"));

        assertTrue(!text.matches("(?s).*\\.case11.*"));

        // case 12
        assertTrue(text.matches("(?s).*<head>.*"
                + "<link.* type=\"text/css\".*rel=\"stylesheet\".* href=\"/jsf-systest/faces/javax.faces.resource/case12.css.*\"\\s*/>.*"
                + "</head>.*"));

        // case 13, if not satisfied, would cause the page to fail.

        // case 14
        assertTrue(text.matches("(?s).*<head>.*"
                + "<link.* type=\"text/css\".*rel=\"stylesheet\".* href=\"/jsf-systest/faces/javax.faces.resource/case14.css.*\"\\s*/>.*"
                + "</head>.*"));

        // case 15, if not satisfied, would cause the page to fail.

        // case 16
        assertTrue(text.matches("(?s).*<head>.*" + "<style\\s*type=\"text/css\">.*" + "\\.case16\\s*\\{.*" + "color: orange;.*" + "\\}.*"
                + "</style>.*" + "</head>.*"));

    }

    public void testScriptQuery() throws Exception {
        lastpage = getPage("/faces/render/outputScriptQuery.xhtml");
        String text = lastpage.asXml();
        assertTrue(text.contains("/jsf-systest/faces/javax.faces.resource/simple.js"));
        assertTrue(text.contains("/jsf-systest/faces/javax.faces.resource/simple2.js"));
        assertTrue(text.contains("/jsf-systest/faces/javax.faces.resource/jsf.js"));
    }

    public void testSheetMedia() throws Exception {
        lastpage = getPage("/faces/render/outputSheetMedia.xhtml");
        String text = lastpage.asXml();
        assertTrue(text.matches("(?s).*<head>.*"
                + "<link.* type=\"text/css\".* rel=\"stylesheet\".* href=\"/jsf-systest/faces/javax.faces.resource/case9.css.*\".* media=\"print\"\\s*/>.*"
                + "</head>.*"));
    }
}
