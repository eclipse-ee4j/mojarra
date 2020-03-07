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
import jakarta.faces.convert.FacesConverter;

/**
 * A helper class.
 *
 * <p>
 * Used in CdiUtils to define a CDI qualifier so we can get a match out using the BeanManager API.
 * </p>
 */
@SuppressWarnings("all")
class FacesConverterAnnotationLiteral extends AnnotationLiteral<FacesConverter> implements FacesConverter {

    /**
     * Stores the serial version UID.
     */
    private static final long serialVersionUID = -6822858461634741174L;

    /**
     * Stores the value attribute.
     */
    private String value;

    /**
     * Stores the forClass attribute.
     */
    private Class forClass;

    /**
     * Constructor.
     *
     * @param value the value attribute.
     * @param forClass the forClass attribute.
     */
    public FacesConverterAnnotationLiteral(String value, Class forClass) {
        this.value = value;
        this.forClass = forClass;
    }

    /**
     * Get the value attribute.
     *
     * @return the value attribute.
     */
    @Override
    public String value() {
        return value;
    }

    /**
     * Get the forClass attribute.
     *
     * @return the forClass attribute.
     */
    @Override
    public Class forClass() {
        return forClass;
    }

    /**
     * Get the managed attribute.
     *
     * @return true.
     */
    @Override
    public boolean managed() {
        return true;
    }
}
