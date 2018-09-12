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
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import java.io.IOException;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Validate resource re-location of scripts and stylesheets
 */
public class TagAttributeListenerMethodExpressionNoArgITCase extends HtmlUnitFacesITCase {

    public TagAttributeListenerMethodExpressionNoArgITCase() {
        this("TagAttributeListenerMethodExpressionNoArgTestCase");
    }

    public TagAttributeListenerMethodExpressionNoArgITCase(String name) {
        super(name);
    }

    public static Test suite() {
        return (new TestSuite(TagAttributeListenerMethodExpressionNoArgITCase.class));
    }

    // ------------------------------------------------------------ Test Methods

    public void testResourceRelocation() throws Exception {

        HtmlPage page = getPage("/faces/TestValueChangeAndActionListenerNoArg.jsp");
        executeTest(page);
        page = getPage("/faces/TestValueChangeAndActionListenerNoArg.xhtml");
        executeTest(page);
    }

    // --------------------------------------------------------- Private Methods

    private void executeTest(HtmlPage page) throws IOException {
        HtmlTextInput usernameField = (HtmlTextInput) page.getHtmlElementById("username");
        usernameField.setValueAttribute("newValue, not oldValue");
        HtmlForm form = getFormById(page, "form");
        HtmlSubmitInput button = (HtmlSubmitInput) form.getInputByName("loginEvent");
        page = (HtmlPage) button.click();
        String text = page.asText();
        boolean hasExpectedValue = (-1 != text.indexOf("valueChange0Called:true")) || (-1 != text.indexOf("valueChange0Called: true"));
        assertTrue(hasExpectedValue);
        hasExpectedValue = (-1 != text.indexOf("actionListener0Called:true")) || (-1 != text.indexOf("actionListener0Called: true"));
        assertTrue(hasExpectedValue);
    }
}
