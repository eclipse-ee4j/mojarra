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

/*
 * CatchExceptionServlet.java
 *
 * Created on March 19, 2007, 2:14 PM
 */

package com.sun.faces.test.servlet30.neverunwrapexceptions;

import java.io.IOException;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.faces.webapp.FacesServlet;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;


/**
 *
 * @author edburns
 * @version
 */
public class CatchExceptionServlet implements Servlet {
    
    private FacesServlet wrapped = null;
    
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        try {
            wrapped.service(servletRequest, servletResponse);
            
        }
        catch (ServletException e) {
            HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
            httpRequest.setAttribute("exceptionClass", e.getClass().getName());
            httpRequest.setAttribute("rootCause", e.getCause().getClass().getName());
            httpRequest.setAttribute("exceptionMessage", e.getMessage());
            throw e;
        }
    }

    public void init(ServletConfig servletConfig) throws ServletException {
        if (null == wrapped) {
            wrapped = new FacesServlet();
        }
        wrapped.init(servletConfig);
    }

    public void destroy() {
        wrapped.destroy();
    }

    public String getServletInfo() {
        return ((wrapped != null) ? wrapped.getServletInfo() : null);
    }

    public ServletConfig getServletConfig() {
        return ((wrapped != null) ? wrapped.getServletConfig() : null);
    }
    

}
