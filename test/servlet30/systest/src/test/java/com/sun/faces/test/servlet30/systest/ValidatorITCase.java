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
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.List;

/**
 * <p>
 * Test that invalid values don't cause valueChangeEvents to occur.
 * </p>
 */

public class ValidatorITCase extends HtmlUnitFacesITCase {

    // ------------------------------------------------------------ Constructors

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public ValidatorITCase(String name) {
        super(name);
    }

    // ------------------------------------------------------ Instance Variables

    // ---------------------------------------------------- Overall Test Methods

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(ValidatorITCase.class));
    }

    // ------------------------------------------------- Individual Test Methods
    public void testValidator() throws Exception {
        HtmlPage page = getPage("/faces/validator02.jsp");
        List list;
        list = getAllElementsOfGivenClass(page, null, HtmlTextInput.class);

        // set the initial value to be 1 for all input fields
        ((HtmlTextInput) list.get(0)).setValueAttribute("1111111111");
        ((HtmlTextInput) list.get(1)).setValueAttribute("1111111111");
        ((HtmlTextInput) list.get(2)).setValueAttribute("1111111111");
        ((HtmlTextInput) list.get(3)).setValueAttribute("1111111111");
        ((HtmlTextInput) list.get(4)).setValueAttribute("1111111111");
        ((HtmlTextInput) list.get(5)).setValueAttribute("1111111111");
        ((HtmlTextInput) list.get(6)).setValueAttribute("1111111111");

        list = getAllElementsOfGivenClass(page, null, HtmlSubmitInput.class);
        HtmlSubmitInput button = (HtmlSubmitInput) list.get(0);
        page = (HtmlPage) button.click();
        String text = page.asText();
        assertTrue(text.contains("text1 was validated"));
        assertTrue(text.contains("text2 was validated"));
        assertTrue(text.contains("text3 was validated"));
        assertTrue(text.contains("text4 was validated"));
        String str = "allowable maximum of " + '"' + "2" + '"';
        assertTrue(text.contains(str));
        assertTrue(text.contains("allowable maximum of '5'"));

    }

    public void testValidatorMessages() throws Exception {
        HtmlPage page = getPage("/faces/validator03.jsp");
        List list;
        list = getAllElementsOfGivenClass(page, null, HtmlTextInput.class);

        // set the initial value to be "1" for all input fields
        for (int i = 0; i < list.size(); i++) {
            ((HtmlTextInput) list.get(i)).setValueAttribute("1");
        }

        list = getAllElementsOfGivenClass(page, null, HtmlSubmitInput.class);
        HtmlSubmitInput button = (HtmlSubmitInput) list.get(0);
        page = (HtmlPage) button.click();
        String text = page.asXml();
        assertTrue(text.contains("Validation Error: Specified attribute is not between the expected values of 2 and 5."));
        assertTrue(text.contains("DoubleRange2: Validation Error: Specified attribute is not between the expected values of 2 and 5."));
        assertTrue(text.contains("Validation Error: Length is less than allowable minimum of '2'"));
        assertTrue(text.contains("Length2: Validation Error: Length is less than allowable minimum of '2'"));
        assertTrue(text.contains("Validation Error: Specified attribute is not between the expected values of 2 and 5."));
        assertTrue(text.contains("LongRange2: Validation Error: Specified attribute is not between the expected values of 2 and 5."));
    }

    public void testRequiredValidatorMessage() throws Exception {
        HtmlPage page = getPage("/faces/validator04.jsp");
        List list;
        list = getAllElementsOfGivenClass(page, null, HtmlTextInput.class);
        ((HtmlTextInput) list.get(2)).setValueAttribute("a");
        ((HtmlTextInput) list.get(3)).setValueAttribute("a");
        ((HtmlTextInput) list.get(4)).setValueAttribute("20");
        ((HtmlTextInput) list.get(5)).setValueAttribute("20");
        list = getAllElementsOfGivenClass(page, null, HtmlSubmitInput.class);
        page = (HtmlPage) ((HtmlSubmitInput) list.get(0)).click();
        String text = page.asText();
        assertTrue(text.contains("Literal Message"));
        assertTrue(text.contains("New String Value"));
        assertTrue(text.contains("Converter Literal"));
        assertTrue(text.contains("Converter Message Expression"));
        assertTrue(text.contains("Validator Literal"));
        assertTrue(text.contains("Validator Message Expression"));
    }

}
