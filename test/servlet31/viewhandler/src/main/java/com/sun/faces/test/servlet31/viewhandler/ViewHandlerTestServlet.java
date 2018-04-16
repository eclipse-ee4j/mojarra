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

package com.sun.faces.test.servlet31.viewhandler;

import static com.sun.faces.util.RequestStateManager.INVOCATION_PATH;
import static javax.faces.FactoryFinder.LIFECYCLE_FACTORY;
import static javax.faces.lifecycle.LifecycleFactory.DEFAULT_LIFECYCLE;

import java.io.IOException;
import java.util.Objects;

import javax.faces.FactoryFinder;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.sun.faces.application.ViewHandlerImpl;
import com.sun.faces.context.ExternalContextImpl;
import com.sun.faces.context.FacesContextImpl;
import com.sun.faces.util.RequestStateManager;

/**
 * This servlet does a kind of in-container unit tests for {@link ViewHandlerImpl} (which is deprecated
 * and not used by Mojarra).
 * <p>
 * These tests have been migrated from cactus based tests and are mostly Mojarra specific.
 * Some of these should be migrated to regular univeral JSF tests.
 * 
 */
@WebServlet("/testViewHandler")
public class ViewHandlerTestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        LifecycleFactory factory = (LifecycleFactory) FactoryFinder.getFactory(LIFECYCLE_FACTORY);
        Lifecycle lifecycle = factory.getLifecycle(DEFAULT_LIFECYCLE);

        // Wrap the request so we can control the return value of getServletPath
        TestRequest testRequest = new TestRequest(request);

        ExternalContext extContext = new ExternalContextImpl(request.getServletContext(), testRequest, response);
        FacesContext facesContext = new FacesContextImpl(extContext, lifecycle);
        String contextPath = request.getContextPath();

        // NOTE: ViewHandlerImpl is deprecated and not used by Mojarra. It's not entirely
        // clear why this still needs to be tested.
        ViewHandlerImpl handler = new ViewHandlerImpl();

        // If getServletPath() returns a path prefix, then the viewId path
        // returned must have that path prefixed.
        testRequest.setServletPath("/faces");
        testRequest.setPathInfo("/path/test.jsp");
        RequestStateManager.remove(facesContext, INVOCATION_PATH);
        String path = handler.getActionURL(facesContext, "/path/test.jsp");
        System.out.println("VIEW ID PATH 2: " + path);
        String expected = contextPath + "/faces/path/test.jsp";

        response.getWriter().println(Objects.equals(expected, path));
        
        
        // If getServletPath() returns a path indicating extension mapping
        // and the viewId passed has no extension, append the extension
        // to the provided viewId
        testRequest.setServletPath("/path/firstRequest.jsf");
        testRequest.setPathInfo(null);
        RequestStateManager.remove(facesContext, INVOCATION_PATH);
        path = handler.getActionURL(facesContext, "/path/test");
        System.out.println("VIEW ID PATH 3: " + path);
        expected = contextPath + "/path/test";
        
        response.getWriter().println(Objects.equals(expected, path));
        
        
        // If getServletPath() returns a path indicating extension mapping
        // and the viewId passed has an extension, replace the extension with
        // the extension defined in the deployment descriptor
        testRequest.setServletPath("/path/firstRequest.jsf");
        testRequest.setPathInfo(null);
        RequestStateManager.remove(facesContext, INVOCATION_PATH);
        path = handler.getActionURL(facesContext, "/path/t.est.jsp");
        System.out.println("VIEW ID PATH 4: " + path);
        expected = contextPath + "/path/t.est.jsf";
        
        response.getWriter().println(Objects.equals(expected, path));
        
        
        // If path info is null, the impl must check to see if
        // there is an exact match on the servlet path, if so, return
        // the servlet path
        testRequest.setServletPath("/faces");
        testRequest.setPathInfo(null);
        RequestStateManager.remove(facesContext, INVOCATION_PATH);
        path = handler.getActionURL(facesContext, "/path/t.est");
        System.out.println("VIEW ID PATH 5: " + path);
        System.out.println("End of action test " + path);
        expected = contextPath + "/faces/path/t.est";
        
        response.getWriter().println(Objects.equals(expected, path));
    }
    
    private class TestRequest extends HttpServletRequestWrapper {

        private String servletPath;
        private String pathInfo;

        public TestRequest(HttpServletRequest request) {
            super(request);
        }

        public String getServletPath() {
            return servletPath;
        }

        public void setServletPath(String servletPath) {
            this.servletPath = servletPath;
        }

        public String getPathInfo() {
            return pathInfo;
        }

        public void setPathInfo(String pathInfo) {
            this.pathInfo = pathInfo;
        }
    }

}
