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

import javax.el.ValueExpression;
import javax.el.ExpressionFactory;
import javax.servlet.jsp.JspException;

import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.RegexValidator;
import jakarta.faces.validator.Validator;


/**
 * Tag for the Regular Expression Validator.  Can accept a regex pattern as a
 * property - this will be used to validate against.
 * @author driscoll
 */
public class RegexValidatorTag extends AbstractValidatorTag {

    private static final long serialVersionUID = 5353063400995625645L;
    private ValueExpression regex;
    private ValueExpression VALIDATOR_ID_EXPR;

    // ------------------------------------------------------------ Constructors


    public RegexValidatorTag() {
        if (VALIDATOR_ID_EXPR == null) {
            FacesContext context = FacesContext.getCurrentInstance();
            ExpressionFactory factory =
                context.getApplication().getExpressionFactory();
            VALIDATOR_ID_EXPR =
                factory.createValueExpression(context.getELContext(),
                    "jakarta.faces.RegularExpression",String.class);
        }
    }

    /**
     * Set the Regular Expression to use for validation.
     * @param pattern A regular expression - needs to be escaped, @see java.util.regex .
     */
    public void setPattern(ValueExpression pattern) {
        this.regex = pattern;
    }

    @Override
    protected Validator createValidator() throws JspException {
        super.setValidatorId(VALIDATOR_ID_EXPR);
        RegexValidator validator = (RegexValidator) super.createValidator();
        assert (validator != null);
        if (regex != null) {
            FacesContext ctx = FacesContext.getCurrentInstance();
            validator.setPattern((String) regex.getValue(ctx.getELContext()));
        }
        return validator;
        
    }
}
