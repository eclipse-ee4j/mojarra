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
import static junit.framework.TestCase.assertTrue;
import junit.framework.TestSuite;


/**
 * <p>Verify that required validation occurrs for Select* components.</p>
 */

public class SelectItemEscapeITCase extends HtmlUnitFacesITCase {

    // ------------------------------------------------------------ Constructors


    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public SelectItemEscapeITCase(String name) {
        super(name);
    }

    // ------------------------------------------------------ Instance Variables

    // ---------------------------------------------------- Overall Test Methods


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
        return (new TestSuite(SelectComponentValueITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }

    // ------------------------------------------------------ Instance Variables

    // ------------------------------------------------- Individual Test Methods

    /**
     * <p>Verify that the required validator works for SelectOne</p>
     */

    public void testSelectOneNoValue() throws Exception {
        HtmlPage page = getPage("/faces/selectItemEscape.jsp");

        assertTrue(-1 != page.asText().indexOf("menu1_Wayne &lt;Gretzky&gt;"));
        assertTrue(-1 != page.asText().indexOf("menu1_Bobby +Orr+"));
        assertTrue(-1 != page.asText().indexOf("menu1_Brad &amp;{Park}"));
        assertTrue(-1 != page.asText().indexOf("menu1_Brad &amp;{Park}"));

        assertTrue(-1 != page.asText().indexOf("menu2_Wayne &lt;Gretzky&gt;"));
        assertTrue(-1 != page.asText().indexOf("menu2_Bobby +Orr+"));
        assertTrue(-1 != page.asText().indexOf("menu2_Brad &amp;{Park}"));
        assertTrue(-1 != page.asText().indexOf("menu2_Brad &amp;{Park}"));

        assertTrue(-1 != page.asText().indexOf("menu3_Wayne <Gretzky>"));
        assertTrue(-1 != page.asText().indexOf("menu3_Bobby +Orr+"));
        assertTrue(-1 != page.asText().indexOf("menu3_Brad &{Park}"));
        assertTrue(-1 != page.asText().indexOf("menu3_Gordie &Howe&"));

    }

}

