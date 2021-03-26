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

import java.util.ArrayList;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>Test Case for JSP Interoperability.</p>
 */

public class ComponentMiscITCase extends HtmlUnitFacesITCase {


    // ------------------------------------------------------------ Constructors


    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public ComponentMiscITCase(String name) {
        super(name);
    }


    // ------------------------------------------------------ Instance Variables


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
        return (new TestSuite(ComponentMiscITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }


    // ------------------------------------------------- Individual Test Methods


    public void testModelCoercionForUISelectOne() throws Exception {
        HtmlPage page = getPage("/faces/ModelSelectItemConversion.jsp");
        HtmlSelect select = (HtmlSelect) getAllElementsOfGivenClass(page, 
                                                                    new ArrayList(),
                                                                    HtmlSelect.class).get(0);
        HtmlOption option = select.getOption(1);
        option.setSelected(true);
        
        HtmlSubmitInput submit = (HtmlSubmitInput) getInputContainingGivenId(page,
                                                                             "submit");
        
        // clicking this should yield no errors.
        submit.click();
        
    }
    
    public void testConverterForUISelectMany() throws Exception {
        HtmlPage page = getPage("/faces/SelectManyConverterTest.jsp");
        List selects = getAllElementsOfGivenClass(page, 
                                                  new ArrayList(), 
                                                  HtmlSelect.class);
        HtmlSelect select = (HtmlSelect) selects.get(0);
        HtmlSelect select2 = (HtmlSelect) selects.get(1);
        select.getOption(1).setSelected(true);
        select.getOption(2).setSelected(true);
        select2.getOption(1).setSelected(true);
        select2.getOption(2).setSelected(true);
        
        HtmlSubmitInput submit = (HtmlSubmitInput) getInputContainingGivenId(page,
                                                                             "submit");
        page = (HtmlPage) submit.click();
        
        // ensure no validator errors
        String pageText = page.asText();
        assertTrue(pageText.indexOf("Value is not valid") < 0);
    }
}
