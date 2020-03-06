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
import java.lang.reflect.Method;
import java.util.Arrays;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ExpressionFactory;
import jakarta.el.MethodExpression;
import jakarta.el.MethodInfo;
import jakarta.el.ValueExpression;
import jakarta.faces.component.StateHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.MethodBinding;

/**
 * <p>
 * Wrap a MethodBinding instance and expose it as a MethodExpression.
 * </p>
 */

public class MethodExpressionMethodBindingAdapter extends MethodExpression implements Serializable, StateHolder {

    private static final long serialVersionUID = 5639227653537610567L;

    public MethodExpressionMethodBindingAdapter() {
    } // for StateHolder

    private MethodBinding binding = null;

    public MethodExpressionMethodBindingAdapter(MethodBinding binding) {
        assert null != binding;
        this.binding = binding;
    }

    //
    // Methods from MethodExpression
    //

    private transient MethodInfo info = null;

    @Override
    public MethodInfo getMethodInfo(ELContext context) throws ELException {
        assert null != binding;

        if (context == null) {
            throw new NullPointerException("ELContext -> null");
        }

        if (null == info) {
            FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
            if (null != facesContext) {
                try {
                    // PENDING - we should find a way to provide more information
                    info = new MethodInfo(null, binding.getType(facesContext), null);
                } catch (Exception e) {
                    throw new ELException(e);
                }
            }
        }

        return info;
    }

    @Override
    public Object invoke(ELContext context, Object[] params) throws ELException {
        assert null != binding;

        if (context == null) {
            throw new NullPointerException("ELContext -> null");
        }

        Object result = null;
        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        if (null != facesContext) {
            try {
                result = binding.invoke(facesContext, params);
            } catch (Exception e) {
                throw new ELException(e);
            }
        }
        return result;
    }

    @Override
    public String getExpressionString() {
        assert null != binding;
        return binding.getExpressionString();

    }

    @Override
    public boolean isLiteralText() {
        assert binding != null;
        String expr = binding.getExpressionString();
        return !(expr.startsWith("#{") && expr.endsWith("}"));
    }

    @Override
    public boolean equals(Object other) {

        if (other == this) {
            return true;
        }

        if (other instanceof MethodExpressionMethodBindingAdapter) {
            MethodBinding ob = ((MethodExpressionMethodBindingAdapter) other).getWrapped();
            return binding.equals(ob);
        } else if (other instanceof MethodExpression) {
            MethodExpression expression = (MethodExpression) other;

            // We'll need to do a little leg work to determine
            // if the MethodBinding is equivalent to the
            // wrapped MethodExpression
            String expr = binding.getExpressionString();
            int idx = expr.indexOf('.');
            String target = expr.substring(0, idx).substring(2);
            String t = expr.substring(idx + 1);
            String method = t.substring(0, t.length() - 1);

            FacesContext context = FacesContext.getCurrentInstance();
            ELContext elContext = context.getELContext();
            MethodInfo controlInfo = expression.getMethodInfo(elContext);

            // ensure the method names are the same
            if (!controlInfo.getName().equals(method)) {
                return false;
            }

            // Using the target, create an expression and evaluate
            // it.
            ExpressionFactory factory = context.getApplication().getExpressionFactory();
            ValueExpression ve = factory.createValueExpression(elContext, "#{" + target + '}', Object.class);
            if (ve == null) {
                return false;
            }

            Object result = ve.getValue(elContext);

            if (result == null) {
                return false;
            }

            // Get all of the methods with the matching name and try
            // to find a match based on controlInfo's return and parameter
            // types
            Method[] methods = result.getClass().getMethods();
            for (Method meth : methods) {
                if (meth.getName().equals(method) && meth.getReturnType().equals(controlInfo.getReturnType())
                        && Arrays.equals(meth.getParameterTypes(), controlInfo.getParamTypes())) {
                    return true;
                }
            }
        }
        return false;

    }

    @Override
    public int hashCode() {
        assert null != binding;

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

        if (null == state) {
            return;
        }

        if (!(state instanceof MethodBinding)) {
            Object[] stateStruct = (Object[]) state;
            Object savedState = stateStruct[0];
            String className = stateStruct[1].toString();
            MethodBinding result = null;

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
            binding = (MethodBinding) state;
        }
    }

    private boolean tranzient = false;

    @Override
    public boolean isTransient() {
        return tranzient;
    }

    @Override
    public void setTransient(boolean newTransientMethod) {
        tranzient = newTransientMethod;
    }

    //
    // methods used by classes aware of this class's wrapper nature
    //

    public MethodBinding getWrapped() {
        return binding;
    }

}
