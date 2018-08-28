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

package com.sun.faces.test.servlet30.multitenantusesfactoryfinder;

import java.io.IOException;
import javax.faces.FactoryFinder;
import javax.faces.context.FacesContext;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BeforeServlet extends HttpServlet {

    private static final long serialVersionUID = 2759083267171493482L;

    private static final String INIT_HAS_LIFECYCLE_KEY = "BeforeServlet_hasLifecycle";
    private static final String INIT_HAS_INITFACESCONTEXT_KEY = "BeforeServlet_hasInitFacesContext";

    private static final String REQUEST_HAS_LIFECYCLE = "BeforeServlet_requestHasLifecycle";
    private static final String REQUEST_HAS_FACESCONTEXT = "BeforeServlet_requestHasFacesContext";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        LifecycleFactory lifecycle = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        ServletContext sc = config.getServletContext();
        sc.setAttribute(INIT_HAS_LIFECYCLE_KEY, (null != lifecycle) ? "TRUE" : "FALSE");
        FacesContext initFacesContext = FacesContext.getCurrentInstance();
        sc.setAttribute(INIT_HAS_INITFACESCONTEXT_KEY, (null != initFacesContext) ? "TRUE" : "FALSE");
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        LifecycleFactory lifecycle = (LifecycleFactory) FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
        req.setAttribute(REQUEST_HAS_LIFECYCLE, (null != lifecycle) ? "TRUE" : "FALSE");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        req.setAttribute(REQUEST_HAS_FACESCONTEXT, (null != facesContext) ? "TRUE" : "FALSE");

        getServletContext().getRequestDispatcher("/faces/index.xhtml").forward(req, resp);
    }
}
