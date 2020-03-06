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

// ValidateLongRangeTag.java

package com.sun.faces.taglib.jsf_core;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.el.ExpressionFactory;
import jakarta.servlet.jsp.JspException;

import com.sun.faces.el.ELUtils;

import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.LongRangeValidator;
import jakarta.faces.validator.Validator;

/**
 * ValidateLongRangeTag is the tag handler class for <code>validate_longrange</code> tag.
 */

public class ValidateLongRangeTag extends MaxMinValidatorTag {

    private static final long serialVersionUID = 292617728229736800L;
    private static ValueExpression VALIDATOR_ID_EXPR = null;

// Attribute Instance Variables
    protected ValueExpression maximumExpression = null;
    protected ValueExpression minimumExpression = null;

    protected long maximum = 0;
    protected long minimum = 0;

// Relationship Instance Variables

//
// Constructors and Initializers
//

    public ValidateLongRangeTag() {
        super();
        if (VALIDATOR_ID_EXPR == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            ExpressionFactory factory = FacesContext.getCurrentInstance().getApplication().getExpressionFactory();
            VALIDATOR_ID_EXPR = factory.createValueExpression(context.getELContext(), "jakarta.faces.LongRange", String.class);
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

        LongRangeValidator result = (LongRangeValidator) super.createValidator();
        assert null != result;

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
                minimum = ((Number) ELUtils.evaluateValueExpression(minimumExpression, context)).longValue();
            } else {
                minimum = Long.valueOf(minimumExpression.getExpressionString()).longValue();
            }
        }
        if (maximumExpression != null) {
            if (!maximumExpression.isLiteralText()) {
                maximum = ((Number) ELUtils.evaluateValueExpression(maximumExpression, context)).longValue();
            } else {
                maximum = Long.valueOf(maximumExpression.getExpressionString()).longValue();
            }
        }
    }

} // end of class ValidateLongRangeTag
