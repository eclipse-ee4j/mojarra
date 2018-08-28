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

public class DynamicStateITCase extends HtmlUnitFacesITCase {

    public DynamicStateITCase(String name) {
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
        return (new TestSuite(DynamicStateITCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    public void testDynamicDeletionPrefix() throws Exception {
        doTestDynamicDeletion("/faces/state/dynamicDeletion.xhtml");
    }

    public void testDynamicDeletionExtension() throws Exception {
        doTestDynamicDeletion("/state/dynamicDeletion.faces");
    }

    public void testDynamicAdditionPrefix() throws Exception {
        doTestDynamicAddition("/faces/state/dynamicAddition.xhtml");
    }

    public void testDynamicAdditionExtension() throws Exception {
        doTestDynamicAddition("/state/dynamicAddition.faces");
    }

    /*
     * Added for issue 1183.
     */
    public void testNestedComponentAddition() throws Exception {

        HtmlPage page = getPage("/faces/state/dynamicAddition2.xhtml");
        HtmlSubmitInput submit = (HtmlSubmitInput) getInputContainingGivenId(page, "form:render");
        page = submit.click();
        HtmlTextInput input = (HtmlTextInput) getInputContainingGivenId(page, "form:textInput");
        assertNotNull(input);
        assertEquals("default value", input.getValueAttribute());
        input.setValueAttribute("new value");
        submit = (HtmlSubmitInput) getInputContainingGivenId(page, "form:render");
        page = submit.click();
        input = (HtmlTextInput) getInputContainingGivenId(page, "form:textInput");
        assertNotNull(input);
        assertEquals("new value", input.getValueAttribute());

        // ensure events are fired properly when adding tree deltas
        // to the view
        submit = (HtmlSubmitInput) getInputContainingGivenId(page, "form:submit");
        page = submit.click();
        input = (HtmlTextInput) getInputContainingGivenId(page, "form:textInput");
        assertNotNull(input);

        // once more for good measure
        submit = (HtmlSubmitInput) getInputContainingGivenId(page, "form:submit");
        page = submit.click();
        input = (HtmlTextInput) getInputContainingGivenId(page, "form:textInput");
        assertNotNull(input);

    }

    /**
     * Added for issue 1185.
     */
    public void testDeleteAddSameAction() throws Exception {

        HtmlPage page = getPage("/faces/state/dynamicAdditionDeletion.xhtml");
        HtmlSubmitInput submit = (HtmlSubmitInput) getInputContainingGivenId(page, "form:render");
        page = submit.click();

        // first click removes children from the panel (should be empty)
        // and adds a new button
        assertTrue(page.asText().contains("dynamically added button"));

        for (int i = 0; i < 5; i++) {
            // repeated clicks will remove the single child and add a new button
            // back.
            submit = (HtmlSubmitInput) getInputContainingGivenId(page, "form:render");
            page = submit.click();

            assertTrue(page.asText().contains("dynamically added button"));
        }

    }

    /**
     * Added for issue 1553.
     */
    public void testDynamicAdditionTransietSubTree() throws Exception {

        // any exception thrown here will fail the test
        client.getOptions().setThrowExceptionOnFailingStatusCode(true);
        getPage("/faces/state/dynamicAdditionTransientSubTree.xhtml");

    }

    // --------------------------------------------------------- Private Methods

    private void doTestDynamicDeletion(String viewId) throws Exception {
        HtmlPage page = getPage(viewId);
        HtmlTextInput textField = (HtmlTextInput) getInputContainingGivenId(page, "textField");
        textField.setValueAttribute("some text");
        HtmlSubmitInput button = (HtmlSubmitInput) getInputContainingGivenId(page, "reload");
        try {
            page = (HtmlPage) button.click();
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue(-1 == page.asText().indexOf("cbutton should not be found"));
    }

    private void doTestDynamicAddition(String viewId) throws Exception {
        HtmlPage page = getPage(viewId);
        HtmlTextInput textField = (HtmlTextInput) getInputContainingGivenId(page, "textField");
        textField.setValueAttribute("some text");
        HtmlSubmitInput button = (HtmlSubmitInput) getInputContainingGivenId(page, "reload");
        try {
            page = (HtmlPage) button.click();
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue(-1 == page.asText().indexOf("cbutton should be found"));
    }

} // end of class PathTestCase
