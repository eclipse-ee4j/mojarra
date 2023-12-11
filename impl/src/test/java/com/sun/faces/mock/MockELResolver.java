/*
 * Copyright (c) 2023 Contributors to Eclipse Foundation.
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

import java.beans.FeatureDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * Mock implementation of <code>ELResolver</code> that supports a limited subset of expression evaluation functionality:
 * </p>
 *
 * <ul>
 * <li>Recognizes <code>applicationScope</code>, <code>requestScope</code>, and <code>sessionScope</code> implicit
 * names.</li>
 * <li>Searches in ascending scopes for non-reserved names.</li>
 * </ul>
 */
public class MockELResolver extends ELResolver {

    // ------------------------------------------------------------ Constructors

    @Override
    public Object getValue(ELContext context, Object base, Object property) throws ELException {
        Object result = null;
        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);

        if (null == base) {
            String name = (null != property) ? property.toString() : null;
            try {
                result = resolveVariable(facesContext, name);
            } catch (Throwable e) {
                throw new ELException(e);
            }
        } else {
            try {
                result = getValue(base, property);
            } catch (Throwable e) {
                throw new ELException(e);
            }
        }
        context.setPropertyResolved(result != null);
        return result;
    }

    public Object resolveVariable(FacesContext context, String name) {

        if ((context == null) || (name == null)) {
            throw new NullPointerException();
        }

        // Handle predefined variables
        if ("applicationScope".equals(name)) {
            return (econtext().getApplicationMap());
        } else if ("requestScope".equals(name)) {
            return (econtext().getRequestMap());
        } else if ("sessionScope".equals(name)) {
            return (econtext().getSessionMap());
        }

        // Look up in ascending scopes
        Map<String, Object> map = null;
        map = econtext().getRequestMap();
        if (map.containsKey(name)) {
            return (map.get(name));
        }
        map = econtext().getSessionMap();
        if ((map != null) && (map.containsKey(name))) {
            return (map.get(name));
        }
        map = econtext().getApplicationMap();
        if (map.containsKey(name)) {
            return (map.get(name));
        }

        // Requested object is not found
        return (null);

    }

    public Object getValue(Object base, Object property) {

        if (base == null) {
            throw new NullPointerException();
        }
        String name = property.toString();
        try {
            if (base instanceof Map) {
                Map<?, ?> map = (Map<?, ?>) base;
                if (map.containsKey(name)) {
                    return (map.get(name));
                } else {
                    throw new IllegalStateException(name);
                }
            } else {
                return (PropertyUtils.getSimpleProperty(base, name));
            }
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getTargetException());
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(name);
        }

    }

    private ExternalContext econtext() {

        return (FacesContext.getCurrentInstance().getExternalContext());

    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) throws ELException {
        return null;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) throws ELException {
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) throws ELException {
        boolean result = false;
        return result;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        return null;
    }
}
