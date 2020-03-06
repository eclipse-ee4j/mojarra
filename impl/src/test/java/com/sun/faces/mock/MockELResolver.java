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

package com.sun.faces.mock;

import java.util.Iterator;

import jakarta.el.ELResolver;

import jakarta.faces.context.FacesContext;

import jakarta.el.ELContext;
import jakarta.el.ELException;

/**
 * <p>
 * Mock implementation of <code>ELResolver</code> that supports a limited subset
 * of expression evaluation functionality:</p>
 *
 * <ul>
 * <li>Recognizes <code>applicationScope</code>, <code>requestScope</code>, and
 * <code>sessionScope</code> implicit names.</li>
 * <li>Searches in ascending scopes for non-reserved names.</li>
 * </ul>
 */
public class MockELResolver extends ELResolver {

    private MockVariableResolver variableResolver = null;
    private MockPropertyResolver propertyResolved = null;

    // ------------------------------------------------------------ Constructors
    public MockELResolver() {
        variableResolver = new MockVariableResolver();
        propertyResolved = new MockPropertyResolver();
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property)
            throws ELException {
        Object result = null;
        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);

        if (null == base) {
            String name = (null != property) ? property.toString() : null;
            try {
                result = variableResolver.resolveVariable(facesContext, name);
            } catch (Throwable e) {
                throw new ELException(e);
            }
        } else {
            try {
                result = propertyResolved.getValue(base, property);
            } catch (Throwable e) {
                throw new ELException(e);
            }
        }
        context.setPropertyResolved(result != null);
        return result;
    }

    @Override
    public Class getType(ELContext context, Object base, Object property)
            throws ELException {
        Class result = null;
        return result;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property,
            Object value) throws ELException {
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property)
            throws ELException {
        boolean result = false;
        return result;
    }

    @Override
    public Iterator getFeatureDescriptors(ELContext context, Object base) {
        return null;
    }

    @Override
    public Class getCommonPropertyType(ELContext context, Object base) {
        Class result = null;
        return result;
    }
}
