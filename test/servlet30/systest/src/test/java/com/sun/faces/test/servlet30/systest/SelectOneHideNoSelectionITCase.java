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

import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;


public class SelectOneHideNoSelectionITCase extends HtmlUnitFacesITCase {


    private HtmlPage page;

	/**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public SelectOneHideNoSelectionITCase(String name) {
        super(name);
    }


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        super.setUp();
        this.page = getPage(getPath());
    }


	protected String getPath() {
		return "/faces/standard/selectOneLiteralHideNoSelectionOption.xhtml";
	}


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(SelectOneHideNoSelectionITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }


    // ------------------------------------------------------------ Test Methods


    public void testSelectItemsWithSelectedValueDoesntShowNoSelectionOption() throws Exception {
        HtmlSelect select = (HtmlSelect) this.page.getElementById("f:selectItemsSelectedValue");
        assertEquals(4, select.getOptionSize());
    }
    
    public void testSelectItemsWithNoSelectedValueShowsNoSelectionOption() throws Exception {
    	HtmlSelect select = (HtmlSelect) this.page.getElementById("f:selectItemsNoSelectedValue");
    	assertEquals(5, select.getOptionSize());
    }
    
    public void testSelectItemsWithNoSelectionSelectedValueShowsNoSelectionOption() throws Exception {
    	HtmlSelect select = (HtmlSelect) this.page.getElementById("f:selectItemsNoSelectionSelectedValue");
    	assertEquals(5, select.getOptionSize());
    }
    
    public void testSelectItemWithSelectedValueDoesntShowNoSelectionOption() throws Exception {
    	HtmlSelect select = (HtmlSelect) this.page.getElementById("f:selectItemSelectedValue");
    	assertEquals(4, select.getOptionSize());
    }
    
    //same test as testSelectItemWithSelectedValueDoesntShowNoSelectionOption, but now the
    //no selection option is put as last option in the selectOneMenu
    public void testSelectItemAsLastWithSelectedValueDoesntShowNoSelectionOption() throws Exception {
    	HtmlSelect select = (HtmlSelect) this.page.getElementById("f:selectItemSelectedValueLast");
    	assertEquals(4, select.getOptionSize());
    }
    
    public void testSelectItemWithNoSelectedValueShowsNoSelectionOption() throws Exception {
    	HtmlSelect select = (HtmlSelect) this.page.getElementById("f:selectItemNoSelectedValue");
    	assertEquals(5, select.getOptionSize());
    }
    
    public void testSelectItemWithNoSelectionSelectedValueShowsNoSelectionOption() throws Exception {
    	HtmlSelect select = (HtmlSelect) this.page.getElementById("f:selectItemNoSelectionSelectedValue");
    	assertEquals(5, select.getOptionSize());
    }
    
    
    public void testSelectItemsWithSelectedValueNoHidingShowsNoSelectionOption() throws Exception {
    	HtmlSelect select = (HtmlSelect) this.page.getElementById("f:selectItemsSelectedValueNoHiding");
    	assertEquals(5, select.getOptionSize());
    }
    
    public void testSelectItemsWithNoSelectedValueNoHidingShowsNoSelectionOption() throws Exception {
    	HtmlSelect select = (HtmlSelect) this.page.getElementById("f:selectItemsNoSelectedValueNoHiding");
    	assertEquals(5, select.getOptionSize());
    }
    
    public void testSelectItemsWithNoSelectionSelectedValueNoHidingShowsNoSelectionOption() throws Exception {
    	HtmlSelect select = (HtmlSelect) this.page.getElementById("f:selectItemsNoSelectionSelectedValueNoHiding");
    	assertEquals(5, select.getOptionSize());
    }
    
    public void testSelectItemWithSelectedValueNoHidingShowsNoSelectionOption() throws Exception {
    	HtmlSelect select = (HtmlSelect) this.page.getElementById("f:selectItemSelectedValueNoHiding");
    	assertEquals(5, select.getOptionSize());
    }
    
    public void testSelectItemWithNoSelectedValueNoHidingShowsNoSelectionOption() throws Exception {
    	HtmlSelect select = (HtmlSelect) this.page.getElementById("f:selectItemNoSelectedValueNoHiding");
    	assertEquals(5, select.getOptionSize());
    }
    
    public void testSelectItemWithNoSelectionSelectedValueNoHidingShowsNoSelectionOption() throws Exception {
    	HtmlSelect select = (HtmlSelect) this.page.getElementById("f:selectItemNoSelectionSelectedValueNoHiding");
    	assertEquals(5, select.getOptionSize());
    }

}
