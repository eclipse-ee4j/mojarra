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

package com.sun.faces.cdi;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.faces.validator.FacesValidator;

/**
 * A helper class.
 *
 * <p>
 * Used in CdiUtils to define a CDI qualifier so we can get a match out using the BeanManager API.
 * </p>
 */
@SuppressWarnings("all")
class FacesValidatorAnnotationLiteral extends AnnotationLiteral<FacesValidator> implements FacesValidator {

    /**
     * Stores the serial version UID.
     */
    private static final long serialVersionUID = -6266044469152347882L;

    /**
     * Stores the value.
     */
    private String value;

    /**
     * Constructor.
     *
     * @param value the value.
     * @param defaultValue the default value.
     */
    public FacesValidatorAnnotationLiteral(String value) {
        this.value = value;
    }

    /**
     * Get the value.
     *
     * @return the value.
     */
    @Override
    public String value() {
        return value;
    }

    /**
     * Is default.
     *
     * @return false.
     */
    @Override
    public boolean isDefault() {
        return false;
    }

    /**
     * Is managed.
     *
     * @return true.
     */
    @Override
    public boolean managed() {
        return true;
    }
}
