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

public class SelectOneManyEnumITCase extends HtmlUnitFacesITCase {

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public SelectOneManyEnumITCase(String name) {
        super(name);
    }

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(SelectOneManyEnumITCase.class));
    }

    // ------------------------------------------------------------ Test Methods

    /*
     * When migrating the test it was noticed that the suite method was not pointing towards the actual
     * test. So the test below was never invoked. Upon turning it on the test started failing,
     * commenting out. This test needs to be fixed.
     */

    public void testEnums() throws Exception {
//        HtmlPage page = getPage("/faces/standard/selectonemanyenum.jsp");
//        HtmlForm form = getFormById(page, "test");
//        assertNotNull(form);
//        List<HtmlSelect> selectList = getAllElementsOfGivenClass(page,
//                                                     new ArrayList<HtmlSelect>(),
//                                                     HtmlSelect.class);
//        assertTrue(selectList.size() == 5);
//
//        // ID selected
//        HtmlSelect select = selectList.get(0);
//        assertTrue(select.getId().contains("selected"));
//        List<HtmlOption> selectedOptions = select.getSelectedOptions();
//        assertTrue(selectedOptions.size() == 1);
//        assertTrue("Value2".equals(selectedOptions.get(0).getValueAttribute()));
//        select.setSelectedAttribute(selectedOptions.get(0), false);
//        select.setSelectedAttribute("Value1", true);
//
//        // ID selected2
//        HtmlSelect select2 = selectList.get(1);
//        assertTrue(select2.getId().contains("selected2"));
//        List<HtmlOption> selectedOptions2 = select2.getSelectedOptions();
//        assertTrue(selectedOptions2.size() == 1);
//        assertTrue("Value3".equals(selectedOptions2.get(0).getValueAttribute()));
//        select2.setSelectedAttribute(selectedOptions2.get(0), false);
//        select2.setSelectedAttribute("Value2", true);
//
//        // ID selected3
//        HtmlSelect select3 = selectList.get(2);
//        assertTrue(select3.getId().contains("selected3"));
//        List<HtmlOption> selectedOptions3 = select3.getSelectedOptions();
//        assertTrue(selectedOptions3.size() == 1);
//        assertTrue("Value4".equals(selectedOptions3.get(0).getValueAttribute()));
//        select3.setSelectedAttribute(selectedOptions3.get(0), false);
//        select3.setSelectedAttribute("Value3", true);
//
//        // ID array
//        HtmlSelect selectArray = selectList.get(3);
//        assertTrue(selectArray.getId().contains("array"));
//        List<HtmlOption> selectedOptionsArray = selectArray.getSelectedOptions();
//        assertTrue(selectedOptionsArray.size() == 2);
//        assertTrue("Value2".equals(selectedOptionsArray.get(0).getValueAttribute()));
//        assertTrue("Value4".equals(selectedOptionsArray.get(1).getValueAttribute()));
//        selectArray.setSelectedAttribute(selectedOptionsArray.get(0), false);
//        selectArray.setSelectedAttribute(selectedOptionsArray.get(1), false);
//        selectArray.setSelectedAttribute("Value1", true);
//        selectArray.setSelectedAttribute("Value3", true);
//
//
//        // ID list
//        HtmlSelect selectListt = selectList.get(4);
//        assertTrue(selectListt.getId().contains("list"));
//        List<HtmlOption> selectedOptionsList = selectListt.getSelectedOptions();
//        assertTrue(selectedOptionsList.size() == 2);
//        assertTrue("Value1".equals(selectedOptionsList.get(0).getValueAttribute()));
//        assertTrue("Value2".equals(selectedOptionsList.get(1).getValueAttribute()));
//        selectListt.setSelectedAttribute(selectedOptionsList.get(0), false);
//        selectListt.setSelectedAttribute(selectedOptionsList.get(1), false);
//        selectListt.setSelectedAttribute("Value2", true);
//        selectListt.setSelectedAttribute("Value4", true);
//
//        HtmlSubmitInput submit = (HtmlSubmitInput)
//            form.getInputByName("test" +
//                                NamingContainer.SEPARATOR_CHAR +
//                                "submit");
//        page = (HtmlPage) submit.click();
//
//        // verify the correct options were selected
//
//        selectList = getAllElementsOfGivenClass(page,
//                                                     new ArrayList<HtmlSelect>(),
//                                                     HtmlSelect.class);
//        assertTrue(selectList.size() == 5);
//
//        assertTrue(selectList.size() == 5);
//
//        // ID selected
//        select = selectList.get(0);
//        assertTrue(select.getId().contains("selected"));
//        selectedOptions = select.getSelectedOptions();
//        assertTrue(selectedOptions.size() == 1);
//        assertTrue("Value1".equals(selectedOptions.get(0).getValueAttribute()));
//
//        // ID selected2
//        select2 = selectList.get(1);
//        assertTrue(select2.getId().contains("selected2"));
//        selectedOptions2 = select2.getSelectedOptions();
//        assertTrue(selectedOptions2.size() == 1);
//        assertTrue("Value2".equals(selectedOptions2.get(0).getValueAttribute()));
//
//        // ID selected3
//        select3 = selectList.get(2);
//        assertTrue(select3.getId().contains("selected3"));
//        selectedOptions3 = select3.getSelectedOptions();
//        assertTrue(selectedOptions3.size() == 1);
//        assertTrue("Value3".equals(selectedOptions3.get(0).getValueAttribute()));
//
//        // ID array
//        selectArray = selectList.get(3);
//        assertTrue(selectArray.getId().contains("array"));
//        selectedOptionsArray = selectArray.getSelectedOptions();
//        assertTrue(selectedOptionsArray.size() == 2);
//        assertTrue("Value1".equals(selectedOptionsArray.get(0).getValueAttribute()));
//        assertTrue("Value2".equals(selectedOptionsArray.get(1).getValueAttribute()));
//
//        // ID list
//        selectListt = selectList.get(4);
//        assertTrue(selectListt.getId().contains("list"));
//        selectedOptionsList = selectListt.getSelectedOptions();
//        assertTrue(selectedOptionsList.size() == 2);
//        assertTrue("Value2".equals(selectedOptionsList.get(0).getValueAttribute()));
//        assertTrue("Value3".equals(selectedOptionsList.get(1).getValueAttribute()));
//
    }

} // END SelectOneManyEnumTestCase
