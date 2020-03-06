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

package com.sun.faces.el;

import jakarta.el.ELException;

import java.lang.reflect.Array;
import java.util.List;

import com.sun.faces.util.MessageUtils;

import jakarta.faces.context.FacesContext;
import jakarta.faces.el.EvaluationException;
import jakarta.faces.el.PropertyNotFoundException;
import jakarta.faces.el.PropertyResolver;

/**
 * <p>
 * Concrete implementation of <code>PropertyResolver</code>.
 * </p>
 */
@SuppressWarnings("deprecation")
public class PropertyResolverImpl extends PropertyResolver {

    private PropertyResolver delegate;

    // ------------------------------------------- Methods from PropertyResolver

    // Specified by jakarta.faces.el.PropertyResolver.getType(Object,int)
    @Override
    public Class getType(Object base, int index) throws EvaluationException, PropertyNotFoundException {

        // validates base != null and index >= 0
        assertInput(base, index);

        if (delegate != null) {
            return delegate.getType(base, index);
        }

        Class<?> type = base.getClass();
        try {
            if (type.isArray()) {
                Array.get(base, index);
                return type.getComponentType();
            } else if (base instanceof List) {
                Object value = ((List) base).get(index);
                return value != null ? value.getClass() : null;
            } else {
                throw new PropertyNotFoundException(MessageUtils.getExceptionMessageString(MessageUtils.EL_PROPERTY_TYPE_ERROR_ID, base));
            }
        } catch (ArrayIndexOutOfBoundsException aioobe) {
            throw new PropertyNotFoundException(
                    MessageUtils.getExceptionMessageString(MessageUtils.EL_SIZE_OUT_OF_BOUNDS_ERROR_ID, base, index, Array.getLength(base)));
        } catch (IndexOutOfBoundsException ioobe) {
            throw new PropertyNotFoundException(
                    MessageUtils.getExceptionMessageString(MessageUtils.EL_SIZE_OUT_OF_BOUNDS_ERROR_ID, base, index, ((List) base).size()));
        }
    }

    // Specified by jakarta.faces.el.PropertyResolver.getType(Object,String)
    @Override
    public Class getType(Object base, Object property) {

        assertInput(base, property);

        if (delegate != null) {
            return delegate.getType(base, property);
        }

        try {
            FacesContext context = FacesContext.getCurrentInstance();
            return context.getApplication().getELResolver().getType(context.getELContext(), base, property);
        } catch (jakarta.el.PropertyNotFoundException pnfe) {
            throw new PropertyNotFoundException(pnfe);
        } catch (ELException elex) {
            throw new EvaluationException(elex);
        }
    }

    // Specified by jakarta.faces.el.PropertyResolver.getValue(Object,int)
    @Override
    public Object getValue(Object base, int index) {

        // validates base and index
        if (base == null) {
            return null;
        }

        if (delegate != null) {
            return delegate.getValue(base, index);
        }

        if (base.getClass().isArray()) {
            try {
                return Array.get(base, index);
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                return null;
            }
        } else if (base instanceof List) {
            try {
                return ((List) base).get(index);
            } catch (IndexOutOfBoundsException ioobe) {
                return null;
            }
        } else {
            throw new PropertyNotFoundException(MessageUtils.getExceptionMessageString(MessageUtils.EL_PROPERTY_TYPE_ERROR_ID, base));
        }

    }

    // Specified by jakarta.faces.el.PropertyResolver.getValue(Object,String)
    @Override
    public Object getValue(Object base, Object property) {

        if (delegate != null) {
            return delegate.getValue(base, property);
        }

        try {
            FacesContext context = FacesContext.getCurrentInstance();
            return context.getApplication().getELResolver().getValue(context.getELContext(), base, property);
        } catch (jakarta.el.PropertyNotFoundException pnfe) {
            throw new PropertyNotFoundException(pnfe);
        } catch (ELException elex) {
            throw new EvaluationException(elex);
        }
    }

    // Specified by jakarta.faces.el.PropertyResolver.isReadOnly(Object,int)
    @Override
    public boolean isReadOnly(Object base, int index) {

        // validate input
        assertInput(base, index);

        if (delegate != null) {
            return delegate.isReadOnly(base, index);
        }

        if (base instanceof List || base.getClass().isArray()) {
            return false;
        } else {
            throw new PropertyNotFoundException(MessageUtils.getExceptionMessageString(MessageUtils.EL_PROPERTY_TYPE_ERROR_ID, base));
        }
    }

    // Specified by jakarta.faces.el.PropertyResolver.isReadOnly(Object,String)
    @Override
    public boolean isReadOnly(Object base, Object property) {

        if (delegate != null) {
            return delegate.isReadOnly(base, property);
        }

        try {
            FacesContext context = FacesContext.getCurrentInstance();
            return context.getApplication().getELResolver().isReadOnly(context.getELContext(), base, property);
        } catch (ELException elex) {
            throw new EvaluationException(elex);
        }
    }

    // Specified by jakarta.faces.el.PropertyResolver.setValue(Object,int,Object)
    @Override
    public void setValue(Object base, int index, Object value) {

        // validate input
        assertInput(base, index);

        if (delegate != null) {
            delegate.setValue(base, index, value);
            return;
        }

        FacesContext context = FacesContext.getCurrentInstance();
        Class<?> type = base.getClass();
        if (type.isArray()) {
            try {
                Array.set(base, index, context.getApplication().getExpressionFactory().coerceToType(value, type.getComponentType()));
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                throw new PropertyNotFoundException(
                        MessageUtils.getExceptionMessageString(MessageUtils.EL_SIZE_OUT_OF_BOUNDS_ERROR_ID, base, index, Array.getLength(base)));
            }
        } else if (base instanceof List) {
            try {
                // Inherently not type safe, but nothing can be done about it.
                // noinspection unchecked
                ((List) base).set(index, value);
            } catch (IndexOutOfBoundsException ioobe) {
                throw new PropertyNotFoundException(
                        MessageUtils.getExceptionMessageString(MessageUtils.EL_SIZE_OUT_OF_BOUNDS_ERROR_ID, base, index, ((List) base).size()));
            }
        } else {
            throw new PropertyNotFoundException(MessageUtils.getExceptionMessageString(MessageUtils.EL_PROPERTY_TYPE_ERROR_ID, base));
        }
    }

    // Specified by
    // jakarta.faces.el.PropertyResolver.setValue(Object,String,Object)
    @Override
    public void setValue(Object base, Object property, Object value) {

        if (delegate != null) {
            delegate.setValue(base, property, value);
            return;
        }

        try {
            FacesContext context = FacesContext.getCurrentInstance();
            context.getApplication().getELResolver().setValue(context.getELContext(), base, property, value);
        } catch (jakarta.el.PropertyNotFoundException | jakarta.el.PropertyNotWritableException pnfe) {
            throw new PropertyNotFoundException(pnfe);
        } catch (ELException elex) {
            throw new EvaluationException(elex);
        }
    }

    // ---------------------------------------------------------- Public Methods

    public void setDelegate(PropertyResolver delegate) {

        this.delegate = delegate;

    }

    protected static void assertInput(Object base, Object property) throws PropertyNotFoundException {
        if (base == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "base");
            throw new PropertyNotFoundException(message);
        }
        if (property == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "property");
            throw new PropertyNotFoundException(message);
        }
    }

    protected static void assertInput(Object base, int index) throws PropertyNotFoundException {
        if (base == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "base");
            throw new PropertyNotFoundException(message);
        }
        if (index < 0) {
            throw new PropertyNotFoundException(MessageUtils.getExceptionMessageString(MessageUtils.EL_OUT_OF_BOUNDS_ERROR_ID, base, index));
        }
    }
}
