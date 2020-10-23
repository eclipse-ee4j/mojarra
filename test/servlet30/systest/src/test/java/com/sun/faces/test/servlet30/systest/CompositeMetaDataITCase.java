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

public class CompositeMetaDataITCase extends HtmlUnitFacesITCase {

    public CompositeMetaDataITCase(String name) {
        super(name);
        addExclusion(HtmlUnitFacesITCase.Container.TOMCAT6, "testPrefixMappedFaceletPage");
        addExclusion(HtmlUnitFacesITCase.Container.TOMCAT7, "testPrefixMappedFaceletPage");
        addExclusion(HtmlUnitFacesITCase.Container.WLS_10_3_4_NO_CLUSTER, "testPrefixMappedFaceletPage");
        addExclusion(HtmlUnitFacesITCase.Container.TOMCAT6, "testExtensionMappedFaceletPage");
        addExclusion(HtmlUnitFacesITCase.Container.TOMCAT7, "testExtensionMappedFaceletPage");
        addExclusion(HtmlUnitFacesITCase.Container.WLS_10_3_4_NO_CLUSTER, "testExtensionMappedFaceletPage");

        addExclusion(HtmlUnitFacesITCase.Container.TOMCAT6, "testPrefixMappedFaceletPage");
        addExclusion(HtmlUnitFacesITCase.Container.TOMCAT7, "testPrefixMappedFaceletPage");
        addExclusion(HtmlUnitFacesITCase.Container.WLS_10_3_4_NO_CLUSTER, "testPrefixMappedFaceletPage");
        addExclusion(HtmlUnitFacesITCase.Container.TOMCAT6, "testExtensionMappedFaceletPage");
        addExclusion(HtmlUnitFacesITCase.Container.TOMCAT7, "testExtensionMappedFaceletPage");
        addExclusion(HtmlUnitFacesITCase.Container.WLS_10_3_4_NO_CLUSTER, "testExtensionMappedFaceletPage");

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
        return (new TestSuite(CompositeMetaDataITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }


    /**
     * Added for issue 10
     *
     * @throws Exception
     */
    public void testPrefixMappedFaceletPage() throws Exception {

//        HtmlPage page = getPage("/faces/composite/jsr276-using.xhtml");
//        String text = page.asText();
//        assertTrue(-1 != text.indexOf("composite component with correctly specified jsr276 metadata"));
//        assertTrue(-1 == text.indexOf("prefix fmd"));
//        assertTrue(-1 != text.indexOf("prefix metaData"));
    }

    public void testExtensionMappedFaceletPage() throws Exception {

//        HtmlPage page = getPage("/composite/jsr276-using.faces");
//        String text = page.asText();
//        assertTrue(-1 != text.indexOf("composite component with correctly specified jsr276 metadata"));
//        assertTrue(-1 != text.indexOf("composite component with incorrectly specified jsr276 metadata"));
    }

   public void testDirectlyAccessedCompositeComponent() throws Exception {

//       HtmlPage page = null;
//       client.getOptions().setThrowExceptionOnFailingStatusCode(false);
//       page = getPage("/faces/resources/composite/jsr276Correct01.xhtml");
//       String text = page.asText();
//       assertTrue(text.contains("Component Not Found for identifier"));
    }

    

} // end of class PathTestCase
