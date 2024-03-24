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

package com.sun.faces.mock;

import jakarta.faces.FactoryFinder;
import jakarta.faces.webapp.FacesServletFactory;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;

import com.sun.faces.webapp.FacesServletImpl;

public class MockFacesServletFactory extends FacesServletFactory {
    public MockFacesServletFactory() {
        System.setProperty(FactoryFinder.FACES_SERVLET_FACTORY, this.getClass().getName());
    }

    @Override
    public Servlet getFacesServlet(ServletConfig config) {
        Servlet facesServlet = new FacesServletImpl();
        try {
            facesServlet.init(config);
        } catch (ServletException e) {
            throw new IllegalStateException(e);
        }
        return facesServlet;
    }

    @Override
    public FacesServletFactory getWrapped() {
        return null;
    }
}
