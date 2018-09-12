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
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test cases for Facelets functionality
 */
public class FaceletsTargetNotFoundITCase extends HtmlUnitFacesITCase {

    // --------------------------------------------------------------- Test Init

    public FaceletsTargetNotFoundITCase() {
        this("FaceletsTargetNotFoundTestCase");
    }

    public FaceletsTargetNotFoundITCase(String name) {
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
        return (new TestSuite(FaceletsTargetNotFoundITCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // ------------------------------------------------------------ Test Methods

    /*
     * Check that not found errors give a 500 status, instead of the 404 that they used to give.
     */
    public void testFaillingCode() throws Exception {

        WebClient client = new WebClient();
        client.getOptions().setThrowExceptionOnFailingStatusCode(true);
        HtmlPage page;
        int code;

        try {
            page = getPage("/faces/facelets/compositionnotfound.xhtml", client);
            fail("A Failing status code was not received");
        } catch (FailingHttpStatusCodeException fail) {
            code = fail.getStatusCode();
            assertTrue("Expected 500, got: " + code, code == 500);
        }
        try {
            page = getPage("/faces/facelets/decoratenotfound.xhtml", client);
            fail("A Failing status code was not received");
        } catch (FailingHttpStatusCodeException fail) {
            code = fail.getStatusCode();
            assertTrue("Expected 500, got: " + code, code == 500);
        }
        try {
            page = getPage("/faces/facelets/includenotfound.xhtml", client);
            fail("A Failing status code was not received");
        } catch (FailingHttpStatusCodeException fail) {
            code = fail.getStatusCode();
            assertTrue("Expected 500, got: " + code, code == 500);
        }

    }

    public void testNotFoundCode() throws Exception {
        WebClient client = new WebClient();
        client.getOptions().setThrowExceptionOnFailingStatusCode(true);
        HtmlPage page;
        int code;

        try {
            page = getPage("/faces/missing-top-level-page.xhtml", client);
            fail("A Failing status code was not received");
        } catch (FailingHttpStatusCodeException fail) {
            code = fail.getStatusCode();
            assertTrue("Expected 404, got: " + code, code == 404);
        }

    }

}
