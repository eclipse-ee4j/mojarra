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


import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import junit.framework.Test;
import static junit.framework.TestCase.assertTrue;
import junit.framework.TestSuite;

/**
 * Test cases for Facelets functionality
 */
public class ImplicitFacetITCase extends HtmlUnitFacesITCase {


    // --------------------------------------------------------------- Test Init


    public ImplicitFacetITCase() {
        this("FaceletsTestCase");
    }


    public ImplicitFacetITCase(String name) {
        super(name);
    }


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        super.setUp();
    }


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(ImplicitFacetITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }


    // ------------------------------------------------------------ Test Methods


    /*
     * Added for issue 917.
     */
    public void testUIRepeat() throws Exception {

        HtmlPage page = getPage("/faces/facelets/implicitFacet01.xhtml") ;
        
        String text = page.asText();
        
        assertTrue(text.matches("(?s).*Implicit\\s*facet\\s*01\\s*id:.*Child\\s*01\\s*of\\s*facet\\s*01\\s*id:\\s*output01.\\s*Child\\s*02\\s*of\\s*facet\\s*01\\s*id:\\s*output02.\\s*Child\\s*03\\s*of\\s*facet\\s*01\\s*id:\\s*output03.*"));
        assertTrue(-1 != text.indexOf("Implicit facet 01 id: panelGroup01. Child 01 of facet 01 id: output07. Child 02 of facet 01 id: output08. Child 03 of facet 01 id: output09."));

        HtmlSubmitInput input = (HtmlSubmitInput) getInputContainingGivenId(page, "command");
        page = input.click();

        text = page.asText();

        assertTrue(text.matches("(?s).*Implicit\\s*facet\\s*01\\s*id:.*Child\\s*01\\s*of\\s*facet\\s*01\\s*id:\\s*output01.\\s*Child\\s*02\\s*of\\s*facet\\s*01\\s*id:\\s*output02.\\s*Child\\s*03\\s*of\\s*facet\\s*01\\s*id:\\s*output03.*"));
        assertTrue(-1 != text.indexOf("Implicit facet 01 id: panelGroup01. Child 01 of facet 01 id: output07. Child 02 of facet 01 id: output08. Child 03 of facet 01 id: output09."));


    }

    /*
     * Added for issue 1726.
     */
    public void testPostBack() throws Exception {
        HtmlPage page = getPage("/faces/facelets/issue1726.xhtml");
        if (!page.asXml().toUpperCase().contains("TOMCAT")) {
            HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");
            page = button.click();
            String text = page.asText();
            assert(!text.contains("jakarta.faces.component.UIPanel"));
        }
    }

    public void testConditionalImplicitFacetChild1727() throws Exception {
        HtmlPage page = getPage("/faces/facelets/issue1727-facet-conditional.xhtml");
        HtmlCheckBoxInput checkbox = (HtmlCheckBoxInput) page.getElementById("checkbox");
        checkbox.setChecked(false);
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();
        String text = page.asText();
        assertTrue(text.matches("(?s).*|Not in if 01|\\s|Not in if 02|.*"));

        checkbox = (HtmlCheckBoxInput) page.getElementById("checkbox");
        checkbox.setChecked(true);
        button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();
        text = page.asText();
        assertTrue(text.matches("(?s).*|Not in if 01|\\s|Not in if 02|\\s|In if 01|\\s|In if 02|.*"));

        checkbox = (HtmlCheckBoxInput) page.getElementById("checkbox");
        checkbox.setChecked(false);
        button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();
        text = page.asText();
        assertTrue(text.matches("(?s).*|Not in if 01|\\s|Not in if 02|.*"));

    }

    /*
     * Added for Issue 2066
     * Tests h:column "rowHeader" tag attribute.
     */
    public void testColumnRowHeader() throws Exception {
         HtmlPage page = getPage("/faces/facelets/issue1726.xhtml");
         if (!page.asXml().toUpperCase().contains("TOMCAT")) {
            String xml = page.asXml();
            assertTrue(xml.contains("<th scope=\"row\">"));
         }
    }
}
