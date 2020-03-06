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

package com.sun.faces.systest.model;

import com.sun.faces.systest.TestValidator01;

import javax.faces.event.AbortProcessingException;
import javax.faces.validator.Validator;


public class ValidatorBean extends Object {

    public ValidatorBean() {
    }

    private Validator validator = null;
    public Validator getValidator() {
        if (validator == null) {
            return new TestValidator01();
        }
        return validator;
    }
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    private Validator doubleValidator = null;
    public Validator getDoubleValidator() {
        if (doubleValidator == null) {
            return new javax.faces.validator.DoubleRangeValidator();
        }
        return doubleValidator;
    }
    public void setDoubleValidator(Validator doubleValidator) {
        this.doubleValidator = doubleValidator;
    }

    private Validator lengthValidator = null;
    public Validator getLengthValidator() {
//        if (lengthValidator == null) {
//System.out.println("RETURN VAL INSTANCE..");
//            return new jakarta.faces.validator.LengthValidator();
//        }
        return lengthValidator;
    }
    public void setLengthValidator(Validator lengthValidator) {
        this.lengthValidator = lengthValidator;
System.out.println("SET VAL INSTANCE..");
    }

    private Validator longRangeValidator = null;
    public Validator getLongRangeValidator() {
        if (longRangeValidator == null) {
            return new javax.faces.validator.LongRangeValidator();
        }
        return longRangeValidator;
    }
    public void setLongRangeValidator(Validator longRangeValidator) {
        this.longRangeValidator = longRangeValidator;
    }

}
