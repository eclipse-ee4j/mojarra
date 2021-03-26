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

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

import junit.framework.Test;
import junit.framework.TestSuite;

public class Issue2194ITCase extends HtmlUnitFacesITCase {

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public Issue2194ITCase(String name) {
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
        return (new TestSuite(Issue2194ITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }

    public void testCoreListeners() throws Exception {
        HtmlPage page = getPage("/faces/listeners.xhtml");
        assertTrue(page.asText().contains("RENDER_RESPONSE 6"));
        HtmlForm form = getFormById(page, "form");
        HtmlSubmitInput submit = (HtmlSubmitInput)
            form.getInputByName("button1");
        page = (HtmlPage) submit.click();
        assertTrue(page.asText().contains("button1 was pressed"));
        submit = (HtmlSubmitInput)
            form.getInputByName("button2");
        page = (HtmlPage) submit.click();
        assertTrue(page.asText().contains("button2 was pressed"));
        HtmlInput input = (HtmlInput)
            form.getInputByName("input1");
        input.setValueAttribute("Foo");
        submit = (HtmlSubmitInput)
            form.getInputByName("submit");
        page = (HtmlPage) submit.click();
        assertTrue(page.asText().contains("input1 value was changed"));
        input = (HtmlInput)
            form.getInputByName("input2");
        input.setValueAttribute("Bar");
        submit = (HtmlSubmitInput)
            form.getInputByName("submit");
        page = (HtmlPage) submit.click();
        assertTrue(page.asText().contains("input2 value was changed"));
    }
}
        


