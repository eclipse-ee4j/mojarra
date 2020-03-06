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

import jakarta.el.ELException;
import jakarta.el.ValueExpression;

import jakarta.faces.component.StateHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.el.EvaluationException;
import jakarta.faces.el.PropertyNotFoundException;
import jakarta.faces.el.ValueBinding;

/**
 * <p>
 * Wrap a ValueExpression instance and expose it as a ValueBinding
 * </p>
 *
 * @author Jacob Hookom
 */
public class ValueBindingValueExpressionAdapter extends ValueBinding implements StateHolder, Serializable {

    private static final long serialVersionUID = 7410146713650507654L;

    private ValueExpression valueExpression = null;
    private boolean tranzient;

    public ValueBindingValueExpressionAdapter() {
    } // for StateHolder

    public ValueBindingValueExpressionAdapter(ValueExpression valueExpression) {
        this.valueExpression = valueExpression;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.el.ValueBinding#getExpressionString()
     */
    public String getExpressionString() {
        assert (null != valueExpression);
        return valueExpression.getExpressionString();
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.el.ValueBinding#getType(jakarta.faces.context.FacesContext)
     */
    public Class getType(FacesContext context) throws EvaluationException, PropertyNotFoundException {

        if (context == null) {
            throw new NullPointerException("FacesContext -> null");
        }
        Class result = null;
        try {
            result = valueExpression.getType(context.getELContext());
        } catch (jakarta.el.PropertyNotFoundException pnfe) {
            throw new PropertyNotFoundException(pnfe);
        } catch (ELException elex) {
            throw new EvaluationException(elex);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.el.ValueBinding#getValue(jakarta.faces.context.FacesContext)
     */
    public Object getValue(FacesContext context) throws EvaluationException, PropertyNotFoundException {
        if (context == null) {
            throw new NullPointerException("FacesContext -> null");
        }
        Object result = null;
        try {
            result = valueExpression.getValue(context.getELContext());
        } catch (jakarta.el.PropertyNotFoundException pnfe) {
            throw new PropertyNotFoundException(pnfe);
        } catch (ELException elex) {
            throw new EvaluationException(elex);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.el.ValueBinding#isReadOnly(jakarta.faces.context.FacesContext)
     */
    public boolean isReadOnly(FacesContext context) throws EvaluationException, PropertyNotFoundException {

        if (context == null) {
            throw new NullPointerException("FacesContext -> null");
        }
        boolean result = false;
        try {
            result = valueExpression.isReadOnly(context.getELContext());
        } catch (ELException elex) {
            throw new EvaluationException(elex);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see jakarta.faces.el.ValueBinding#setValue(jakarta.faces.context.FacesContext, java.lang.Object)
     */
    public void setValue(FacesContext context, Object value) throws EvaluationException, PropertyNotFoundException {

        if (context == null) {
            throw new NullPointerException("FacesContext -> null");
        }
        try {
            valueExpression.setValue(context.getELContext(), value);
        } catch (jakarta.el.PropertyNotFoundException pnfe) {
            throw new PropertyNotFoundException(pnfe);
        } catch (jakarta.el.PropertyNotWritableException pnwe) {
            throw new PropertyNotFoundException(pnwe);
        } catch (ELException elex) {
            throw new EvaluationException(elex);
        }
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
            if (valueExpression instanceof StateHolder) {
                Object[] stateStruct = new Object[2];

                // save the actual state of our wrapped valueExpression
                stateStruct[0] = ((StateHolder) valueExpression).saveState(context);
                // save the class name of the valueExpression impl
                stateStruct[1] = valueExpression.getClass().getName();

                result = stateStruct;
            } else {
                result = valueExpression;
            }
        }

        return result;

    }

    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }

        if (null == state) {
            return;
        }

        if (!(state instanceof ValueExpression)) {
            Object[] stateStruct = (Object[]) state;
            Object savedState = stateStruct[0];
            String className = stateStruct[1].toString();
            ValueExpression result = null;

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
                valueExpression = result;
            }
        } else {
            valueExpression = (ValueExpression) state;
        }
    }

    public boolean equals(Object other) {

        if (other == this) {
            return true;
        }

        if (other instanceof ValueBindingValueExpressionAdapter) {
            ValueExpression expr = ((ValueBindingValueExpressionAdapter) other).getWrapped();
            return (valueExpression.equals(expr));
        } else if (other instanceof ValueBinding) {
            FacesContext context = FacesContext.getCurrentInstance();
            ValueBinding otherVB = (ValueBinding) other;
            Class type = otherVB.getType(context);
            if (type != null) {
                return type.equals(valueExpression.getType(context.getELContext()));
            }
        }
        return false;

    }

    public int hashCode() {
        assert (null != valueExpression);
        return valueExpression.hashCode();
    }

    public ValueExpression getWrapped() {
        return valueExpression;
    }

}
