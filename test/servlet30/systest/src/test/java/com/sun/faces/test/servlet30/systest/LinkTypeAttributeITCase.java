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
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlElement;


/**
 * Test case for issue 1098.
 */
public class LinkTypeAttributeITCase extends HtmlUnitFacesITCase {

    // ------------------------------------------------------------ Constructors


       /**
        * Construct a new instance of this test case.
        *
        * @param name Name of the test case
        */
       public LinkTypeAttributeITCase(String name) {
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
           return (new TestSuite(LinkTypeAttributeITCase.class));
       }


       /**
        * Tear down instance variables required by this test case.
        */
       public void tearDown() {
           super.tearDown();
       }


    // ------------------------------------------------------------ Test Methods


    public void testLinkAttributeNotDuplicated() throws Exception {

        String type ="type=\"type\"";
        HtmlPage page = getPage("/faces/standard/linkTypeAttribute.xhtml");

        HtmlElement link = (HtmlElement) page.getElementById("form:clink");
        String xml = link.asXml();
        int idx = xml.indexOf(type);
        assertTrue(idx != -1);
        idx = xml.indexOf(type, idx + type.length());
        assertTrue(idx == -1);

        link = (HtmlElement) page.getElementById("form:link");
        xml = link.asXml();
        idx = xml.indexOf(type);
        assertTrue(idx != -1);
        idx = xml.indexOf(type, idx + type.length());
        assertTrue(idx == -1);

    }

}
