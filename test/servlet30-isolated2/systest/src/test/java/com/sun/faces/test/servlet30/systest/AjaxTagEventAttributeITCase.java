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

import static com.gargoylesoftware.htmlunit.javascript.host.event.Event.TYPE_CHANGE;

import com.gargoylesoftware.htmlunit.html.*;

public class AjaxTagEventAttributeITCase extends HtmlUnitFacesITCase {

    public AjaxTagEventAttributeITCase(String name) {
        super(name);
    }

    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    /*
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return new TestSuite(AjaxTagEventAttributeITCase.class);
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    public void testAjaxTagEventAttribute() throws Exception {
        getPage("/faces/ajax/ajaxTagEventAttribute.xhtml");
        System.out.println("Start ajax tag event attribute test");

        // Check initial values
        checkTrue("out1", "0");
        checkTrue("out2", "1");
        checkTrue("out3", "");
        checkTrue("checkedvalue1", "false");
        checkTrue("checkedvalue2", "false");

        // Press Count
        HtmlSubmitInput button = (HtmlSubmitInput) lastpage.getHtmlElementById("button");
        lastpage = (HtmlPage) button.click();

        checkTrue("out1", "0");
        checkTrue("out2", "0");

        HtmlTextInput input = (HtmlTextInput) lastpage.getHtmlElementById("in1");
        input.setText("test");
        input.fireEvent(TYPE_CHANGE);
        getClient().waitForBackgroundJavaScript(6000);

        checkTrue("out3", "test");

        // Check ajax checkbox
        HtmlCheckBoxInput checked = ((HtmlCheckBoxInput) lastpage.getHtmlElementById("checkbox1"));
        lastpage = (HtmlPage) checked.click();

        checkTrue("checkedvalue1", "true");

        // Check ajax checkbox
        checked = ((HtmlCheckBoxInput) lastpage.getHtmlElementById("checkbox2"));
        lastpage = (HtmlPage) checked.click();

        checkTrue("checkedvalue2", "true");

        // Check ajax checkbox
        checked = ((HtmlCheckBoxInput) lastpage.getHtmlElementById("checkbox3"));
        lastpage = (HtmlPage) checked.click();

        checkTrue("checkedvalue3", "true");

        // Check that all ajax requests didn't result in a reload
        checkTrue("out4", "2");

    }

}
