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

import com.gargoylesoftware.htmlunit.CollectingAlertHandler;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.ArrayList;
import java.util.List;

public class AjaxAttrsITCase extends HtmlUnitFacesITCase {

    public AjaxAttrsITCase(String name) {
        super(name);
    }

    /*
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    /*
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(AjaxAttrsITCase.class));
    }

    /*
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // ------------------------------------------------------------ Test Methods

    public void testAjaxAttrs() throws Exception {

        List<String> collectedAlerts = new ArrayList<String>(1);
        client.setAlertHandler(new CollectingAlertHandler(collectedAlerts));

        HtmlPage page = getPage("/faces/ajax/ajaxAttrs.xhtml");

        HtmlSubmitInput button = (HtmlSubmitInput) getInputContainingGivenId(page, "form:attr");
        assertNotNull(button);

        String value = button.getValueAttribute();

        assertTrue("expected Dummy but got " + value, "Dummy".equals(value));

        page = button.click();

        button = (HtmlSubmitInput) getInputContainingGivenId(page, "form:attr");

        value = button.getValueAttribute();

        assertTrue("expected New Value but got " + value, "New Value".equals(value));

        String onclick = button.getOnClickAttribute();
        assertTrue(onclick.matches("(?s).*'delay':'none'.*"));

        button = (HtmlSubmitInput) getInputContainingGivenId(page, "form:attrImplicitDelay");

        value = button.getValueAttribute();

        assertTrue("expected Dummy Implicit Delay but got " + value, "Dummy Implicit Delay".equals(value));
        onclick = button.getOnClickAttribute();
        assertTrue(!onclick.matches("(?s).*'delay':'none'.*"));

        button = (HtmlSubmitInput) getInputContainingGivenId(page, "form:attrDelay");

        value = button.getValueAttribute();

        assertTrue("expected Dummy Delay but got " + value, "Dummy Delay".equals(value));
        onclick = button.getOnClickAttribute();
        assertTrue(onclick.matches("(?s).*'delay':'200'.*"));

    }
}
