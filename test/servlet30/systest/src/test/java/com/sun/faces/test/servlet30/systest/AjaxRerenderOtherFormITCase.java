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

import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import junit.framework.Test;
import static junit.framework.TestCase.assertTrue;
import junit.framework.TestSuite;

public class AjaxRerenderOtherFormITCase  extends HtmlUnitFacesITCase {

    public AjaxRerenderOtherFormITCase(String name) {
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
        return (new TestSuite(AjaxRerenderOtherFormITCase.class));
    }


    /*
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }


    public void testRerenderingOtherForm() throws Exception {
        HtmlPage htmlPage = getPage("/faces/ajax/ajaxRerenderOtherForm.xhtml");
        
        HtmlInput ajaxButton = getInputContainingGivenId(htmlPage, "button2");
        HtmlPage rerenderedPage = ajaxButton.click();
        
        HtmlInput nonAjaxButton = getInputContainingGivenId(rerenderedPage, "button1");
        HtmlPage finalPage = nonAjaxButton.click();
        assertTrue(-1 != finalPage.asText().indexOf("It was a postback!!!"));
        
    }
}

