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

package com.sun.faces.ext.validator;

import java.util.Locale;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.validator.ValidatorException;

import java.io.Serializable;

/**
 * A Validator that checks against a Regular Expression (which is the pattern 
 * property).  The pattern must resolve to a String that follows the java.util.regex
 * standards.  
 * @author driscoll
 */
public class CreditCardValidator implements Validator, Serializable {

    private static final long serialVersionUID = 3534760827770436010L;

    /**
     * Validate a String against a regular expression pattern...  The full regex
     * pattern must be matched in order to pass the validation.
     * @param context Context of this request
     * @param component The component wrapping this validator 
     * @param obj A string which will be compared to the pattern property of this validator.  Must be a string.
     */
    @Override
    public void validate(FacesContext context, UIComponent component, Object obj) {

        FacesMessage fmsg;

        Locale locale = context.getViewRoot().getLocale();

        if (obj == null) {
            return;
        }
        if (!(obj instanceof String)) {
            fmsg = MojarraMessageFactory.getMessage(locale,
                    "com.sun.faces.ext.validator.creditcardValidator.NOT_STRING",
                    (Object) null);
            throw new ValidatorException(fmsg);
        }

        String input = (String) obj;

        if (!input.matches("^[0-9\\ \\-]*$")) {
            fmsg = MojarraMessageFactory.getMessage(locale,
                    "com.sun.faces.ext.validator.creditcardValidator.INVALID_CHARS",
                    (Object) null);
            throw new ValidatorException(fmsg);
        }

        if (!luhnCheck(stripNonDigit(input))) {
            fmsg = MojarraMessageFactory.getMessage(locale,
                    "com.sun.faces.ext.validator.creditcardValidator.INVALID_NUMBER",
                    (Object) null);
            throw new ValidatorException(fmsg);            
        }
    }

    private String stripNonDigit(String s) {
        return s.replaceAll(" ", "").replaceAll("-", "");
    }

    private boolean luhnCheck(String number) {
        int sum = 0;

        boolean timestwo = false;
        for (int i = number.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(number.substring(i, i + 1));
            if (timestwo) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            timestwo = !timestwo;
        }
        return sum % 10 == 0;
    }


}
