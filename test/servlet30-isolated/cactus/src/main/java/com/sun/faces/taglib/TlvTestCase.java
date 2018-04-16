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

package com.sun.faces.taglib;


import junit.framework.TestCase;
import junit.framework.Test;
import junit.framework.TestSuite;


public class TlvTestCase extends TestCase {


    // ------------------------------------------------------ Instance Variables


    public TlvTestCase(String name) {
        super(name);
    }


    // ---------------------------------------------------- Overall Test Methods


    // Set up instance variables required by this test case.
    public void setUp() { }



    // Return the tests included in this test case.
    public static Test suite() {

        return (new TestSuite(TlvTestCase.class));

    }


    // Tear down instance variables required by this test case.
    public void tearDown() {

    }


    // ------------------------------------------------- Individual Test Methods


    public void testDesignTime() {

        java.beans.Beans.setDesignTime(true);
        TestHtmlBasicValidator hbv = new TestHtmlBasicValidator();
        org.xml.sax.helpers.DefaultHandler handler  =  hbv.getSAXHandler();
        assertTrue(null == handler);

        java.beans.Beans.setDesignTime(false);
        handler =  hbv.getSAXHandler();
        assertTrue(null != handler);

        java.beans.Beans.setDesignTime(true);
        TestCoreValidator cv = new TestCoreValidator();
        handler  =  cv.getSAXHandler();
        assertTrue(null == handler);

        java.beans.Beans.setDesignTime(false);
        handler  =  cv.getSAXHandler();
        assertTrue(null != handler);
    }

}
