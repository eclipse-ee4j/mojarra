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
import com.gargoylesoftware.htmlunit.html.*;

/**
 * Unit tests for Composite Components.
 */
public class CompositeComponentsWithEE6DependenciesITCase extends HtmlUnitFacesITCase {

    @SuppressWarnings({ "UnusedDeclaration" })
    public CompositeComponentsWithEE6DependenciesITCase() {
        this("CompositeComponentsTestCaseWithEE6Dependencies");
    }

    public CompositeComponentsWithEE6DependenciesITCase(String name) {
        super(name);
    }

    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(CompositeComponentsWithEE6DependenciesITCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // -------------------------------------------------------------- Test Cases

    /**
     * Added for issue 1318.
     */
    public void testIssue1318() throws Exception {

        HtmlPage page = getPage("/faces/composite/issue1318.xhtml");
        HtmlSubmitInput button = (HtmlSubmitInput) getInputContainingGivenId(page, "form:arg:n1:n2:command");
        assertNotNull(button);
        page = button.click();
        String message = "Action invoked: form:arg:n1:n2:command, arg1: Hello, arg2: World!";
        assertTrue(page.asText().contains(message));

    }

    public void testCompositeComponentActionWithArgs() throws Exception {

        HtmlPage page = getPage("/faces/composite/compActionWithArgs.xhtml");
        HtmlSubmitInput button = (HtmlSubmitInput) getInputContainingGivenId(page, "n:form:command");
        assertNotNull(button);
        page = button.click();
        String message = "Custom action invoked: c:n:form:command, arg1: arg1, arg2: arg2";
        assertTrue(page.asText().contains(message));

    }

    public void testCompositeComponentAttributeWithArgs() throws Exception {

        HtmlPage page = getPage("/faces/composite/compAttributeWithArgs.xhtml");
        String message = "arg: arg1";
        assertTrue(page.asText().contains(message));

    }

    public void testCompositeComponentAttributeRequired() throws Exception {

        HtmlPage page = getPage("/faces/composite/compAttributeRequired.xhtml");
        String message = "xx1:0xx";
        assertTrue(page.asText().contains(message));

        page = getPage("/faces/composite/compAttributeRequiredNullValue.xhtml");
        message = "xx:0xx";
        assertTrue(page.asText().contains(message));

        page = getPage("/faces/composite/compAttributeRequiredLiteral.xhtml");
        message = "xx2:0xx";
        assertTrue(page.asText().contains(message));
    }

    public void testInvalidArgsToCCExpression() throws Exception {

        client.getOptions().setThrowExceptionOnFailingStatusCode(false);
        HtmlPage page = getPage("/faces/composite/invalidMeArgs.xhtml");
        assertTrue(page.asText().contains(
                "value=\"#{cc.attrs.custom(cc.attrs.arg1)}\" Illegal attempt to pass arguments to a composite component lookup expression"));

        page = getPage("/faces/composite/invalidVeArgs.xhtml");
        assertTrue(page.asText().contains(
                "value=\"#{cc.attrs.bean(cc.attrs.arg1)}\" Illegal attempt to pass arguments to a composite component lookup expression"));
    }

}
