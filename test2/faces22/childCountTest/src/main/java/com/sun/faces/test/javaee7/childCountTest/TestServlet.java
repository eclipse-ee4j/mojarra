/*
 * Copyright (c) 2009, 2021 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id$
 */

package com.sun.faces.test.javaee7.childCountTest;

import static jakarta.faces.FactoryFinder.FACES_CONTEXT_FACTORY;
import static jakarta.faces.FactoryFinder.LIFECYCLE_FACTORY;
import static jakarta.faces.FactoryFinder.getFactory;
import static jakarta.faces.lifecycle.LifecycleFactory.DEFAULT_LIFECYCLE;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jakarta.faces.application.Application;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIComponentBase;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.FacesContextFactory;
import jakarta.faces.lifecycle.LifecycleFactory;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/childCountTest")
public final class TestServlet extends HttpServlet {

    private static final long serialVersionUID = 7048423291731606016L;
    private static final String FAIL = "Test FAILED";
    private static final String PASS = "Test PASSED";

    /**
     * The {@link FacesContext} object for this request.
     */
    private FacesContext facesContext;

    /**
     * The environment for this web application.
     */
    private ServletContext context;

    /**
     * The {@link Application} object for this context.
     */
    private Application application;

    /**
     * <p>
     * Initializes this {@link jakarta.servlet.Servlet}.
     * </p>
     * 
     * @param config this Servlet's configuration
     * @throws ServletException if an error occurs
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        context = config.getServletContext();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        initFaces(context, request, response);

        try {
            uiComponentGetChildCountTest(request, response);
        } catch (Exception e) {
            response.getWriter().write("Error executing test: " + "uiComponentGetChildCountTest\n\n");
            
            e.printStackTrace(response.getWriter());
        } finally {
            if (FacesContext.getCurrentInstance() != null) {
                facesContext.release();
            }
        }

    }

    // ------------------------------------------- Test Methods ----

    //UIComponent.getChildCount()
    public void uiComponentGetChildCountTest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        UIComponent component = createComponent();

        if (component.getChildCount() != 0) {
            out.println(FAIL + " getChildCount() returned a value greater than zero for a newly created component.");
            out.println("Value returned: " + component.getChildCount());
            return;
        }

        UIComponent child1 = new UIInput();
        UIComponent child2 = new UIForm();
        UIComponent child2_1 = new UIInput();
        UIComponent child1_1 = new UIOutput();
        child1.setId("child1");
        child2.setId("child2");
        child2_1.setId("child2_1");
        child1_1.setId("child1_1");
        child2.getChildren().add(child2_1);
        child1.getChildren().add(child1_1);

        List<UIComponent> children = component.getChildren();

        children.add(child1);
        children.add(child2);

        // children count should be 2
        if (component.getChildCount() != 2) {
            out.println(FAIL + " Expected getChildCount() to return 2.");
            out.println("Child count received: " + component.getChildCount());
            return;
        }

        children.remove(child1);
        if (component.getChildCount() != 1) {
            out.println(FAIL + " Expected getChildCount() to return 1.");
            out.println("Child count received: " + component.getChildCount());
            return;
        }

        child2.getChildren().remove(child2_1);
        if (component.getChildCount() != 1) {
            out.println(FAIL + " Expected getChildCount() to return 1.");
            out.println("Child count received: " + component.getChildCount());
            return;
        }

        children.remove(child2);
        if (component.getChildCount() != 0) {
            out.println(FAIL + " Expected getChildCount() to return 0.");
            out.println("Child count received: " + component.getChildCount());
            return;
        }

        out.println(PASS);
    }

    /**
     * <p>
     * Creates a new {@link UIComponent} instance.
     * </p>
     * 
     * @return a new {@link UIComponent} instance.
     */
    protected UIComponentBase createComponent() {
        return new HtmlInputText();
    }

    private void initFaces(ServletContext context, ServletRequest request, ServletResponse response) {
        FacesContextFactory facesContextFactory = (FacesContextFactory) getFactory(FACES_CONTEXT_FACTORY);

        if (facesContextFactory != null) {
            LifecycleFactory lifecycleFactory = (LifecycleFactory) getFactory(LIFECYCLE_FACTORY);
            facesContext = facesContextFactory.getFacesContext(context, request, response, lifecycleFactory.getLifecycle(DEFAULT_LIFECYCLE));

            if (facesContext == null) {
                throw new IllegalStateException("Unable to obtain FacesContext instance");
            }

            // Set up references to the application and facesContext objects
            application = facesContext.getApplication();
            facesContext.setViewRoot(createViewRoot());
        } else {
            throw new IllegalStateException("Unable to obtain FacesContextFactory instance.");
        }
    }

    protected UIViewRoot createViewRoot(String viewId) {
        return application.getViewHandler().createView(facesContext, viewId);
    }

    protected UIViewRoot createViewRoot() {
        return createViewRoot("foo.xhtml");
    }
}
