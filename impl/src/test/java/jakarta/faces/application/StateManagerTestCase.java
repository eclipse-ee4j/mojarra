/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.faces.application;

import com.sun.faces.junit.JUnitFacesTestCaseBase;
import com.sun.faces.mock.*;

import jakarta.faces.FactoryFinder;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.*;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>
 * Base unit tests for all {@link UIComponent} implementation classes.</p>
 */
public class StateManagerTestCase extends JUnitFacesTestCaseBase {

    // ------------------------------------------------------------ Constructors
    // Construct a new instance of this test case.
    public StateManagerTestCase(String name) {
        super(name);
    }

    // ---------------------------------------------------- Overall Test Methods
    // Set up instance variables required by this test case.
    @Override
    public void setUp() throws Exception {
        super.setUp();

        UIViewRoot root = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        root.setViewId("/viewId");
        facesContext.setViewRoot(root);
        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = new MockRenderKit();
        try {
            renderKitFactory.addRenderKit(RenderKitFactory.HTML_BASIC_RENDER_KIT,
                    renderKit);
        } catch (IllegalArgumentException e) {

        }

    }

    // Return the tests included in this test case.
    public static Test suite() {

        return (new TestSuite(StateManagerTestCase.class));

    }

    // ------------------------------------------------- Individual Test Methods
    public void testNoStackOverflowOnNonOverriddenStateManagerMethods() throws Exception {
        StateManager override = new StateManager() {

            @Override
            public UIViewRoot restoreView(FacesContext context, String viewId, String renderKitId) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

        };

        override.saveView(facesContext);
        override.saveSerializedView(facesContext);
    }
}
