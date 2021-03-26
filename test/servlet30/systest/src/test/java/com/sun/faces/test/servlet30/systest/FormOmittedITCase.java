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
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

/**
 * <p>Test Case for checking if form is omitted when there is a button/link in the page.</p>
 */

public class FormOmittedITCase extends HtmlUnitFacesITCase {


    // ------------------------------------------------------------ Constructors


    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public FormOmittedITCase(String name) {
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
        return (new TestSuite(FormOmittedITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }


    // ------------------------------------------------- Individual Test Methods


    public void testFormOmitted() throws Exception {
        HtmlPage page = getPage("/faces/standard/formomitted.xhtml");
        String pageAsText = page.asXml();       
        if (pageAsText.contains("Project Stage: Development")) {
            assertTrue (pageAsText.contains("The form component needs to have a UIForm in its ancestry."));
        }
    }
    
    public void testFormNotOmitted() throws Exception {
        HtmlPage page = getPage("/faces/standard/formnotomitted.xhtml");
        String pageAsText = page.asXml();
        if (pageAsText.contains("Project Stage: Development")) {
            assertFalse (pageAsText.contains("The form component needs to have a UIForm in its ancestry. Suggestion: enclose the necessary components within <h:form>"));
        }
    }
    
    public void testFormOmittedForCC() throws Exception {
        HtmlPage page = getPage("/faces/standard/formomittedforcc.xhtml");
        String pageAsText = page.asXml();
        if (pageAsText.contains("Project Stage: Development")) {
            assertTrue (pageAsText.contains("The form component needs to have a UIForm in its ancestry."));
        }
    }

    public void testFormOmittedFormlayoutTemplates() throws Exception {
        HtmlPage page = getPage("/faces/standard/formlayouttemplates.xhtml");
        String pageAsText = page.asXml();
        if (pageAsText.contains("Project Stage: Development")) {
            assertTrue (pageAsText.contains("Page defined content"));
            assertFalse (pageAsText.contains("The form component needs to have a UIForm in its ancestry. Suggestion: enclose the necessary components within <h:form>"));
        }
    }
}
