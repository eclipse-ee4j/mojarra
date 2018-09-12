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
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Jason Lee
 */

public class MissingActionListenerMethodITCase extends HtmlUnitFacesITCase {
    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public MissingActionListenerMethodITCase(String name) {
        super(name);
    }

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(MissingActionListenerMethodITCase.class));
    }

    // ------------------------------------------------- Individual Test Methods

    public void testMissingActionListenerMethod() throws Exception {
        HtmlPage page = getPage("/faces/jsp/testMissingActionListenerMethod.jsp");
        HtmlSubmitInput button = (HtmlSubmitInput) page.getHtmlElementById("testForm:testButton");
        try {
            button.click();
            Assert.fail();
        } catch (Exception e) {
        }
    }
}
