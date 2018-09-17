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
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.util.List;

/**
 * <p>
 * Make sure that only unique view ids are saved in the session
 * </p>
 */

public class ViewRootPhaseListenerITCase extends HtmlUnitFacesITCase {

    // ------------------------------------------------------------ Constructors

    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public ViewRootPhaseListenerITCase(String name) {
        super(name);
    }

    // ------------------------------------------------------ Instance Variables

    // ---------------------------------------------------- Overall Test Methods

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
        return (new TestSuite(ViewRootPhaseListenerITCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        super.tearDown();
    }

    // ------------------------------------------------------ Instance Variables

    // ------------------------------------------------- Individual Test Methods

    public void testListenerTagListenersType() throws Exception {
        HtmlPage page = getPage("/faces/listenerTagListenersType.jsp");
        doTestListenerTagListeners(page);
    }

    public void testListenerTagListenersBinding() throws Exception {
        HtmlPage page = getPage("/faces/listenerTagListenersBinding.jsp");
        doTestListenerTagListeners(page);
    }

    public void testListenerTagListenersBindingType() throws Exception {
        HtmlPage page = getPage("/faces/listenerTagListenersBindingType.jsp");
        doTestListenerTagListeners(page);
    }

    public void doTestListenerTagListeners(HtmlPage page) throws Exception {

        assertTrue(-1 != page.asText().indexOf("beforePhaseEvent: beforePhase: RENDER_RESPONSE 6."));
        assertTrue(-1 != page.asText().indexOf("afterPhaseEvent: ."));

        List list;

        HtmlSubmitInput button = null;
        list = getAllElementsOfGivenClass(page, null, HtmlSubmitInput.class);
        button = (HtmlSubmitInput) list.get(0);
        page = (HtmlPage) button.click();

        assertTrue(-1 != page.asText().indexOf(
                "beforePhaseEvent: beforePhase: APPLY_REQUEST_VALUES 2 beforePhase: PROCESS_VALIDATIONS 3 beforePhase: UPDATE_MODEL_VALUES 4 beforePhase: INVOKE_APPLICATION 5 beforePhase: RENDER_RESPONSE 6."));
        assertTrue(-1 != page.asText().indexOf(
                "afterPhaseEvent: afterPhase: APPLY_REQUEST_VALUES 2 afterPhase: PROCESS_VALIDATIONS 3 afterPhase: UPDATE_MODEL_VALUES 4 afterPhase: INVOKE_APPLICATION 5."));

    }

}
