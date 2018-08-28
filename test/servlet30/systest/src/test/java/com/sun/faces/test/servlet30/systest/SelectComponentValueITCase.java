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
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.List;
import java.util.ResourceBundle;

/**
 * <p>
 * Verify that required validation occurrs for Select* components.
 * </p>
 */

public class SelectComponentValueITCase extends HtmlUnitFacesITCase {

    // ------------------------------------------------------------ Constructors

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public SelectComponentValueITCase(String name) {
        super(name);
    }

    // ------------------------------------------------------ Instance Variables

    // ---------------------------------------------------- Overall Test Methods

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
        return (new TestSuite(SelectComponentValueITCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // ------------------------------------------------------ Instance Variables

    // ------------------------------------------------- Individual Test Methods

    /**
     * <p>
     * Verify that the required validator works for SelectOne
     * </p>
     */

    public void testSelectOneNoValue() throws Exception {
        HtmlPage page = getPage("/faces/jsp/selectOneNoValue.jsp");
        List list = getAllElementsOfGivenClass(page, null, HtmlSubmitInput.class);
        HtmlSubmitInput button = (HtmlSubmitInput) list.get(0);
        page = (HtmlPage) button.click();
        assertTrue(-1 != page.asText().indexOf("equired"));

    }

    /**
     * <p>
     * Verify that the required validator works for SelectMany
     * </p>
     */

    public void testSelectManyNoValue() throws Exception {
        HtmlPage page = getPage("/faces/jsp/selectManyNoValue.jsp");
        List list = getAllElementsOfGivenClass(page, null, HtmlSubmitInput.class);
        HtmlSubmitInput button = (HtmlSubmitInput) list.get(0);
        page = (HtmlPage) button.click();
        assertTrue(-1 != page.asText().indexOf("equired"));

    }

    /**
     * <p>
     * Verify that the conversion error works for SelectMany
     * </p>
     */

    public void testSelectManyMismatchValue() throws Exception {
//        HtmlPage page = getPage("/faces/jsp/selectManyMismatchValue.jsp");
//        List list = getAllElementsOfGivenClass(page, null,
//                HtmlSubmitInput.class);
//        HtmlSubmitInput button = (HtmlSubmitInput) list.get(0);
//        list = getAllElementsOfGivenClass(page, null, HtmlSelect.class);
//        HtmlSelect options = (HtmlSelect) list.get(0);
//        String chosen[] = {"one", "three"};
//        options.setSelectedAttribute("one", true);
//        options.setSelectedAttribute("three", true);
//        //options.fakeSelectedAttribute(chosen);
//        page = (HtmlPage) button.click();
//        assertTrue(-1 != page.asText().indexOf("one three"));
//        assertTrue(-1 != page.asText().indexOf("#{test3.selection}"));
//
    }

    /**
     * On SelectMany, test that the membership test works and doesn't produce spurious ValueChangeEvent
     * instances.
     */
    public void testSelectManyInvalidValue() throws Exception {
//        HtmlPage page = getPage("/faces/jsp/selectManyInvalidValue.jsp");
//        List list = getAllElementsOfGivenClass(page, null,
//                HtmlSubmitInput.class);
//        HtmlSubmitInput button = (HtmlSubmitInput) list.get(0);
//        list = getAllElementsOfGivenClass(page, null, HtmlSelect.class);
//        HtmlSelect options = (HtmlSelect) list.get(0);
//        Random random = new Random(4143);
//        String str = new String((new Float(random.nextFloat())).toString());
//
//        String chosen[] = {
//                (new Float(random.nextFloat())).toString(),
//                (new Float(random.nextFloat())).toString()
//        };
//        options.setSelectedAttribute((new Float(random.nextFloat())).toString(), true);
//        options.setSelectedAttribute((new Float(random.nextFloat())).toString(), true);
//        //options.fakeSelectedAttribute(chosen);
//        page = (HtmlPage) button.click();
//        ResourceBundle messages = ResourceBundle.getBundle(
//                "javax.faces.Messages");
//        String message = messages.getString("javax.faces.component.UISelectMany.INVALID");
//        // it does have a validation message
//        assertTrue(-1 != page.asText().indexOf("Validation Error"));
//        // it does not have a value change message
//        assertTrue(-1 == page.asText().indexOf("value changed"));
    }

    /**
     * run doInvalidTest on UISelectOne
     */

    public void testSelectOneInvalidValue() throws Exception {
        HtmlPage page = getPage("/faces/jsp/selectOneInvalidValue.jsp");

        List list = getAllElementsOfGivenClass(page, null, HtmlSubmitInput.class);
        HtmlSubmitInput button = (HtmlSubmitInput) list.get(0);
        list = getAllElementsOfGivenClass(page, null, HtmlRadioButtonInput.class);
        HtmlRadioButtonInput radio = (HtmlRadioButtonInput) list.get(0);
        radio.setChecked(true);
        page = (HtmlPage) button.click();
        ResourceBundle messages = ResourceBundle.getBundle("javax.faces.Messages");
        String message = messages.getString("javax.faces.component.UIInput.REQUIRED");
        // it does not have a validation error
        assertTrue(-1 == page.asText().indexOf(message));
    }

    public void testSelectOneTypeInt() throws Exception {
        HtmlPage page = getPage("/faces/jsp/selectOneTypeInt.jsp");
        List list = getAllElementsOfGivenClass(page, null, HtmlSubmitInput.class);
        HtmlSubmitInput button = (HtmlSubmitInput) list.get(0);
        list = getAllElementsOfGivenClass(page, null, HtmlSelect.class);
        HtmlSelect options = (HtmlSelect) list.get(0);

        String chosen = "2";
        options.setSelectedAttribute(chosen, true);
        // options.fakeSelectedAttribute(chosen);
        page = (HtmlPage) button.click();
        ResourceBundle messages = ResourceBundle.getBundle("javax.faces.Messages");
        String message = messages.getString("javax.faces.component.UISelectMany.INVALID");
        // it does not have a validation message
        assertTrue(-1 == page.asText().indexOf("Validation Error"));
    }

    // Verifies original selection from model is unchanged
    // when button with immediate="true" is pressed.
    //
    public void testSelectOneRadioTypeInteger() throws Exception {
        HtmlPage page = getPage("/faces/jsp/selectOneRadioTypeInteger.jsp");
        HtmlSubmitInput nonImmediateButton = null, immediateButton = null;
        HtmlRadioButtonInput radio1 = null, radio2 = null, radio3 = null;
        List submitList = getAllElementsOfGivenClass(page, null, HtmlSubmitInput.class);
        List radioList = getAllElementsOfGivenClass(page, null, HtmlRadioButtonInput.class);
        radio1 = (HtmlRadioButtonInput) radioList.get(0);
        radio2 = (HtmlRadioButtonInput) radioList.get(1);
        radio3 = (HtmlRadioButtonInput) radioList.get(2);
        for (int i = 0; i < submitList.size(); i++) {
            HtmlSubmitInput result = (HtmlSubmitInput) submitList.get(i);
            if (-1 != result.getAttribute("id").indexOf("nonImmediate")) {
                nonImmediateButton = result;
            } else {
                immediateButton = result;
            }
        }
        assertTrue(-1 != page.asText().indexOf("Model Selection:2"));

        // now press the immediate button and verify that our original
        // selection is there...
        page = (HtmlPage) immediateButton.click();
        assertTrue(-1 != page.asText().indexOf("Model Selection:2"));

        // now click the first radio option...
        radio1.setChecked(true);
        // now press the immediate button and verify that our original
        // selection is there...
        page = (HtmlPage) immediateButton.click();
        assertTrue(-1 != page.asText().indexOf("Model Selection:2"));

        // now click the first radio option...
        radio1.setChecked(true);
        // now press the non immediate button and verify that our new
        // selection is there...
        page = (HtmlPage) nonImmediateButton.click();
        assertTrue(-1 != page.asText().indexOf("Model Selection:1"));
    }

    public void testSelectOneRadioTypeInt() throws Exception {
//        HtmlPage page = getPage("/faces/jsp/selectOneRadioTypeInt.jsp");
//        HtmlSubmitInput nonImmediateButton = null, immediateButton = null;
//        HtmlRadioButtonInput radio1 = null, radio2 = null, radio3 = null;
//        List submitList = getAllElementsOfGivenClass(page, null, HtmlSubmitInput.class);
//        List radioList = getAllElementsOfGivenClass(page, null, HtmlRadioButtonInput.class);
//        radio1 = (HtmlRadioButtonInput) radioList.get(0);
//        radio2 = (HtmlRadioButtonInput) radioList.get(1);
//        radio3 = (HtmlRadioButtonInput) radioList.get(2);
//        for (int i = 0; i < submitList.size(); i++) {
//            HtmlSubmitInput result = (HtmlSubmitInput) submitList.get(i);
//            if (-1 != result.getIdAttribute().indexOf("nonImmediate")) {
//                nonImmediateButton = result;
//            } else {
//                immediateButton = result;
//            }
//        }
//        assertTrue(-1 != page.asText().indexOf("Model Selection:3"));
//
//        // now press the immediate button and verify that our original
//        // selection is there...
//        page = (HtmlPage) immediateButton.click();
//        assertTrue(-1 != page.asText().indexOf("Model Selection:3"));
//
//        // now click the first radio option...
//        radio1.setChecked(true);
//        // now press the immediate button and verify that our original
//        // selection is there...
//        page = (HtmlPage) immediateButton.click();
//        assertTrue(-1 != page.asText().indexOf("Model Selection:3"));
//
//        // now click the first radio option...
//        radio1.setChecked(true);
//        // now press the non immediate button and verify that our new
//        // selection is there...
//        page = (HtmlPage) nonImmediateButton.click();
//        assertTrue(-1 != page.asText().indexOf("Model Selection:1"));
    }

    public void testSelectManyTypeInts() throws Exception {
        HtmlPage page = getPage("/faces/jsp/selectManyTypeInts.jsp");
        List list = getAllElementsOfGivenClass(page, null, HtmlSubmitInput.class);
        HtmlSubmitInput button = (HtmlSubmitInput) list.get(0);
        list = getAllElementsOfGivenClass(page, null, HtmlSelect.class);
        HtmlSelect options = (HtmlSelect) list.get(0);

        String chosen[] = { "2", "3" };
        options.setSelectedAttribute("2", true);
        options.setSelectedAttribute("3", true);
        // options.fakeSelectedAttribute(chosen);
        page = (HtmlPage) button.click();
        ResourceBundle messages = ResourceBundle.getBundle("javax.faces.Messages");
        String message = messages.getString("javax.faces.component.UISelectMany.INVALID");
        // it does not have a validation message
        assertTrue(-1 == page.asText().indexOf("Validation Error"));
    }

}
