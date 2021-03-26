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

package com.sun.faces.facelets.tag.composite;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

import com.sun.faces.facelets.util.ReflectionUtil;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;

/**
 * A property descriptor for a composite component attribute.
 */
public class CompositeAttributePropertyDescriptor extends PropertyDescriptor {

    public CompositeAttributePropertyDescriptor(String propertyName, Method readMethod, Method writeMethod) throws IntrospectionException {
        super(propertyName, readMethod, writeMethod);
    }

    @Override
    public Object getValue(String attributeName) {
        Object result = super.getValue(attributeName);
        if ("type".equals(attributeName) && null != result && !(result instanceof Class)) {
            FacesContext context = FacesContext.getCurrentInstance();
            ELContext elContext = context.getELContext();
            String classStr = (String) ((ValueExpression) result).getValue(elContext);
            if (null != classStr) {
                try {
                    result = ReflectionUtil.forName(classStr);

                    setValue(attributeName, result);
                } catch (ClassNotFoundException ex) {
                    classStr = "java.lang." + classStr; // NOPMD
                    boolean throwException = false;
                    try {
                        result = ReflectionUtil.forName(classStr);

                        setValue(attributeName, result);
                    } catch (ClassNotFoundException ex2) {
                        throwException = true;
                    }
                    if (throwException) {
                        String message = "Unable to obtain class for " + classStr;
                        throw new FacesException(message, ex);
                    }
                }
            }
        }
        return result;
    }
}
