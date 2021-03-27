/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.faces.test.javaee7.cdimultitenantsetstccl;

import static jakarta.faces.FactoryFinder.FACES_CONTEXT_FACTORY;
import static jakarta.faces.FactoryFinder.LIFECYCLE_FACTORY;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import jakarta.faces.FactoryFinder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.FacesContextFactory;
import jakarta.faces.lifecycle.LifecycleFactory;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class BeforeFilter implements Filter {

    private FilterConfig filterConfig;

    private static final String INIT_HAS_LIFECYCLE_KEY = "BeforeServlet_hasLifecycle";
    private static final String INIT_HAS_INITFACESCONTEXT_KEY = "BeforeServlet_hasInitFacesContext";

    private static final String REQUEST_HAS_LIFECYCLE = "BeforeServlet_requestHasLifecycle";
    private static final String REQUEST_HAS_FACESCONTEXT = "BeforeServlet_requestHasFacesContext";

    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
        ServletContext servletContext = this.filterConfig.getServletContext();
        
        LifecycleFactory lifecycle = (LifecycleFactory) FactoryFinder.getFactory(LIFECYCLE_FACTORY);
        servletContext.setAttribute(INIT_HAS_LIFECYCLE_KEY, lifecycle != null ? "TRUE" : "FALSE");
        
        FacesContext initFacesContext = FacesContext.getCurrentInstance();
        servletContext.setAttribute(INIT_HAS_INITFACESCONTEXT_KEY, initFacesContext != null ? "TRUE" : "FALSE");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        Thread thread = Thread.currentThread();
        ClassLoader originalContextClassLoader = thread.getContextClassLoader();
        ClassLoader ourCustomClassLoader = new URLClassLoader(new URL[0], originalContextClassLoader);
        thread.setContextClassLoader(ourCustomClassLoader);

        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        
        LifecycleFactory lifecycle = (LifecycleFactory) FactoryFinder.getFactory(LIFECYCLE_FACTORY);
        httpServletRequest.setAttribute(REQUEST_HAS_LIFECYCLE, lifecycle != null ? "TRUE" : "FALSE");
        
        FacesContext facesContext = FacesContext.getCurrentInstance();
        httpServletRequest.setAttribute(REQUEST_HAS_FACESCONTEXT, facesContext != null ? "TRUE" : "FALSE");

        boolean cdiIsTCCLReplacementResilient = true;

        if (cdiIsTCCLReplacementResilient) {
            try {
                chain.doFilter(request, response);
            } catch (Exception t) {
                throw new ServletException(t);
            } finally {
                thread.setContextClassLoader(originalContextClassLoader);
            }
        } else {
            FacesContextFactory facesContextFactory = (FacesContextFactory) FactoryFinder.getFactory(FACES_CONTEXT_FACTORY);
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            PrintWriter printWriter = httpServletResponse.getWriter();
            try {
                if (facesContextFactory != null) {
                    printWriter.print("<html><body><p id=\"result\">SUCCESS</p></body></html>");
                } else {
                    printWriter.print("<html><body><p id=\"result\">FAILURE</p></body></html>");
                }
                httpServletResponse.setStatus(200);
                printWriter.close();
            } catch (Exception e) {
            }
        }

    }

}
