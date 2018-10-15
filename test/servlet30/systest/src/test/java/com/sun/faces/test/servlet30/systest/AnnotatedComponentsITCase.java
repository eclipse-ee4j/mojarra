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


import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import junit.framework.Test;
import static junit.framework.TestCase.assertTrue;
import junit.framework.TestSuite;


import org.junit.Ignore;

@Ignore
public class AnnotatedComponentsITCase extends HtmlUnitFacesITCase {


    // ------------------------------------------------------------ Constructors


    public AnnotatedComponentsITCase(String name) {
        super(name);
        addExclusion(HtmlUnitFacesITCase.Container.WLS_10_3_4_NO_CLUSTER, "testAnnotations");
        addExclusion(HtmlUnitFacesITCase.Container.WLS_12_1_1_NO_CLUSTER, "testAnnotations");
        addExclusion(HtmlUnitFacesITCase.Container.TOMCAT6, "testAnnotations");
        addExclusion(HtmlUnitFacesITCase.Container.TOMCAT7, "testAnnotations");
    }

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(AnnotatedComponentsITCase.class));
    }


    // ------------------------------------------------------------ Test Methods


    public void testAnnotations() throws Exception {
        HtmlPage page = getPage("/faces/annotationtest.xhtml");
        if (!page.asXml().toUpperCase().contains("TOMCAT") &&
                !page.asXml().toUpperCase().contains("WEBLOGIC")) {
            List<HtmlSpan> output = new ArrayList<HtmlSpan>(1);
            getAllElementsOfGivenClass(page, output, HtmlSpan.class);
            assertTrue(output.size() == 1);
            HtmlSpan span = output.get(0);
            // assertTrue(span.asText().contains("true"));
            // assertTrue(page.asText().contains("AnnotatedPhaseListener: Hello World from env-entry!"));        
        }
    }
}
