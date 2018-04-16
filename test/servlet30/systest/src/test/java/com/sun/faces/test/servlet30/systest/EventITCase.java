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

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestSuite;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSpan;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * Validate new EL features such as the component implicit object
 */
public class EventITCase extends HtmlUnitFacesITCase {

    public EventITCase(String name) {
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
        return (new TestSuite(EventITCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
    }

    // ------------------------------------------------------------ Test Methods
    public void testValidEvents() throws Exception {
        HtmlPage page = getPage("/faces/eventTag.xhtml");
        List<HtmlSpan> outputs = new ArrayList<HtmlSpan>(4);
        getAllElementsOfGivenClass(page, outputs, HtmlSpan.class);
        assertTrue(outputs.size() == 6);
        validateOutput(outputs);

        HtmlSubmitInput submit = (HtmlSubmitInput) getInputContainingGivenId(page, "click");
        assertNotNull(submit);
        page = (HtmlPage) submit.click();
        outputs.clear();
        getAllElementsOfGivenClass(page, outputs, HtmlSpan.class);
        assertTrue(outputs.size() == 6);
        validateOutput(outputs);
    }

    public void testBeforeViewRender() throws Exception {
        HtmlPage page = getPage("/faces/eventTag01.xhtml");
        assertTrue(-1 != page.asText().indexOf("class javax.faces.component.UIViewRoot pre-render"));

        page = getPage("/faces/eventTag02.xhtml");
        assertTrue(-1 != page.asText().indexOf("class javax.faces.component.UIViewRoot pre-render"));

    }

    public void testInvalidEvent() throws Exception {
        try {
            getPage("/faces/eventTagInvalid.xhtml");
            fail("An exception should be thrown for an invalid event name in Development mode");
        } catch (FailingHttpStatusCodeException fail) {
            //
        }
    }

    public static void main(String... args) {
        try {
            EventITCase etc = new EventITCase("foo");
            etc.setUp();
            etc.testValidEvents();
            etc.testInvalidEvent();
            etc.tearDown();
        } catch (Exception ex) {
            Logger.getLogger(EventITCase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // --------------------------------------------------------- Private Methods
    private void validateOutput(List<HtmlSpan> outputs) {

        HtmlSpan s;

        // Short name
        s = outputs.get(0);
        assertTrue(("The 'javax.faces.event.PreRenderComponentEvent' event fired!").equals(s.asText()));

        // Long name
        s = outputs.get(1);
        assertTrue(("The 'javax.faces.event.PreRenderComponentEvent' event fired!").equals(s.asText()));

        // Short Name
        s = outputs.get(2);
        assertTrue(("The 'javax.faces.event.PostAddToViewEvent' event fired!").equals(s.asText()));

        // Long name
        s = outputs.get(3);
        assertTrue(("The 'javax.faces.event.PostAddToViewEvent' event fired!").equals(s.asText()));

        // Fully-qualified class name
        s = outputs.get(4);
        assertTrue(("The 'javax.faces.event.PreRenderComponentEvent' event fired!").equals(s.asText()));

        // No-arg
        s = outputs.get(5);
        assertTrue(("The no-arg event fired!").equals(s.asText()));

    }

    public void testPostAddParentCorrect1682() throws Exception {
        HtmlPage page = getPage("/faces/issue1682.xhtml");
        HtmlSubmitInput button = (HtmlSubmitInput) page.getElementById("reload");
        page = button.click();
        assertTrue(page.asText().contains("source id: postAddTester"));
    }
}
