/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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
package com.sun.faces.webapp;

import java.util.HashMap;

import jakarta.faces.FacesException;
import jakarta.faces.webapp.FacesServletFactory;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;

/**
 * The implementation of the FacesServletFactory.
 *
 * @author Manfred Riem
 */
public class FacesServletFactoryImpl extends FacesServletFactory {

    /**
     * Stores the map of FacesServlet instances keyed by ServletConfig.
     */
    private HashMap<ServletConfig, Servlet> servlets = new HashMap<>();

    @Override
    public Servlet getFacesServlet(ServletConfig config) {
        Servlet servlet = servlets.get(config);
        if (servlet == null) {
            servlet = new FacesServletImpl();
            try {
                servlet.init(config);
            } catch (ServletException e) {
                throw new FacesException(e);
            }
            servlets.put(config, servlet);
        }
        return servlet;
    }

    @Override
    public FacesServletFactory getWrapped() {
        return null; // we don't actually wrap anything
    }
}
