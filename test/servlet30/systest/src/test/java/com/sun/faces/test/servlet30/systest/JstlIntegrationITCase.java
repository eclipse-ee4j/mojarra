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


import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import junit.framework.Test;
import junit.framework.TestSuite;

import javax.faces.component.NamingContainer;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;


/**
 * <p>Test Case for JSTL Interoperability.</p>
 */

public class JstlIntegrationITCase extends HtmlUnitFacesITCase {

    // ------------------------------------------------------------ Constructors


    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public JstlIntegrationITCase(String name) {
        super(name);
    }

    // ------------------------------------------------------ Instance Variables

    // ---------- jstl-foreach-01.jsp values ----------

    private String jstlForEach01_name = "jstlForeach01_form";

    private String jstlForEach01_names[] =
            {"arrayProp0", "arrayProp1", "arrayProp2", "arrayProp3", "arrayProp4"};

    private String jstlForEach01_pristine[] =
            {
                    "First String", "Second String", "Third String", "Fourth String",
                    "Fifth String"
            };

    private String jstlForEach01_updated[] =
            {
                    "New First String", "Second String", "Third String", "New Fourth String",
                    "Fifth String"
            };

    // ---------------------------------------------------- Overall Test Methods


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
        return (new TestSuite(JstlIntegrationITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }

    // ------------------------------------------------- Individual Test Methods

    public void testEmpty() {
    }


    /**
     * ******************* PENDING(): re-enable these when we figure
     * out how to do c:forEach.
     * <p/>
     * // Components Inside Choose (Explicit Identifiers)
     * public void testJstlChoose01() throws Exception {
     * <p/>
     * // Check each individual case multiple times
     * checkJstlChoose00();
     * checkJstlChoose01a();
     * checkJstlChoose01a();
     * checkJstlChoose01a();
     * checkJstlChoose00();
     * checkJstlChoose01b();
     * checkJstlChoose01b();
     * checkJstlChoose01b();
     * checkJstlChoose00();
     * checkJstlChoose01c();
     * checkJstlChoose01c();
     * checkJstlChoose01c();
     * <p/>
     * // Check cases in ascending order
     * checkJstlChoose00();
     * checkJstlChoose01a();
     * checkJstlChoose01b();
     * checkJstlChoose01c();
     * <p/>
     * // Check cases in descending order
     * checkJstlChoose00();
     * checkJstlChoose01c();
     * checkJstlChoose01b();
     * checkJstlChoose01a();
     * <p/>
     * // Check cases in random order
     * checkJstlChoose00();
     * checkJstlChoose01b();
     * checkJstlChoose01a();
     * checkJstlChoose01c();
     * <p/>
     * }
     * <p/>
     * <p/>
     * // Components Inside Choose (In Naming Container)
     * public void testJstlChoose02() throws Exception {
     * <p/>
     * // Check each individual case multiple times
     * checkJstlChoose00();
     * checkJstlChoose02a();
     * checkJstlChoose02a();
     * checkJstlChoose02a();
     * checkJstlChoose00();
     * checkJstlChoose02b();
     * checkJstlChoose02b();
     * checkJstlChoose02b();
     * checkJstlChoose00();
     * checkJstlChoose02c();
     * checkJstlChoose02c();
     * checkJstlChoose02c();
     * <p/>
     * // Check cases in ascending order
     * checkJstlChoose00();
     * checkJstlChoose02a();
     * checkJstlChoose02b();
     * checkJstlChoose02c();
     * <p/>
     * // Check cases in descending order
     * checkJstlChoose00();
     * checkJstlChoose02c();
     * checkJstlChoose02b();
     * checkJstlChoose02a();
     * <p/>
     * // Check cases in random order
     * checkJstlChoose00();
     * checkJstlChoose02b();
     * checkJstlChoose02a();
     * checkJstlChoose02c();
     * <p/>
     * }
     * <p/>
     * <p/>
     * // Form with fields rendered inside a <c:forEach> - pristine
     * public void testJstForEach01_pristine() throws Exception {
     * <p/>
     * checkJstlForEach00();
     * checkJstlForEach01(getJstlForEach01(), jstlForEach01_pristine);
     * checkJstlForEach00();
     * checkJstlForEach01(getJstlForEach01(), jstlForEach01_pristine);
     * checkJstlForEach00();
     * <p/>
     * }
     * <p/>
     * <p/>
     * // Form with fields rendered inside a <c:forEach> - submit unchanged
     * public void testJstForEach01_submit01() throws Exception {
     * <p/>
     * checkJstlForEach00();
     * HtmlPage page = getJstlForEach01();
     * checkJstlForEach01(page, jstlForEach01_pristine);
     * HtmlForm form = getFormById(page, jstlForEach01_name);
     * assertNotNull("form exists", form);
     * HtmlSubmitInput submit = (HtmlSubmitInput)
     * form.getInputByName(jstlForEach01_name +
     * NamingContainer.SEPARATOR_CHAR +
     * "submit");
     * page = (HtmlPage) submit.click();
     * checkJstlForEach01(page, jstlForEach01_pristine);
     * checkJstlForEach00();
     * <p/>
     * }
     * <p/>
     * <p/>
     * // Form with fields rendered inside a <c:forEach> - submit modified
     * public void testJstForEach01_submit02() throws Exception {
     * <p/>
     * checkJstlForEach00();
     * HtmlPage page = getJstlForEach01();
     * checkJstlForEach01(page, jstlForEach01_pristine);
     * HtmlForm form = getFormById(page, jstlForEach01_name);
     * assertNotNull("form exists", form);
     * for (int i = 0; i < jstlForEach01_names.length; i++) {
     * HtmlTextInput input = (HtmlTextInput)
     * form.getInputByName(jstlForEach01_name +
     * NamingContainer.SEPARATOR_CHAR +
     * jstlForEach01_names[i]);
     * assertNotNull("field '" + jstlForEach01_names[i] + "' exists", input);
     * input.setValueAttribute(jstlForEach01_updated[i]);
     * }
     * HtmlSubmitInput submit = (HtmlSubmitInput)
     * form.getInputByName(jstlForEach01_name + NamingContainer.SEPARATOR_CHAR +
     * "submit");
     * page = (HtmlPage) submit.click();
     * checkJstlForEach01(page, jstlForEach01_updated);
     * checkJstlForEach00();
     * <p/>
     * }
     * <p/>
     * <p/>
     * // Components Inside Conditional
     * public void testJstlIf01() throws Exception {
     * <p/>
     * // Check the "true" case multiple times in a row
     * checkJstlIf00();
     * checkJstlIf01a();
     * checkJstlIf01a();
     * checkJstlIf01a();
     * checkJstlIf01a();
     * <p/>
     * // Check the "false case multiple times in a row
     * checkJstlIf00();
     * checkJstlIf01b();
     * checkJstlIf01b();
     * checkJstlIf01b();
     * checkJstlIf01b();
     * <p/>
     * // Check alternating access to the same page (first pattern)
     * checkJstlIf00();
     * checkJstlIf01a();
     * checkJstlIf01b();
     * checkJstlIf01a();
     * checkJstlIf01b();
     * <p/>
     * // Check alternating access to the same page (second pattern)
     * checkJstlIf00();
     * checkJstlIf01b();
     * checkJstlIf01a();
     * checkJstlIf01b();
     * checkJstlIf01a();
     * <p/>
     * }
     * <p/>
     * <p/>
     * // Components and facets inside conditional
     * public void testJstlIf02() throws Exception {
     * <p/>
     * // Check each style sequentially
     * checkJstlIf00();
     * checkJstlIf02a();
     * checkJstlIf02a();
     * checkJstlIf02a();
     * checkJstlIf00();
     * checkJstlIf02b();
     * checkJstlIf02b();
     * checkJstlIf02b();
     * checkJstlIf00();
     * checkJstlIf02c();
     * checkJstlIf02c();
     * checkJstlIf02c();
     * checkJstlIf00();
     * checkJstlIf02d();
     * checkJstlIf02d();
     * checkJstlIf02d();
     * checkJstlIf00();
     * checkJstlIf02e();
     * checkJstlIf02e();
     * checkJstlIf02e();
     * <p/>
     * // Check each style in ascending order
     * checkJstlIf00();
     * checkJstlIf02a();
     * checkJstlIf02b();
     * checkJstlIf02c();
     * checkJstlIf02d();
     * checkJstlIf02e();
     * <p/>
     * // Check each style in descending order
     * checkJstlIf00();
     * checkJstlIf02e();
     * checkJstlIf02d();
     * checkJstlIf02c();
     * checkJstlIf02b();
     * checkJstlIf02a();
     * <p/>
     * // Check each style in a more random order
     * checkJstlIf00();
     * checkJstlIf02c();
     * checkJstlIf02e();
     * checkJstlIf02a();
     * checkJstlIf02d();
     * checkJstlIf02b();
     * <p/>
     * }
     * <p/>
     * <p/>
     * // Component and Template Text Inside Conditional
     * public void testJstlIf03() throws Exception {
     * <p/>
     * // Check the "true" case multiple times in a row
     * checkJstlIf00();
     * checkJstlIf03a();
     * checkJstlIf03a();
     * checkJstlIf03a();
     * checkJstlIf03a();
     * <p/>
     * // Check the "false case multiple times in a row
     * checkJstlIf00();
     * checkJstlIf03b();
     * checkJstlIf03b();
     * checkJstlIf03b();
     * checkJstlIf03b();
     * <p/>
     * // Check alternating access to the same page (first pattern)
     * checkJstlIf00();
     * checkJstlIf03a();
     * checkJstlIf03b();
     * checkJstlIf03a();
     * checkJstlIf03b();
     * <p/>
     * // Check alternating access to the same page (second pattern)
     * checkJstlIf00();
     * checkJstlIf03b();
     * checkJstlIf03a();
     * checkJstlIf03b();
     * checkJstlIf03a();
     * <p/>
     * }
     * <p/>
     * <p/>
     * // Renders-Child Component Inside Conditional (no nested template text) (
     * public void testJstlIf04() throws Exception {
     * <p/>
     * // Check the "true" case multiple times in a row
     * checkJstlIf00();
     * checkJstlIf04a();
     * checkJstlIf04a();
     * checkJstlIf04a();
     * checkJstlIf04a();
     * <p/>
     * // Check the "false case multiple times in a row
     * checkJstlIf00();
     * checkJstlIf04b();
     * checkJstlIf04b();
     * checkJstlIf04b();
     * checkJstlIf04b();
     * <p/>
     * // Check alternating access to the same page (first pattern)
     * checkJstlIf00();
     * checkJstlIf04a();
     * checkJstlIf04b();
     * checkJstlIf04a();
     * checkJstlIf04b();
     * <p/>
     * // Check alternating access to the same page (second pattern)
     * checkJstlIf00();
     * checkJstlIf04b();
     * checkJstlIf04a();
     * checkJstlIf04b();
     * checkJstlIf04a();
     * <p/>
     * }
     * <p/>
     * <p/>
     * // Test importing JSPs with literal text
     * public void testJstlImport01() throws Exception {
     * <p/>
     * checkJstlImport00();
     * checkJstlImport01();
     * checkJstlImport01();
     * <p/>
     * checkJstlImport00();
     * checkJstlImport01();
     * checkJstlImport01();
     * <p/>
     * }
     * <p/>
     * <p/>
     * // Test importing JSPs with simple components
     * public void testJstlImport02() throws Exception {
     * <p/>
     * checkJstlImport00();
     * checkJstlImport02();
     * checkJstlImport02();
     * <p/>
     * checkJstlImport00();
     * checkJstlImport02();
     * checkJstlImport02();
     * <p/>
     * }
     * <p/>
     * <p/>
     * // Test selectively importing JSPs with simple components (explicit ids)
     * public void testJstlImport03() throws Exception {
     * <p/>
     * // Check each individual case multiple times
     * checkJstlImport00();
     * checkJstlImport03a();
     * checkJstlImport03a();
     * checkJstlImport03a();
     * checkJstlImport00();
     * checkJstlImport03b();
     * checkJstlImport03b();
     * checkJstlImport03b();
     * checkJstlImport00();
     * checkJstlImport03c();
     * checkJstlImport03c();
     * checkJstlImport03c();
     * <p/>
     * // Check cases in ascending order
     * checkJstlImport00();
     * checkJstlImport03a();
     * checkJstlImport03b();
     * checkJstlImport03c();
     * <p/>
     * // Check cases in descending order
     * checkJstlImport00();
     * checkJstlImport03c();
     * checkJstlImport03b();
     * checkJstlImport03a();
     * <p/>
     * // Check cases in random order
     * checkJstlImport00();
     * checkJstlImport03b();
     * checkJstlImport03a();
     * checkJstlImport03c();
     * <p/>
     * }
     * <p/>
     * <p/>
     * // Test selectively importing JSPs with simple components (naming container)
     * public void testJstlImport04() throws Exception {
     * <p/>
     * // Check each individual case multiple times
     * checkJstlImport00();
     * checkJstlImport04a();
     * checkJstlImport04a();
     * checkJstlImport04a();
     * checkJstlImport00();
     * checkJstlImport04b();
     * checkJstlImport04b();
     * checkJstlImport04b();
     * checkJstlImport00();
     * checkJstlImport04c();
     * checkJstlImport04c();
     * checkJstlImport04c();
     * <p/>
     * // Check cases in ascending order
     * checkJstlImport00();
     * checkJstlImport04a();
     * checkJstlImport04b();
     * checkJstlImport04c();
     * <p/>
     * // Check cases in descending order
     * checkJstlImport00();
     * checkJstlImport04c();
     * checkJstlImport04b();
     * checkJstlImport04a();
     * <p/>
     * // Check cases in random order
     * checkJstlImport00();
     * checkJstlImport04b();
     * checkJstlImport04a();
     * checkJstlImport04c();
     * <p/>
     * }
     * <p/>
     * <p/>
     * **************************************
     */

    // --------------------------------------------------------- Private Methods

    // Check the reset page to force a new component tree
    private void checkJstlChoose00() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-choose-00.jsp");
        assertEquals("Correct page title",
                "jstl-choose-00", page.getTitleText());

    }


    // Check chosen components with explicit ids
    private void checkJstlChoose01a() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-choose-01.jsp?choose=a");
        assertEquals("Correct page title",
                "jstl-choose-01", page.getTitleText());
        assertEquals("Correct body element",
                "[1] [2a] [2z] [3]", getBodyText(page));

    }


    // Check chosen components with explicit ids
    private void checkJstlChoose01b() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-choose-01.jsp?choose=b");
        assertEquals("Correct page title",
                "jstl-choose-01", page.getTitleText());
        assertEquals("Correct body element",
                "[1] [2b] [2y] [3]", getBodyText(page));

    }


    // Check chosen components with explicit ids
    private void checkJstlChoose01c() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-choose-01.jsp?choose=c");
        assertEquals("Correct page title",
                "jstl-choose-01", page.getTitleText());
        assertEquals("Correct body element",
                "[1] [2c] [2x] [3]", getBodyText(page));

    }


    // Check chosen components with naming containers
    private void checkJstlChoose02a() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-choose-02.jsp?choose=a");
        assertEquals("Correct page title",
                "jstl-choose-02", page.getTitleText());
        assertEquals("Correct body element",
                "[1] [2a] [2z] [3]", getBodyText(page));

    }


    // Check chosen components with naming containers
    private void checkJstlChoose02b() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-choose-02.jsp?choose=b");
        assertEquals("Correct page title",
                "jstl-choose-02", page.getTitleText());
        assertEquals("Correct body element",
                "[1] [2b] [2y] [3]", getBodyText(page));

    }


    // Check chosen components with naming containers
    private void checkJstlChoose02c() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-choose-02.jsp?choose=c");
        assertEquals("Correct page title",
                "jstl-choose-02", page.getTitleText());
        assertEquals("Correct body element",
                "[1] [2c] [2x] [3]", getBodyText(page));

    }


    // Check the reset page to force a new component tree
    private void checkJstlForEach00() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-foreach-00.jsp");
        assertEquals("Correct page title",
                "jstl-foreach-00", page.getTitleText());

    }


    // Check the values of the input fields against the specified list
    private void checkJstlForEach01(HtmlPage page, String expected[]) {


        assertEquals("Correct page title",
                "jstl-foreach-01", page.getTitleText());
        HtmlForm form = getFormById(page, jstlForEach01_name);
        assertNotNull("form exists", form);
        for (int i = 0; i < expected.length; i++) {
            HtmlTextInput input = (HtmlTextInput)
                    form.getInputByName(jstlForEach01_name +
                            NamingContainer.SEPARATOR_CHAR +
                            jstlForEach01_names[i]);
            assertNotNull("field '" + jstlForEach01_names[i] + "' exists",
                    input);
            assertEquals("field '" + jstlForEach01_names[i] + "' value",
                    expected[i], input.getValueAttribute());
        }

    }


    // Check the reset page to force a new component tree
    private void checkJstlIf00() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-if-00.jsp");
        assertEquals("Correct page title",
                "jstl-if-00", page.getTitleText());

    }


    // Check the actual conditional page with a "true" flag
    private void checkJstlIf01a() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-if-01.jsp?cond=true");
        assertEquals("Correct page title",
                "jstl-if-01", page.getTitleText());
        assertEquals("Correct body element",
                "[First] [Second] [Third]", getBodyText(page));

    }


    // Check the actual conditional page with a "false" flag
    private void checkJstlIf01b() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-if-01.jsp?cond=false");
        assertEquals("Correct page title",
                "jstl-if-01", page.getTitleText());
        assertEquals("Correct body element",
                "[First] [Third]", getBodyText(page));

    }


    // Check the actual facet page with true/true/true flags
    private void checkJstlIf02a() throws Exception {

        HtmlPage page = getPage
                (
                        "/faces/jsp/jstl-if-02.jsp?component=true&header=true&footer=true");
        assertEquals("Correct page title",
                "jstl-if-02", page.getTitleText());
        assertEquals("Correct body element",
                "[First] [Header] [Second] [Footer] [Third]",
                getBodyText(page));

    }


    // Check the actual facet page with true/true/false flags
    private void checkJstlIf02b() throws Exception {

        HtmlPage page = getPage
                (
                        "/faces/jsp/jstl-if-02.jsp?component=true&header=true&footer=false");
        assertEquals("Correct page title",
                "jstl-if-02", page.getTitleText());
        assertEquals("Correct body element",
                "[First] [Header] [Second] [] [Third]",
                getBodyText(page));

    }


    // Check the actual facet page with true/false/true flags
    private void checkJstlIf02c() throws Exception {

        HtmlPage page = getPage
                (
                        "/faces/jsp/jstl-if-02.jsp?component=true&header=false&footer=true");
        assertEquals("Correct page title",
                "jstl-if-02", page.getTitleText());
        assertEquals("Correct body element",
                "[First] [] [Second] [Footer] [Third]",
                getBodyText(page));

    }


    // Check the actual facet page with true/false/false flags
    private void checkJstlIf02d() throws Exception {

        HtmlPage page = getPage
                (
                        "/faces/jsp/jstl-if-02.jsp?component=true&header=false&footer=false");
        assertEquals("Correct page title",
                "jstl-if-02", page.getTitleText());
        assertEquals("Correct body element",
                "[First] [] [Second] [] [Third]",
                getBodyText(page));

    }


    // Check the actual facet page with false/true/true flags
    private void checkJstlIf02e() throws Exception {

        HtmlPage page = getPage
                (
                        "/faces/jsp/jstl-if-02.jsp?component=false&header=true&footer=true");
        assertEquals("Correct page title",
                "jstl-if-02", page.getTitleText());
        assertEquals("Correct body element",
                "[First] [Third]",
                getBodyText(page));

    }


    // Check the actual template page with a "true" flag
    private void checkJstlIf03a() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-if-03.jsp?cond=true");
        assertEquals("Correct page title",
                "jstl-if-03", page.getTitleText());
        assertEquals("Correct body element",
                "[1] [2] [3] [4] [5]", getBodyText(page));

    }


    // Check the actual conditional page with a "false" flag
    private void checkJstlIf03b() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-if-03.jsp?cond=false");
        assertEquals("Correct page title",
                "jstl-if-03", page.getTitleText());
        assertEquals("Correct body element",
                "[1] [5]", getBodyText(page));

    }


    // Check the actual conditional page with a "true" flag
    private void checkJstlIf04a() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-if-04.jsp?cond=true");
        assertEquals("Correct page title",
                "jstl-if-04", page.getTitleText());
        assertEquals("Correct body element",
                "[1] [2] [3] { [4a] [4b] [4c] } [5] [6] [7]",
                getBodyText(page));

    }


    // Check the actual conditional page with a "false" flag
    private void checkJstlIf04b() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-if-04.jsp?cond=false");
        assertEquals("Correct page title",
                "jstl-if-04", page.getTitleText());
        assertEquals("Correct body element",
                "[1] [7]", getBodyText(page));

    }


    // Check the reset page to force a new component tree
    private void checkJstlImport00() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-import-00.jsp");
        assertEquals("Correct page title",
                "jstl-import-00", page.getTitleText());

    }


    // Check imports with literal text
    private void checkJstlImport01() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-import-01.jsp");
        assertEquals("Correct page title",
                "jstl-import-01", page.getTitleText());
        assertEquals("Correct body element",
                "[A] [B] [C] [D] [E]", getBodyText(page));

    }


    // Check imports with simple components
    private void checkJstlImport02() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-import-02.jsp");
        assertEquals("Correct page title",
                "jstl-import-02", page.getTitleText());
        assertEquals("Correct body element",
                "[A] [B] [C] [D] [E]", getBodyText(page));

    }


    // Check selective imports with simple components (explicit ids)
    private void checkJstlImport03a() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-import-03.jsp?choose=a");
        assertEquals("Correct page title",
                "jstl-import-03", page.getTitleText());
        assertEquals("Correct body element",
                "[1] [2a][2z] [3]", getBodyText(page));

    }


    // Check selective imports with simple components (explicit ids)
    private void checkJstlImport03b() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-import-03.jsp?choose=b");
        assertEquals("Correct page title",
                "jstl-import-03", page.getTitleText());
        assertEquals("Correct body element",
                "[1] [2b][2y] [3]", getBodyText(page));

    }


    // Check selective imports with simple components (explicit ids)
    private void checkJstlImport03c() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-import-03.jsp?choose=c");
        assertEquals("Correct page title",
                "jstl-import-03", page.getTitleText());
        assertEquals("Correct body element",
                "[1] [2c][2x] [3]", getBodyText(page));

    }


    // Check selective imports with simple components (naming container)
    private void checkJstlImport04a() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-import-04.jsp?choose=a");
        assertEquals("Correct page title",
                "jstl-import-04", page.getTitleText());
        assertEquals("Correct body element",
                "[1] [2a][2z] [3]", getBodyText(page));

    }


    // Check selective imports with simple components (naming container)
    private void checkJstlImport04b() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-import-04.jsp?choose=b");
        assertEquals("Correct page title",
                "jstl-import-04", page.getTitleText());
        assertEquals("Correct body element",
                "[1] [2b][2y] [3]", getBodyText(page));

    }


    // Check selective imports with simple components (naming container)
    private void checkJstlImport04c() throws Exception {

        HtmlPage page = getPage("/faces/jsp/jstl-import-04.jsp?choose=c");
        assertEquals("Correct page title",
                "jstl-import-04", page.getTitleText());
        assertEquals("Correct body element",
                "[1] [2c][2x] [3]", getBodyText(page));

    }


    // Retrieve the jstl-foreach-01 page
    private HtmlPage getJstlForEach01() throws Exception {

        return (getPage("/faces/jsp/jstl-foreach-01.jsp"));

    }


}
