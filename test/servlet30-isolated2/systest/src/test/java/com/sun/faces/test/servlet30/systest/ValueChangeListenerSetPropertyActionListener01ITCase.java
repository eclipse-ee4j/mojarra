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

/**
 * Unit tests for Composite Components.
 */
public class ValueChangeListenerSetPropertyActionListener01ITCase extends HtmlUnitFacesITCase {

    public ValueChangeListenerSetPropertyActionListener01ITCase() {
        this("ValueChangeListenerSetPropertyActionListener01TestCase");
    }

    public ValueChangeListenerSetPropertyActionListener01ITCase(String name) {
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
        return (new TestSuite(ValueChangeListenerSetPropertyActionListener01ITCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // -------------------------------------------------------------- Test Cases

    /**
     * <p>
     * Maps ActionListener to commandButton within composite/actionSource1.xhtml using only the name
     * attribute.
     * </p>
     */
    public void testValueChangeActionListener() throws Exception {

        HtmlPage page = getPage("/faces/composite/valueChangeListenerSetPropertyActionListener01.xhtml");
        HtmlInput input = getInputContainingGivenId(page, "form:composite:value");
        input.setValueAttribute("Cause A ValueChangeEvent");
        HtmlSubmitInput button = (HtmlSubmitInput) getInputContainingGivenId(page, "form:composite:submit");
        page = button.click();
        String pageText = page.asText();

        assertTrue(-1 != pageText.indexOf("ValueChangeSetPropertyActionListenerBean.processValueChange called"));

        String searchString = "Property set by setPropertyActionListener:";
        int searchStringLength = searchString.length(), i = 0, j = 0;
        long lesser, greater;

        // Get the value of the property set by the setPropertyActionListener
        assertTrue(-1 != (i = pageText.indexOf(searchString)));
        i += searchStringLength;
        assertTrue(-1 != (j = pageText.indexOf(";", i)));
        lesser = Long.valueOf(pageText.substring(i, j));

        // Get the value of the currentTimeMillis at the time the page rendered
        searchString = "System.currentTimeMillis():";
        searchStringLength = searchString.length();
        assertTrue(-1 != (i = pageText.indexOf(searchString)));
        i += searchStringLength;
        assertTrue(-1 != (j = pageText.indexOf(";", i)));
        greater = Long.valueOf(pageText.substring(i, j));

        assertTrue(lesser < greater);

    }

}
