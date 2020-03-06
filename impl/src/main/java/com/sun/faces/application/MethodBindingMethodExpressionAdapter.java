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
import jakarta.el.PropertyNotFoundException;
import jakarta.el.ValueExpression;

import jakarta.faces.component.StateHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.EvaluationException;
import jakarta.faces.el.MethodBinding;
import jakarta.faces.el.MethodNotFoundException;

/**
 * <p>
 * Wrap a MethodExpression instance and expose it as a MethodBinding
 * </p>
 *
 */
public class MethodBindingMethodExpressionAdapter extends MethodBinding implements StateHolder, Serializable {

    private static final long serialVersionUID = 6351778415298720238L;

    private MethodExpression methodExpression = null;
    private boolean tranzient;

    public MethodBindingMethodExpressionAdapter() {
    } // for StateHolder

    public MethodBindingMethodExpressionAdapter(MethodExpression methodExpression) {
        this.methodExpression = methodExpression;
    }

    public Object invoke(FacesContext context, Object params[]) throws EvaluationException, MethodNotFoundException {
        if (context == null) {
            throw new NullPointerException("FacesConext -> null");
        }

        try {
            return methodExpression.invoke(context.getELContext(), params);
        } catch (jakarta.el.MethodNotFoundException | NullPointerException e) {
            throw new MethodNotFoundException(e);
        } catch (PropertyNotFoundException e) {
            throw new EvaluationException(e);
        } catch (ELException e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            throw new EvaluationException(cause);
        }
    }

    public Class<?> getType(FacesContext context) throws MethodNotFoundException {

        if (context == null) {
            throw new NullPointerException("FacesConext -> null");
        }

        try {
            return methodExpression.getMethodInfo(context.getELContext()).getReturnType();
        } catch (ELException e) {
            throw new MethodNotFoundException(e);
        }
    }

    public String getExpressionString() {
        return methodExpression.getExpressionString();
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other instanceof MethodBindingMethodExpressionAdapter) {
            return methodExpression.equals(((MethodBindingMethodExpressionAdapter) other).getWrapped());
        } else if (other instanceof MethodBinding) {
            MethodBinding binding = (MethodBinding) other;

            // We'll need to do a little leg work to determine
            // if the MethodBinding is equivalent to the
            // wrapped MethodExpression
            String expr = binding.getExpressionString();
            int idx = expr.indexOf('.');
            String target = expr.substring(0, idx).substring(2);
            String t = expr.substring(idx + 1);
            String method = t.substring(0, (t.length() - 1));

            FacesContext context = FacesContext.getCurrentInstance();
            ELContext elContext = context.getELContext();
            MethodInfo controlInfo = methodExpression.getMethodInfo(elContext);

            // Ensure the method names are the same
            if (!controlInfo.getName().equals(method)) {
                return false;
            }

            // Using the target, create an expression and evaluate it.
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
            Class<?> type = binding.getType(context);
            Method[] methods = result.getClass().getMethods();
            for (Method meth : methods) {
                if (meth.getName().equals(method) && type.equals(controlInfo.getReturnType())
                        && Arrays.equals(meth.getParameterTypes(), controlInfo.getParamTypes())) {
                    return true;
                }
            }
        }

        return false;
    }

    public int hashCode() {
        return methodExpression.hashCode();
    }

    public boolean isTransient() {
        return this.tranzient;
    }

    public void setTransient(boolean tranzient) {
        this.tranzient = tranzient;
    }

    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }

        Object result = null;
        if (!tranzient) {
            if (methodExpression instanceof StateHolder) {
                Object[] stateStruct = new Object[2];

                // save the actual state of our wrapped methodExpression
                stateStruct[0] = ((StateHolder) methodExpression).saveState(context);

                // save the class name of the methodExpression impl
                stateStruct[1] = methodExpression.getClass().getName();

                result = stateStruct;
            } else {
                result = methodExpression;
            }
        }

        return result;
    }

    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (state == null) {
            return;
        }

        if (!(state instanceof MethodExpression)) {
            Object[] stateStruct = (Object[]) state;
            Object savedState = stateStruct[0];
            String className = stateStruct[1].toString();
            MethodExpression result = null;

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
                methodExpression = result;
            }
        } else {
            methodExpression = (MethodExpression) state;
        }
    }

    public MethodExpression getWrapped() {
        return methodExpression;
    }

}
