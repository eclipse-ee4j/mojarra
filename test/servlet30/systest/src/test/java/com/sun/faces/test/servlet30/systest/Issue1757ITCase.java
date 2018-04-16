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
import junit.framework.Test;
import static junit.framework.TestCase.assertTrue;
import junit.framework.TestSuite;


/**
 * Test cases for Facelets functionality
 */
public class Issue1757ITCase extends HtmlUnitFacesITCase {


    // --------------------------------------------------------------- Test Init


    public Issue1757ITCase() {
        this("Issue1757TestCase");
    }


    public Issue1757ITCase(String name) {
        super(name);
    }


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
        return (new TestSuite(Issue1757ITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }


    // ------------------------------------------------------------ Test Methods


    public void testDynamicComponents() throws Exception {
        HtmlPage page = getPage("/faces/facelets/issue1757-dynamic-components.xhtml");
        String text = page.asText();
        assertTrue(text.matches("(?s).*TestComponent::encodeBegin\\s*Manually added child\\s*Dynamically added child\\s*TestComponent::encodeEnd.*"));
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();
        text = page.asText();
        assertTrue(text.matches("(?s).*TestComponent::encodeBegin\\s*Manually added child\\s*Dynamically added child\\s*TestComponent::encodeEnd.*"));
        
    }

}
