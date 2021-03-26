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
import com.gargoylesoftware.htmlunit.html.HtmlHorizontalRule;
import com.gargoylesoftware.htmlunit.html.HtmlHeading2;
import junit.framework.Test;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;
import junit.framework.TestSuite;

public class AjaxInsertDeleteITCase extends HtmlUnitFacesITCase {

    public AjaxInsertDeleteITCase(String name) {
        super(name);
    }

    /*
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        super.setUp();
    }


    /*
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(AjaxInsertDeleteITCase.class));
    }


    /*
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }


    // ------------------------------------------------------------ Test Methods


    public void testInsertDelete() throws Exception {

        HtmlPage page = getPage("/faces/ajax/ajaxInsertDelete.xhtml");

        assertNull(getBeforeHeading(page));
        assertNull(getAfterHeading(page));

        HtmlSubmitInput beforeButton = getBeforeButton(page);
        assertNotNull(beforeButton);
        page = beforeButton.click();

        HtmlHeading2 beforeHeading = getBeforeHeading(page);
        assertNotNull(beforeHeading);
        assertTrue(beforeHeading.getNextSibling() instanceof HtmlHorizontalRule);
        assertNotNull(page.getElementById("trbefore"));

        HtmlSubmitInput afterButton = getAfterButton(page);
        assertNotNull(afterButton);
        page = afterButton.click();

        HtmlHeading2 afterHeading = getAfterHeading(page);
        assertNotNull(afterHeading);
        assertTrue(afterHeading.getPreviousSibling() instanceof HtmlHorizontalRule);
        assertNotNull(page.getElementById("trafter"));

        HtmlSubmitInput removeBefore = getRemoveBeforeButton(page);
        assertNotNull(removeBefore);
        page = removeBefore.click();

        assertNull(getBeforeHeading(page));
        assertNotNull(getAfterHeading(page));

        assertNull(page.getElementById("trbefore"));

        HtmlSubmitInput removeAfter = getRemoveAfterButton(page);
        assertNotNull(removeAfter);
        page = removeAfter.click();

        assertNull(page.getElementById("trafter"));

        assertNull(getBeforeHeading(page));
        assertNull(getAfterHeading(page));

    }


    // --------------------------------------------------------  Private Methods


    private HtmlSubmitInput getBeforeButton(HtmlPage page) {

        return (HtmlSubmitInput) getInputContainingGivenId(page, "form1:before");

    }


    private HtmlSubmitInput getAfterButton(HtmlPage page) {

        return (HtmlSubmitInput) getInputContainingGivenId(page, "form1:after");

    }


    private HtmlSubmitInput getRemoveBeforeButton(HtmlPage page) {

        return (HtmlSubmitInput) getInputContainingGivenId(page, "form1:removeBefore");

    }


    private HtmlSubmitInput getRemoveAfterButton(HtmlPage page) {

        return (HtmlSubmitInput) getInputContainingGivenId(page, "form1:removeAfter");

    }


    private HtmlHeading2 getBeforeHeading(HtmlPage page) {

        return (HtmlHeading2) page.getElementById("h2before");

    }


    private HtmlHeading2 getAfterHeading(HtmlPage page) {

        return (HtmlHeading2) page.getElementById("h2after");

    }
}
