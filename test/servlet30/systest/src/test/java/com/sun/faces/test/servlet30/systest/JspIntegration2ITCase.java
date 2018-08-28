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
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>
 * Test Case for JSP Interoperability.
 * </p>
 */

public class JspIntegration2ITCase extends HtmlUnitFacesITCase {

    // ------------------------------------------------------------ Constructors

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public JspIntegration2ITCase(String name) {
        super(name);
    }

    // ------------------------------------------------------ Instance Variables

    // ---------------------------------------------------- Overall Test Methods

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(JspIntegration2ITCase.class));
    }

    // ------------------------------------------------- Individual Test Methods

    public void test6992760() throws Exception {
        HtmlPage page = getPage("/faces/jsp/6992760.jsp");
        assertTrue(page.asText().matches("(?s).*Message:\\s*class\\s*java.lang.String.*"));
    }

}
