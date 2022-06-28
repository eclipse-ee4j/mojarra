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

package com.sun.faces.facelets.tag;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import jakarta.faces.view.facelets.MetadataTarget;

/**
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public class MetadataTargetImpl extends MetadataTarget {

    private final Map pd;
    private final Class<?> type;

    public MetadataTargetImpl(Class type) throws IntrospectionException {
        this.type = type;
        pd = new HashMap();
        BeanInfo info = Introspector.getBeanInfo(type);
        PropertyDescriptor[] pda = info.getPropertyDescriptors();
        for (int i = 0; i < pda.length; i++) {
            pd.put(pda[i].getName(), pda[i]);
        }
    }

    @Override
    public PropertyDescriptor getProperty(String name) {
        return (PropertyDescriptor) pd.get(name);
    }

    @Override
    public boolean isTargetInstanceOf(Class type) {
        return type.isAssignableFrom(this.type);
    }

    @Override
    public Class getTargetClass() {
        return type;
    }

    @Override
    public Class getPropertyType(String name) {
        PropertyDescriptor pd = getProperty(name);
        if (pd != null) {
            return pd.getPropertyType();
        }
        return null;
    }

    @Override
    public Method getWriteMethod(String name) {
        PropertyDescriptor pd = getProperty(name);
        if (pd != null) {
            return pd.getWriteMethod();
        }
        return null;
    }

    @Override
    public Method getReadMethod(String name) {
        PropertyDescriptor pd = getProperty(name);
        if (pd != null) {
            return pd.getReadMethod();
        }
        return null;
    }

}
