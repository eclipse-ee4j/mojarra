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

import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Unit tests for Composite Component Attributes
 */
public class CompositeAttributeITCase extends HtmlUnitFacesITCase {


    @SuppressWarnings({"UnusedDeclaration"})
    public CompositeAttributeITCase() {
        this("CompositeAttributeTestCase");
    }

    public CompositeAttributeITCase(String name) {
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
        return (new TestSuite(CompositeAttributeITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }
    

    // -------------------------------------------------------------- Test Cases

    // Tests cc:attribute default=....

    public void testValueExpressionDefaults() throws Exception {

        HtmlPage page = getPage("/faces/composite/defaultAttributeValueExpression.xhtml");
        assertTrue(page.asText().contains("DEFAULT VALUE"));

    }

    /**
     * Test for Issue #1966
     * @throws Exception
     */
    public void testCompositeAttributeDefaults() throws Exception {
        HtmlPage page = getPage("/faces/composite/defaultAttributeValueExpression_1966.xhtml");

        // Test with empty list as items
        HtmlElement dataTable = (HtmlElement) page.getElementById("WithValueEmptyList:DataTable");
        List<DomText> content = dataTable.getByXPath("./tbody/tr/td/text()");
        assertTrue("Table should conain max. 1 empty cell.", content.size() <= 1);
        for (DomText text : content) {
            assertTrue("Cells should be empty", text.asText().length() == 0);
        }

        // Test with null value as items
        dataTable = (HtmlElement) page.getElementById("WithValueNull:DataTable");
        content = dataTable.getByXPath("./tbody/tr/td/text()");
        assertEquals("Table should contain 2 cells", 2, content.size());
        for (int i = 0; i < 2; i++) {
            assertEquals("---Item " + (i + 1) + "---", content.get(i).toString());
        }

        // @Todo change to a test without awt dependencies
        // Test Colors
//        assertElementContentEquals(page,
//                "ColorWithValueFromBean:Output",
//                "---java.awt.Color[r=255,g=175,b=175]---");
//        assertElementContentEquals(page,
//                "ColorWithValueLiteral:Output",
//                "---java.awt.Color[r=80,g=40,b=20]---");
//        assertElementContentEquals(page,
//                "ColorWithValueNone:Output",
//                "---java.awt.Color[r=200,g=100,b=50]---");
//        assertElementContentEquals(page,
//                "ColorWithValueEmpty:Output",
//                "---java.awt.Color[r=200,g=100,b=50]---");
//        assertElementContentEquals(page,
//                "ColorWithValueNull:Output",
//                "---java.awt.Color[r=200,g=100,b=50]---");

    }

    /**
     * Test for Issue #1986
     */
    public void testCompositeAttributeCanBeNull() throws Exception {
        HtmlPage page = getPage("/faces/composite/defaultAttributeValueExpression_1986.xhtml");
        assertElementAttributeEquals(page, "WithValueNull:Input", "value", "");
        assertElementAttributeEquals(page, "WithValueEmpty:Input", "value", "");
    }

    /**
     * Helper to test for the Content of an HTML-Element.
     * This method will assert, that the Element with the provided id exists
     * and that its content is equal to the provided expected content.
     * @param page the page to test
     * @param elementId the id of the element that contains the content to be
     *  tested.
     * @param expected the expected content
     */
    private void assertElementContentEquals(final HtmlPage page, final String elementId, final String expected) {
        HtmlElement element = (HtmlElement) page.getElementById(elementId);
        assertNotNull(element);
        assertEquals("Testing element content of #" + elementId, expected, element.getTextContent());
    }


    /**
     * Helper to test for the value of an attribute of a HTML-Element.
     * This method will assert, that the Element with the provided id exists
     * and that the value of the attribute with the provided attributeName is
     * equal to the provided expected content.
     * @param page the page to test
     * @param elementId the id of the element with the attribute to be tested
     * @param attributeName the name of the attribute to be tested
     * @param expected the expected content
     */
    private void assertElementAttributeEquals(final HtmlPage page, final String elementId, final String attributeName, final String expected) {
        HtmlElement element = (HtmlElement) page.getElementById(elementId);
        assertNotNull(element);
        assertEquals("Testing attribute '" + attributeName + "' of #" + elementId, expected, element.getAttribute(attributeName));
    }
}
