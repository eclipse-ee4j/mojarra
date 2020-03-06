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

import com.sun.faces.util.MessageUtils;

import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;

import jakarta.el.ValueExpression;
import jakarta.servlet.jsp.JspException;

/**
 * Basic implementation of <code>ConverterELTag</code>.
 */
public class ConverterTag extends AbstractConverterTag {

    // --------------------------------------------- Methods from ConverterELTag

    private static final long serialVersionUID = -7044710401705704550L;

    @Override
    protected Converter createConverter() throws JspException {

        if (converterId != null && converterId.isLiteralText()) {
            return createConverter(converterId, binding, FacesContext.getCurrentInstance());
        } else {
            return new BindingConverter(converterId, binding);
        }

    }

    // ----------------------------------------------------------- Inner Classes

    public static class BindingConverter implements Converter, StateHolder {

        ValueExpression converterId;
        ValueExpression binding;

        // -------------------------------------------------------- Constructors

        /**
         * <p>
         * This is only used during state restoration.
         * </p>
         */
        public BindingConverter() {
        }

        public BindingConverter(ValueExpression converterId, ValueExpression binding) {

            this.converterId = converterId;
            this.binding = binding;

        }

        // ---------------------------------------------- Methods From Converter

        @Override
        public Object getAsObject(FacesContext context, UIComponent component, String value) {
            Converter delegate = getDelegate(context);
            if (delegate != null) {
                return delegate.getAsObject(context, component, value);
            } else {
                throw new ConverterException(MessageUtils.getExceptionMessage(MessageUtils.CANNOT_CONVERT_ID,
                        converterId != null ? converterId.getExpressionString() : "", binding != null ? binding.getExpressionString() : ""));
            }
        }

        @Override
        public String getAsString(FacesContext context, UIComponent component, Object value) {
            Converter delegate = getDelegate(context);
            if (delegate != null) {
                return delegate.getAsString(context, component, value);
            } else {
                throw new ConverterException(MessageUtils.getExceptionMessage(MessageUtils.CANNOT_CONVERT_ID,
                        converterId != null ? converterId.getExpressionString() : "", binding != null ? binding.getExpressionString() : ""));
            }
        }

        // -------------------------------------------- Methods from StateHolder

        private Object[] state;

        @Override
        public Object saveState(FacesContext context) {

            if (context == null) {
                throw new NullPointerException();
            }
            if (state == null) {
                state = new Object[2];
            }
            state[0] = converterId;
            state[1] = binding;

            return state;

        }

        @Override
        public void restoreState(FacesContext context, Object state) {

            if (context == null) {
                throw new NullPointerException();
            }

            this.state = (Object[]) state;
            if (this.state != null) {
                this.converterId = (ValueExpression) this.state[0];
                this.binding = (ValueExpression) this.state[1];
            }

        }

        @Override
        public boolean isTransient() {

            return false;

        }

        @Override
        public void setTransient(boolean newTransientValue) {
            // no-op
        }

        // ----------------------------------------------------- Private Methods

        private Converter getDelegate(FacesContext context) {

            return createConverter(converterId, binding, context);

        }

    }

}
