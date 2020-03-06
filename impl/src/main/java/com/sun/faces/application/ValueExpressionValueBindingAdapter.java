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

package com.sun.faces.application;

import static com.sun.faces.util.Util.isAnyNull;
import static com.sun.faces.util.Util.loadClass2;
import static com.sun.faces.util.Util.newInstance;

import java.io.Serializable;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ValueExpression;

import jakarta.faces.component.StateHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.ValueBinding;

/**
 * <p>
 * Wrap a ValueBinding instance and expose it as a ValueExpression.
 * </p>
 */

public class ValueExpressionValueBindingAdapter extends ValueExpression implements Serializable, StateHolder {

    private static final long serialVersionUID = 2990621816592041196L;

    public ValueExpressionValueBindingAdapter() {
    }

    private ValueBinding binding = null;

    public ValueExpressionValueBindingAdapter(ValueBinding binding) {
        assert (null != binding);
        this.binding = binding;
    }

    //
    // Methods from ValueExpression
    //

    @Override
    public Object getValue(ELContext context) throws ELException {
        assert (null != binding);
        if (context == null) {
            throw new NullPointerException("ELContext -> null");
        }
        Object result = null;
        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        assert (null != facesContext);
        try {
            result = binding.getValue(facesContext);
        } catch (Throwable e) {
            throw new ELException(e);
        }
        return result;
    }

    @Override
    public void setValue(ELContext context, Object value) throws ELException {
        assert (null != binding);
        if (context == null) {
            throw new NullPointerException("ELContext -> null");
        }
        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        assert (null != facesContext);
        try {
            binding.setValue(facesContext, value);
        } catch (Throwable e) {
            throw new ELException(e);
        }
    }

    @Override
    public boolean isReadOnly(ELContext context) throws ELException {
        assert (null != binding);
        if (context == null) {
            throw new NullPointerException("ELContext -> null");
        }
        boolean result = false;
        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        assert (null != facesContext);
        try {
            result = binding.isReadOnly(facesContext);
        } catch (Throwable e) {
            throw new ELException(e);
        }
        return result;
    }

    @Override
    public Class<?> getType(ELContext context) throws ELException {
        assert (null != binding);
        if (context == null) {
            throw new NullPointerException("ELContext -> null");
        }
        Class result = null;
        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        assert (null != facesContext);
        try {
            result = binding.getType(facesContext);
        } catch (Throwable e) {
            throw new ELException(e);
        }
        return result;
    }

    /**
     * <p>
     * Always return <code>false</code> since we can't possibly know if this is a literal text binding or not.
     * </p>
     */

    @Override
    public boolean isLiteralText() {
        return false;
    }

    @Override
    public Class<?> getExpectedType() {
        assert (null != binding);
        Class result = null;
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            Object value = binding.getValue(context);
            result = value.getClass();
        } catch (Throwable e) {
            result = null;
        }
        return result;
    }

    @Override
    public String getExpressionString() {
        assert (null != binding);
        return binding.getExpressionString();

    }

    @Override
    public boolean equals(Object other) {

        if (other == this) {
            return true;
        }

        if (other instanceof ValueExpressionValueBindingAdapter) {
            ValueBinding vb = ((ValueExpressionValueBindingAdapter) other).getWrapped();
            return (binding.equals(vb));
        } else if (other instanceof ValueExpression) {
            FacesContext context = FacesContext.getCurrentInstance();
            ValueExpression otherVE = (ValueExpression) other;
            Class type = binding.getType(context);
            if (type != null) {
                return type.equals(otherVE.getType(context.getELContext()));
            }
        }
        return false;

    }

    @Override
    public int hashCode() {
        assert (null != binding);

        return binding.hashCode();
    }

    public String getDelimiterSyntax() {
        // PENDING (visvan) Implementation
        return "";
    }

    //
    // Methods from StateHolder
    //

    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        Object result = null;
        if (!tranzient) {
            if (binding instanceof StateHolder) {
                Object[] stateStruct = new Object[2];

                // save the actual state of our wrapped binding
                stateStruct[0] = ((StateHolder) binding).saveState(context);
                // save the class name of the binding impl
                stateStruct[1] = binding.getClass().getName();

                result = stateStruct;
            } else {
                result = binding;
            }
        }

        return result;
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (state == null) {
            return;
        }

        if (!(state instanceof ValueBinding)) {
            Object[] stateStruct = (Object[]) state;
            Object savedState = stateStruct[0];
            String className = stateStruct[1].toString();
            ValueBinding result = null;

            if (className != null) {
                Class<?> toRestoreClass = loadClass2(className, this);

                if (toRestoreClass != null) {
                    result = newInstance(toRestoreClass);
                }

                if (!isAnyNull(result, savedState)) {
                    // don't need to check transient, since that was
                    // done on the saving side.
                    ((StateHolder) result).restoreState(context, savedState);
                }
                binding = result;
            }
        } else {
            binding = (ValueBinding) state;
        }
    }

    private boolean tranzient = false;

    @Override
    public boolean isTransient() {
        return tranzient;
    }

    @Override
    public void setTransient(boolean newTransientValue) {
        tranzient = newTransientValue;
    }

    //
    // methods used by classes aware of this class's wrapper nature
    //

    public ValueBinding getWrapped() {
        return binding;
    }

}
