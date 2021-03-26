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
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;

import java.util.ArrayList;
import java.util.List;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

public class AjaxRedirectITCase extends HtmlUnitFacesITCase {

    public AjaxRedirectITCase(String name) {
        super(name);
    }

    /*
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        super.setUp();
    }


    /*
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(AjaxRedirectITCase.class));
    }


    /*
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }


    // ------------------------------------------------------------ Test Methods


    public void testAjaxRedirect() throws Exception {

        HtmlPage page = getPage("/faces/ajax/ajaxRedirect.xhtml");
        HtmlSubmitInput button = (HtmlSubmitInput)
              getInputContainingGivenId(page, "form:redirect");
        assertNotNull(button);

        page = button.click();

        List<HtmlSpan> spans = new ArrayList<HtmlSpan>(1);
        getAllElementsOfGivenClass(page, spans, HtmlSpan.class);
        assertEquals(1, spans.size());
        HtmlSpan span = spans.get(0);
        assertEquals("Redirect Target", span.asText());
        
    }
}
