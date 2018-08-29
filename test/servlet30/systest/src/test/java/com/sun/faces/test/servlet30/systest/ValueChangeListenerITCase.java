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

import com.gargoylesoftware.htmlunit.html.*;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.List;

/**
 * <p>
 * Test that invalid values don't cause valueChangeEvents to occur.
 * </p>
 */

public class ValueChangeListenerITCase extends HtmlUnitFacesITCase {

    // ------------------------------------------------------------ Constructors

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public ValueChangeListenerITCase(String name) {
        super(name);
    }

    // ------------------------------------------------------ Instance Variables

    // ---------------------------------------------------- Overall Test Methods

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(ValueChangeListenerITCase.class));
    }

    // ------------------------------------------------- Individual Test Methods
    public void testValueChangeListener() throws Exception {
        HtmlPage page = getPage("/faces/valueChangeListener.jsp");
        List list;
        list = getAllElementsOfGivenClass(page, null, HtmlTextInput.class);

        // set the initial value to be 1 for both fields
        ((HtmlTextInput) list.get(0)).setValueAttribute("1");
        ((HtmlTextInput) list.get(1)).setValueAttribute("1");

        list = getAllElementsOfGivenClass(page, null, HtmlSubmitInput.class);
        HtmlSubmitInput button = (HtmlSubmitInput) list.get(0);
        page = (HtmlPage) button.click();

        assertTrue(-1 != page.asText().indexOf("Received valueChangeEvent for textA"));

        assertTrue(-1 != page.asText().indexOf("Received valueChangeEvent for textB"));

        // re-submit the form, make sure no valueChangeEvents are fired
        list = getAllElementsOfGivenClass(page, null, HtmlSubmitInput.class);
        button = (HtmlSubmitInput) list.get(0);
        page = (HtmlPage) button.click();

        assertTrue(-1 == page.asText().indexOf("Received valueChangeEvent for textA"));

        assertTrue(-1 == page.asText().indexOf("Received valueChangeEvent for textB"));

        // give invalid values to one field and make sure no
        // valueChangeEvents are fired.
        list = getAllElementsOfGivenClass(page, null, HtmlTextInput.class);

        ((HtmlTextInput) list.get(1)).setValueAttribute("-123");

        list = getAllElementsOfGivenClass(page, null, HtmlSubmitInput.class);
        button = (HtmlSubmitInput) list.get(0);
        page = (HtmlPage) button.click();

        assertTrue(-1 == page.asText().indexOf("Received valueChangeEvent for textA"));

        assertTrue(-1 == page.asText().indexOf("Received valueChangeEvent for textB"));

        assertTrue(-1 != page.asText().indexOf("Validation Error"));

        // make sure dir and lang are passed through as expected for
        // message and messages
        list = getAllElementsOfGivenClass(page, null, HtmlSpan.class);

        boolean hasMessageContent = false, // do we have the h:message
                // content we're looking for
                hasMessagesContent = false; // do we have the h:messages
        // content we're looking for.
        HtmlSpan span = null;
        HtmlUnorderedList ulist = null;

        for (int i = 0; i < list.size(); i++) {
            span = (HtmlSpan) list.get(i);
            if (-1 != span.asXml().indexOf("dir=\"RTL\"") && span.asXml().indexOf("lang=\"de\"") != -1) {
                hasMessageContent = true;
            }
        }
        list = getAllElementsOfGivenClass(page, null, HtmlUnorderedList.class);
        for (int i = 0; i < list.size(); i++) {
            ulist = (HtmlUnorderedList) list.get(i);
            if (-1 != ulist.asXml().indexOf("dir=\"LTR\"") && ulist.asXml().indexOf("lang=\"en\"") != -1) {
                hasMessagesContent = true;
            }
        }
        assertTrue(hasMessagesContent && hasMessageContent);

    }

    // Test case for Issue 752
    // https://javaserverfaces.dev.java.net/issues/show_bug.cgi?id=752
    public void testValueChangeListener02() throws Exception {

        HtmlPage page = getPage("/faces/valueChangeListener02.jsp");
        List list = getAllElementsOfGivenClass(page, null, HtmlSubmitInput.class);
        HtmlSubmitInput button = (HtmlSubmitInput) list.get(0);
        page = (HtmlPage) button.click();
        assertTrue(!page.asText().contains("old value"));
        assertTrue(!page.asText().contains("new value"));

        list = getAllElementsOfGivenClass(page, null, HtmlCheckBoxInput.class);
        HtmlCheckBoxInput input = (HtmlCheckBoxInput) list.get(0);
        input.setChecked(false);
        list = getAllElementsOfGivenClass(page, null, HtmlSubmitInput.class);
        button = (HtmlSubmitInput) list.get(0);
        page = (HtmlPage) button.click();

        assertTrue(page.asText().contains("old value: true"));
        assertTrue(page.asText().contains("new value: false"));

        list = getAllElementsOfGivenClass(page, null, HtmlSubmitInput.class);
        button = (HtmlSubmitInput) list.get(0);
        page = (HtmlPage) button.click();
        assertTrue(!page.asText().contains("old value"));
        assertTrue(!page.asText().contains("new value"));

    }

}
