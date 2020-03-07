/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.ext.taglib;

import com.sun.faces.ext.validator.CreditCardValidator;

import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.webapp.ValidatorELTag;

/**
 * Tag for the Regular Expression Validator.
 *
 * @author driscoll
 */
public class CreditCardValidatorTag extends ValidatorELTag {

    private static final long serialVersionUID = -1794591116597638154L;

    @Override
    protected Validator createValidator() {

        Application app = FacesContext.getCurrentInstance().getApplication();
        CreditCardValidator validator = (CreditCardValidator) app.createValidator("com.sun.faces.ext.validator.CreditCardValidator");
        return validator;
    }
}
