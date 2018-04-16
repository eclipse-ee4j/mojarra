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
 * $Id: MockPropertyResolver.java,v 1.2 2006/03/16 19:41:24 edburns Exp $
 */



package com.sun.faces.mock;


import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.PropertyResolver;
import org.apache.commons.beanutils.PropertyUtils;


/**
 * <p>Mock implementation of {@link PropertyResolver} that supports a limited
 * subset of expression evaluation functionality:</p>
 * <ul>
 * <li>Supports <code>getValue()</code> and <code>setValue()</code> methods
 *     that take a String second argument.</li>
 * <li>Supports property getting and setting as provided by
 *     <code>PropertyUtils.getSimpleProperty()</code> and
 *     <code>PropertyUtils.setSimpleProperty()</code>.</li>
 * </ul>
 */

public class MockPropertyResolver extends PropertyResolver {


    // ------------------------------------------------------------ Constructors


    // ------------------------------------------------ PropertyResolver Methods


    public Object getValue(Object base, Object property)
        throws EvaluationException, PropertyNotFoundException {

        if (base == null) {
            throw new NullPointerException();
        }
	String name = property.toString();
        try {
            if (base instanceof Map) {
                Map map = (Map) base;
                if (map.containsKey(name)) {
                    return (map.get(name));
                } else {
                    throw new PropertyNotFoundException(name);
                }
            } else {
                return (PropertyUtils.getSimpleProperty(base, name));
            }
        } catch (IllegalAccessException e) {
            throw new EvaluationException(e);
        } catch (InvocationTargetException e) {
            throw new EvaluationException(e.getTargetException());
        } catch (NoSuchMethodException e) {
            throw new PropertyNotFoundException(name);
        }

    }


    public Object getValue(Object base, int index)
        throws PropertyNotFoundException {

        throw new UnsupportedOperationException();

    }


    public void setValue(Object base, Object property, Object value)
        throws PropertyNotFoundException {

        if (base == null) {
            throw new NullPointerException();
        }
	String name = property.toString();
        try {
            if (base instanceof Map) {
                ((Map) base).put(name, value);
            } else {
                PropertyUtils.setSimpleProperty(base, name, value);
            }
        } catch (IllegalAccessException e) {
            throw new EvaluationException(e);
        } catch (InvocationTargetException e) {
            throw new EvaluationException(e.getTargetException());
        } catch (NoSuchMethodException e) {
            throw new PropertyNotFoundException(name);
        }

    }


    public void setValue(Object base, int index, Object value)
        throws PropertyNotFoundException {

        throw new UnsupportedOperationException();

    }


    public boolean isReadOnly(Object base, Object property)
        throws PropertyNotFoundException {

        throw new UnsupportedOperationException();

    }


    public boolean isReadOnly(Object base, int index)
        throws PropertyNotFoundException {

        throw new UnsupportedOperationException();

    }


    public Class getType(Object base, Object property)
        throws PropertyNotFoundException {

        throw new UnsupportedOperationException();

    }


    public Class getType(Object base, int index)
        throws PropertyNotFoundException {

        throw new UnsupportedOperationException();

    }




}
