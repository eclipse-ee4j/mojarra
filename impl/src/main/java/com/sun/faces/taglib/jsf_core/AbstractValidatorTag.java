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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.MessageUtils;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.webapp.ValidatorELTag;
import jakarta.servlet.jsp.JspException;

/**
 * <p>
 * Base class for all <code>ValidatorTag<code>s.
 * </p>
 */
public class AbstractValidatorTag extends ValidatorELTag {

    private static final long serialVersionUID = 8941293586160549920L;

    private static final Logger LOGGER = FacesLogger.TAGLIB.getLogger();

    /**
     * <p>
     * The {@link jakarta.el.ValueExpression} that evaluates to an object that implements
     * {@link jakarta.faces.convert.Converter}.
     * </p>
     */
    protected ValueExpression binding = null;

    /**
     * <p>
     * The identifier of the {@link jakarta.faces.validator.Validator} instance to be created.
     * </p>
     */
    protected ValueExpression validatorId = null;

    // ---------------------------------------------------------- Public Methods

    /**
     * <p>
     * Set the expression that will be used to create a {@link jakarta.el.ValueExpression} that references a backing bean
     * property of the {@link jakarta.faces.validator.Validator} instance to be created.
     * </p>
     *
     * @param binding The new expression
     */
    public void setBinding(ValueExpression binding) {

        this.binding = binding;

    } // END setBinding

    /**
     * <p>
     * Set the identifer of the {@link jakarta.faces.validator.Validator} instance to be created.
     *
     * @param validatorId The identifier of the converter instance to be created.
     */
    public void setValidatorId(ValueExpression validatorId) {

        this.validatorId = validatorId;

    } // END setValidatorId

    // --------------------------------------------- Methods from ValdiatorELTag

    @Override
    protected Validator createValidator() throws JspException {

        try {
            return createValidator(validatorId, binding, FacesContext.getCurrentInstance());
        } catch (FacesException fe) {
            throw new JspException(fe.getCause());
        }

    }

    protected static Validator createValidator(ValueExpression validatorId, ValueExpression binding, FacesContext facesContext) {

        ELContext elContext = facesContext.getELContext();
        Validator validator = null;

        // If "binding" is set, use it to create a validator instance.
        if (binding != null) {
            try {
                validator = (Validator) binding.getValue(elContext);
                if (validator != null) {
                    return validator;
                }
            } catch (Exception e) {
                throw new FacesException(e);
            }
        }

        // If "validatorId" is set, use it to create the validator
        // instance. If "validatorId" and "binding" are both set, store the
        // validator instance in the value of the property represented by
        // the ValueExpression 'binding'.
        if (validatorId != null) {
            try {
                String validatorIdVal = (String) validatorId.getValue(elContext);
                validator = facesContext.getApplication().createValidator(validatorIdVal);
                if (validator != null && binding != null) {
                    binding.setValue(elContext, validator);
                }
            } catch (Exception e) {
                throw new FacesException(e);
            }
        }

        if (validator == null) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, MessageUtils.getExceptionMessageString(MessageUtils.CANNOT_VALIDATE_ID,
                        validatorId != null ? validatorId.getExpressionString() : "", binding != null ? binding.getExpressionString() : ""));
            }
        }

        return validator;

    }

}
