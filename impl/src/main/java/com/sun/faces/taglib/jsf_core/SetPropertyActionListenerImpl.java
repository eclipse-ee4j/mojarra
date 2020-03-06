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

package com.sun.faces.taglib.jsf_core;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ValueExpression;

import jakarta.faces.component.StateHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;

import jakarta.el.ExpressionFactory;

public class SetPropertyActionListenerImpl implements ActionListener, StateHolder {

    private ValueExpression target;
    private ValueExpression source;

    // ------------------------------------------------------------ Constructors

    public SetPropertyActionListenerImpl() {
    }

    public SetPropertyActionListenerImpl(ValueExpression target, ValueExpression value) {

        this.target = target;
        source = value;

    }

    // --------------------------------------------- Methods from ActionListener

    @Override
    public void processAction(ActionEvent e) throws AbortProcessingException {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ELContext elContext = facesContext.getELContext();

        try {
            Object value = source.getValue(elContext);
            if (value != null) {
                ExpressionFactory factory = facesContext.getApplication().getExpressionFactory();
                value = factory.coerceToType(value, target.getType(elContext));
            }
            target.setValue(elContext, value);
        } catch (ELException ele) {
            throw new AbortProcessingException(ele);
        }

    }

    // ------------------------------------------------ Methods from StateHolder

    @Override
    public void setTransient(boolean trans) {
    }

    @Override
    public boolean isTransient() {

        return false;

    }

    @Override
    public Object saveState(FacesContext context) {

        if (context == null) {
            throw new NullPointerException();
        }
        Object[] state = new Object[2];
        state[0] = target;
        state[1] = source;
        return state;

    }

    @Override
    public void restoreState(FacesContext context, Object state) {

        if (context == null) {
            throw new NullPointerException();
        }
        if (state == null) {
            return;
        }
        Object[] stateArray = (Object[]) state;
        target = (ValueExpression) stateArray[0];
        source = (ValueExpression) stateArray[1];

    }

}
