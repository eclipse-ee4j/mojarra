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
import static junit.framework.TestCase.assertTrue;

public class AjaxTableITCase extends HtmlUnitFacesITCase {

    public AjaxTableITCase(String name) {
        super(name);
    }

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        super.setUp();
    }


    /*
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(AjaxTableITCase.class));
    }


    /*
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }


    /*
       Test each component to see that it behaves correctly when used with an Ajax tag
     */
    public void testAjaxTable() throws Exception {
        getPage("/faces/ajax/ajaxTable.xhtml");
        System.out.println("Start ajax table test");

        assertTrue(check("table:2:inCity","Boston"));

        // Check on the text field
        HtmlTextInput intext = ((HtmlTextInput)lastpage.getHtmlElementById("table:2:inCity"));
        intext.setValueAttribute("");
        intext.focus();
        intext.type("test");
        intext.blur();

        checkTrue("table:2:inCity","test");
        System.out.println("Text Checked");

        // Check on the checkbox

        checkTrue("table:3:cheesepref","Eww");

        HtmlCheckBoxInput checked = ((HtmlCheckBoxInput)lastpage.getHtmlElementById("table:3:cheesecheck"));
        lastpage = (HtmlPage)checked.click();

        checkTrue("table:3:cheesepref","Cheese Please");
        System.out.println("Boolean Checkbox Checked");

        checkTrue("table:4:count", "4");
        HtmlAnchor countlink = (HtmlAnchor) lastpage.getHtmlElementById("table:4:countlink");
        lastpage = countlink.click();

        checkTrue("table:4:count", "5");
        checkTrue("count","1");


        HtmlSubmitInput button = (HtmlSubmitInput)lastpage.getHtmlElementById("submitButton");
        lastpage = button.click();
        checkTrue("table:0:count", "6");
        checkTrue("count","1");
        
    }

}
