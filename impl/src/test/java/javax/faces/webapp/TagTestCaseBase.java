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

package javax.faces.webapp;

import com.sun.faces.junit.JUnitFacesTestCaseBase;
import javax.faces.FactoryFinder;
import javax.faces.component.UIViewRoot;
import javax.faces.render.RenderKit;
import javax.faces.render.RenderKitFactory;
import javax.servlet.jsp.tagext.Tag;
import com.sun.faces.mock.MockPageContext;
import com.sun.faces.mock.MockRenderKit;
import com.sun.faces.mock.MockServlet;

/**
 * <p>
 * Base unit tests for all UIComponentTag classes.</p>
 */
public class TagTestCaseBase extends JUnitFacesTestCaseBase {

    // ----------------------------------------------------- Instance Variables
    protected MockPageContext pageContext = null;
    protected MockServlet servlet = null;

    protected Tag root = null;

    // ---------------------------------------------------------- Constructors
    /**
     * Construct a new instance of this test case.
     *
     * @param name Name of the test case
     */
    public TagTestCaseBase(String name) {

        super(name);

    }

    // -------------------------------------------------- Overall Test Methods
    /**
     * Set up instance variables required by this test case.
     *
     * @throws java.lang.Exception
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Set up Servlet API Objects
        servlet = new MockServlet(config);

        // Set up JSP API Objects
        pageContext = new MockPageContext();
        pageContext.initialize(servlet, request, response, null,
                true, 1024, true);

        UIViewRoot rootComponent = facesContext.getApplication().getViewHandler().createView(facesContext, null);
        rootComponent.setViewId("/root");
        facesContext.setViewRoot(rootComponent);
        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        RenderKit renderKit = new MockRenderKit();
        try {
            renderKitFactory.addRenderKit(RenderKitFactory.HTML_BASIC_RENDER_KIT,
                    renderKit);
        } catch (IllegalArgumentException e) {
        }
    }

    /**
     * Tear down instance variables required by this test case.
     * @throws java.lang.Exception
     */
    @Override
    public void tearDown() throws Exception {
        pageContext = null;
        root = null;
        super.tearDown();
    }
}
