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

// ValidateLengthTag.java

package com.sun.faces.taglib.jsf_core;

import javax.el.ELContext;
import javax.el.ValueExpression;
import javax.el.ExpressionFactory;
import javax.servlet.jsp.JspException;

import com.sun.faces.el.ELUtils;

import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.LengthValidator;
import jakarta.faces.validator.Validator;

/**
 * ValidateLengthTag is the tag handler class for
 * <code>validate_length</code> tag
 */

public class ValidateLengthTag extends MaxMinValidatorTag {

    private static final long serialVersionUID = -3594596279980791500L;
    private static ValueExpression VALIDATOR_ID_EXPR = null;


// Attribute Instance Variables
    protected ValueExpression maximumExpression = null;
    protected ValueExpression minimumExpression = null;

    protected int maximum = 0;
    protected int minimum = 0;


// Relationship Instance Variables

//
// Constructors and Initializers
//

    public ValidateLengthTag() {
        super();
        if (VALIDATOR_ID_EXPR == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            ExpressionFactory factory = context.getApplication().
                    getExpressionFactory();
            VALIDATOR_ID_EXPR =
                factory.createValueExpression(context.getELContext(), 
                    "jakarta.faces.Length", String.class);
        }
    }

//
// Class methods
//

//
// General Methods
//

    public void setMaximum(ValueExpression newMaximum) {
        maximumSet = true;
        maximumExpression = newMaximum;
    }


    public void setMinimum(ValueExpression newMinimum) {
        minimumSet = true;
        minimumExpression = newMinimum;
    }

    @Override
    public int doStartTag() throws JspException {
        super.setValidatorId(VALIDATOR_ID_EXPR);
        return super.doStartTag();
    }


//
// Methods from ValidatorTag
//

    @Override
    protected Validator createValidator() throws JspException {

        LengthValidator result = (LengthValidator)
            super.createValidator();
        assert (null != result);

        evaluateExpressions();
        if (maximumSet) {
            result.setMaximum(maximum);
        }

        if (minimumSet) {
            result.setMinimum(minimum);
        }

        return result;
    }

/* Evaluates expressions as necessary */
    private void evaluateExpressions() {

        ELContext context = FacesContext.getCurrentInstance().getELContext();

        if (minimumExpression != null) {
            if (!minimumExpression.isLiteralText()) {
                minimum = ((Number)
                              ELUtils.evaluateValueExpression(minimumExpression,
                                  context)).intValue();
            } else {
                minimum =
                Integer.valueOf(minimumExpression.getExpressionString()).
                    intValue();
            }
        }
        if (maximumExpression != null) {
            if (!maximumExpression.isLiteralText()) {
                maximum = ((Number)
                              ELUtils.evaluateValueExpression(maximumExpression,
                                  context)).intValue();
            } else {
                maximum =
                Integer.valueOf(maximumExpression.getExpressionString()).
                    intValue();
            }
        }
    }

} // end of class ValidateLengthTag
