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

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
//import com.gargoylesoftware.htmlunit.javascript.host.HTMLAnchorElement;
import junit.framework.Test;
import junit.framework.TestSuite;
import static junit.framework.TestCase.assertTrue;


/**
 * Unit tests for Composite Components.
 */
public class ValueChangeListenerCalledITCase extends HtmlUnitFacesITCase {


    public ValueChangeListenerCalledITCase() {
        this("ValueChangeListenerCalledTestCase");
    }

    public ValueChangeListenerCalledITCase(String name) {
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
        return (new TestSuite(ValueChangeListenerCalledITCase.class));
    }


    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }
    

    // -------------------------------------------------------------- Test Cases

    public void testValueChangeListenerCalled() throws Exception {

        HtmlPage page = getPage("/faces/listener-1729.xhtml");
        HtmlTextInput textField = (HtmlTextInput) page.getElementById("test");
        textField.setValueAttribute("0");
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("button");
        page = button.click();
        String text = page.asText();
        String currentTime = "" + System.currentTimeMillis();
        currentTime = currentTime.substring(0, 7);
System.out.println("TEXT:"+text);
System.out.println("CURRENTTIME:"+currentTime);
        assertTrue(text.contains("Aufgerufen: " + currentTime));
        assertTrue(text.contains("Hello from processValueChange: " + currentTime));

        textField = (HtmlTextInput) page.getElementById("test");
        textField.setValueAttribute("3");
        HtmlAnchor anchor = (HtmlAnchor) page.getElementById("link");
        page = anchor.click();
        text = page.asText();
        currentTime = "" + System.currentTimeMillis();
        currentTime = currentTime.substring(0, 7);
System.out.println("TEXT:"+text);
System.out.println("CURRENTTIME:"+currentTime);
        assertTrue(text.contains("Aufgerufen: " + currentTime));
        assertTrue(text.contains("Hello from processValueChange: " + currentTime));


    }

}

    

