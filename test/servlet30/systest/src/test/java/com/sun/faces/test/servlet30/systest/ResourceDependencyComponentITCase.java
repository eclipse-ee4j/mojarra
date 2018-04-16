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

import org.w3c.dom.NodeList;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;


/**
 * Unit tests for Composite Components.
 */
public class ResourceDependencyComponentITCase extends HtmlUnitFacesITCase {


    public ResourceDependencyComponentITCase() {
        this("ResourceDependencyComponentTestCase");
    }

    public ResourceDependencyComponentITCase(String name) {
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
        return (new TestSuite(ResourceDependencyComponentITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }
    

    // -------------------------------------------------------------- Test Cases

    public void testForwardingToNextPageProcessesResourceDependencies() throws Exception {
        HtmlPage page = getPage("/faces/composite/resourceDependencyComponentUsingPage.xhtml");
        assertNrOfLinksPresent(page, 1);
        HtmlInput button = getInputContainingGivenId(page, "navigateAway");
        page = button.click();
        assertTrue(-1 != page.asText().indexOf("Next page"));
        assertNrOfLinksPresent(page, 1);
    }
    
    public void testStayingOnSamePageProcessesResourceDependencies() throws Exception {
        HtmlPage page = getPage("/faces/composite/resourceDependencyComponentUsingPage.xhtml");
        assertNrOfLinksPresent(page, 1);
        HtmlInput button = getInputContainingGivenId(page, "stay");
        page = button.click();
        assertTrue(-1 != page.asText().indexOf("Using page"));
        assertNrOfLinksPresent(page, 1);
    }

    private void assertNrOfLinksPresent(HtmlPage page, int number) {
        NodeList nodeList = page.getElementsByTagName("link");
        assertEquals(1, nodeList.getLength());
    }

}
