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

/**
 * Unit tests for Composite Components.
 */
public class VerifyBuildBeforeRestoreITCase extends HtmlUnitFacesITCase {

    public VerifyBuildBeforeRestoreITCase() {
        this("VerifyBuildBeforeRestoreTestCase");
    }

    public VerifyBuildBeforeRestoreITCase(String name) {
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
        return (new TestSuite(VerifyBuildBeforeRestoreITCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // -------------------------------------------------------------- Test Cases

    public void testVerifyBuildBeforeRestore() throws Exception {

        if (Boolean.TRUE.equals(Boolean.valueOf(System.getProperty("partial.state.saving")))) {
            HtmlPage page = getPage("/faces/regression/verifyBuildBeforeRestore.xhtml");
            HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("submit");

            page = button.click();
            String text = page.asText();
            assertTrue(text.contains("Message: javax.faces.event.PostAddToViewEvent javax.faces.event.PostRestoreStateEvent"));
            page = getPage("/faces/regression/uninstallListeners.xhtml");
        }

    }

}
