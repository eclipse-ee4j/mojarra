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
import java.util.HashMap;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpSession;

// Mock Object for HttpSession (Version 2.3)
public class MockHttpSession implements HttpSession {

    public MockHttpSession() {
        super();
    }

    public MockHttpSession(ServletContext servletContext) {
        super();
        setServletContext(servletContext);
    }

    protected HashMap<String, Object> attributes = new HashMap<>();
    protected ServletContext servletContext = null;

    // --------------------------------------------------------- Public Methods
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    // ---------------------------------------------------- HttpSession Methods
    @Override
    public Object getAttribute(String name) {
        return (attributes.get(name));
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return (new MockEnumeration<String>(attributes.keySet().iterator()));
    }

    @Override
    public long getCreationTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getLastAccessedTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getMaxInactiveInterval() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletContext getServletContext() {
        return (this.servletContext);
    }

    @Override
    public void invalidate() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNew() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    @Override
    public void setAttribute(String name, Object value) {
        if (value == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, value);
        }
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        throw new UnsupportedOperationException();
    }
}
