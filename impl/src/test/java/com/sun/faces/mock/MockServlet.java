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

import java.io.IOException;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

/**
 * <p>
 * Mock <strong>Servlet</strong> for unit tests.</p>
 */
public class MockServlet implements Servlet {

    public MockServlet() {
    }

    public MockServlet(ServletConfig config) throws ServletException {
        init(config);
    }

    private ServletConfig config;

    @Override
    public void destroy() {
    }

    @Override
    public ServletConfig getServletConfig() {
        return (this.config);
    }

    @Override
    public String getServletInfo() {
        return ("MockServlet");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
    }

    @Override
    public void service(ServletRequest request, ServletResponse response)
            throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }
}
