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

package jakarta.faces.validator;

import com.sun.faces.junit.JUnitFacesTestCaseBase;
import com.sun.faces.mock.MockRenderKit;

import jakarta.faces.FactoryFinder;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * <p>
 * Base unit tests for all {@link Validator} implementations.</p>
 */
public class ValidatorTestCase extends JUnitFacesTestCaseBase {

    // ------------------------------------------------------------ Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public ValidatorTestCase(String name) {
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
        return (new TestSuite(ValidatorTestCase.class));
    }

    public void testNoOp() {
    }
}
