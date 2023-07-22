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

import java.util.Enumeration;
import java.util.Hashtable;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;

// Mock Object for ServletConfig (version 2.3)
public class MockServletConfig implements ServletConfig {

    public MockServletConfig() {
    }

    public MockServletConfig(ServletContext context) {
        setServletContext(context);
    }

    private Hashtable<String, String> parameters = new Hashtable<>();
    private ServletContext context;

    // --------------------------------------------------------- Public Methods
    public void addInitParameter(String name, String value) {
        parameters.put(name, value);
    }

    public void setServletContext(ServletContext context) {
        this.context = context;
    }

    // -------------------------------------------------- ServletConfig Methods
    @Override
    public String getInitParameter(String name) {
        return (parameters.get(name));
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return (parameters.keys());
    }

    @Override
    public ServletContext getServletContext() {
        return (this.context);
    }

    @Override
    public String getServletName() {
        return ("MockServlet");
    }
}
