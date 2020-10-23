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

import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;

import junit.framework.Test;
import junit.framework.TestSuite;


public class AjaxSelectITCase extends HtmlUnitFacesITCase {

    public AjaxSelectITCase(String name) {
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
        return (new TestSuite(AjaxTagWrappingITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }


    /*
       Test each component to see that it behaves correctly when used with an Ajax tag
     */
    public void testAjaxSelect() throws Exception {
        getPage("/faces/ajax/ajaxSelect.xhtml");

        checkTrue("out", "Pending");

        HtmlElement click = lastpage.getHtmlElementById("form:s1rad:0");

        lastpage = click.click();

        checkTrue("out", "radio-1");

        HtmlSelect select = lastpage.getHtmlElementById("form:s1menu");

        lastpage = (HtmlPage) select.setSelectedAttribute("2",true);

        checkTrue("out", "menu-2");

        select = lastpage.getHtmlElementById("form:s1list");

        lastpage = (HtmlPage) select.setSelectedAttribute("2",true);

        checkTrue("out", "list-2");

        select = lastpage.getHtmlElementById("form:smlist");

        lastpage = (HtmlPage) select.setSelectedAttribute("2",true);

        checkTrue("out", "mlist-2");

        click = lastpage.getHtmlElementById("form:smcheck:0");

        lastpage = click.click();

        checkTrue("out", "mcheck-1");

        click = lastpage.getHtmlElementById("form:bool");

        lastpage = click.click();

        checkTrue("out", "PASSED");


        // Now, reload everything and do it again.
        // This tests for bug 1339
        click = lastpage.getHtmlElementById("form:button");

        click.click();

        checkTrue("out", "Pending");

        click = lastpage.getHtmlElementById("form:s1rad:0");

        lastpage = click.click();

        checkTrue("out", "radio-1");

        select = lastpage.getHtmlElementById("form:s1menu");

        lastpage = (HtmlPage) select.setSelectedAttribute("2",true);

        checkTrue("out", "menu-2");

        select = lastpage.getHtmlElementById("form:s1list");

        lastpage = (HtmlPage) select.setSelectedAttribute("2",true);

        checkTrue("out", "list-2");

        select = lastpage.getHtmlElementById("form:smlist");

        lastpage = (HtmlPage) select.setSelectedAttribute("2",true);

        checkTrue("out", "mlist-2");

        click = lastpage.getHtmlElementById("form:smcheck:0");

        lastpage = click.click();

        checkTrue("out", "mcheck-1");

        click = lastpage.getHtmlElementById("form:bool");

        lastpage = click.click();

        checkTrue("out", "PASSED");

    }
}
