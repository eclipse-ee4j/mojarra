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

import java.util.List;
import java.util.ArrayList;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlInput;

/**
 * Validate new EL features such as the component implicit object
 */
public class ELITCase extends HtmlUnitFacesITCase {

    public ELITCase(String name) {
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
        return (new TestSuite(ELITCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // ------------------------------------------------------------ Test Methods
    public void testComponentImplicitObject() throws Exception {
        HtmlPage page = getPage("/faces/componentImplicitObject.jsp");
        List<HtmlSpan> outputs = new ArrayList<HtmlSpan>(2);
        getAllElementsOfGivenClass(page, outputs, HtmlSpan.class);
        assertTrue(outputs.size() == 2);
        HtmlSpan s = outputs.get(0);
        assertTrue("ot".equals(s.getId()));
        assertTrue("ot".equals(s.asText()));
        s = outputs.get(1);
        assertTrue(s.getId().contains("facetOT"));
        assertTrue("facetOT".equals(s.asText()));
        List<HtmlInput> inputs = new ArrayList<HtmlInput>(2);
        getAllElementsOfGivenClass(page, inputs, HtmlInput.class);
        HtmlInput i = inputs.get(2);
        assertTrue(i.getId().contains("0:it"));
        assertTrue("it".equals(i.asText()));
        i = inputs.get(3);
        assertTrue(i.getId().contains("1:it"));
        assertTrue("it".equals(i.asText()));
    }

    public void testProgrammaticExpressionFunctionEval() throws Exception {
        HtmlPage page = getPage("/faces/elfunction.xhtml");
        assertTrue(page.asText().contains("PASSED"));
    }
}
