/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * $Id: MockServletConfig.java,v 1.1 2005/10/18 17:48:03 edburns Exp $
 */



package com.sun.faces.mock;


import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;


// Mock Object for ServletConfig (version 2.3)
public class MockServletConfig implements ServletConfig {


    public MockServletConfig() {
    }


    public MockServletConfig(ServletContext context) {
        setServletContext(context);
    }


    private Hashtable parameters = new Hashtable();
    private ServletContext context;


    // --------------------------------------------------------- Public Methods


    public void addInitParameter(String name, String value) {
        parameters.put(name, value);
    }


    public void setServletContext(ServletContext context) {
        this.context = context;
    }


    // -------------------------------------------------- ServletConfig Methods


    public String getInitParameter(String name) {
        return ((String) parameters.get(name));
    }


    public Enumeration getInitParameterNames() {
        return (parameters.keys());
    }


    public ServletContext getServletContext() {
        return (this.context);
    }


    public String getServletName() {
        return ("MockServlet");
    }


}
