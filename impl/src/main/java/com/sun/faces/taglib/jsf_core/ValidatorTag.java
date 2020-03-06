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

import jakarta.el.ValueExpression;
import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;
import jakarta.servlet.jsp.JspException;

/**
 * Basic implementation of <code>ValidatorELTag</code>.
 */
public class ValidatorTag extends AbstractValidatorTag {

    // --------------------------------------------- Methods from ValidatorELTag

    private static final long serialVersionUID = -2450754172058855404L;

    @Override
    protected Validator createValidator() throws JspException {

        if (validatorId != null && validatorId.isLiteralText()) {
            return createValidator(validatorId, binding, FacesContext.getCurrentInstance());
        } else {
            return new BindingValidator(validatorId, binding);
        }

    }

    // ----------------------------------------------------------- Inner Classes

    public static class BindingValidator implements Validator, StateHolder {

        private ValueExpression binding;
        private ValueExpression validatorId;

        // -------------------------------------------------------- Constructors

        /**
         * <p>
         * Only used during state restoration
         * </p>
         */
        public BindingValidator() {
        }

        public BindingValidator(ValueExpression validatorId, ValueExpression binding) {

            this.validatorId = validatorId;
            this.binding = binding;

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
            state[0] = validatorId;
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
                validatorId = (ValueExpression) this.state[0];
                binding = (ValueExpression) this.state[1];
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

        // ---------------------------------------------- Methods from Validator

        /**
         * <p>
         * Perform the correctness checks implemented by this {@link jakarta.faces.validator.Validator} against the specified
         * {@link jakarta.faces.component.UIComponent}. If any violations are found, a
         * {@link jakarta.faces.validator.ValidatorException} will be thrown containing the
         * {@link jakarta.faces.application.FacesMessage} describing the failure.
         *
         * @param context FacesContext for the request we are processing
         * @param component UIComponent we are checking for correctness
         * @param value the value to validate
         * @throws jakarta.faces.validator.ValidatorException if validation fails
         * @throws NullPointerException if <code>context</code> or <code>component</code> is <code>null</code>
         */
        @Override
        public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

            Validator instance = createValidator(validatorId, binding, context);

            if (instance != null) {
                instance.validate(context, component, value);
            } else {
                throw new ValidatorException(MessageUtils.getExceptionMessage(MessageUtils.CANNOT_VALIDATE_ID,
                        validatorId != null ? validatorId.getExpressionString() : "", binding != null ? binding.getExpressionString() : ""));
            }

        }

    }

}
