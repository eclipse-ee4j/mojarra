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

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.List;

/**
 * <p>
 * Verify that we have an intelligent error message when the user forgets the view tag.
 * </p>
 */

public class FormELITCase extends HtmlUnitFacesITCase {

    // ------------------------------------------------------------ Constructors

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public FormELITCase(String name) {
        super(name);
    }

    // ------------------------------------------------------ Instance Variables

    // ---------------------------------------------------- Overall Test Methods

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
        return (new TestSuite(FormELITCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // ------------------------------------------------------ Instance Variables

    // ------------------------------------------------- Individual Test Methods

    public void testFormEnctype() throws Exception {
        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = getPage("/faces/jsp/formEl.jsp");
        List forms = page.getForms();
        assertEquals("multipart/form-data", ((HtmlForm) forms.get(0)).getEnctypeAttribute());
        assertEquals("application/x-www-form-urlencoded", ((HtmlForm) forms.get(1)).getEnctypeAttribute());
        assertEquals("hi", ((HtmlForm) forms.get(2)).getEnctypeAttribute());
        assertEquals("multipart/form-data", ((HtmlForm) forms.get(3)).getEnctypeAttribute());
    }

}
