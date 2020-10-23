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

package com.sun.faces.taglib;

/**
 * <p>
 * Interface defining the Validator Tag Parser implementation methods
 * </p>
 */
public interface TagParser {

    /**
     * <p>
     * Return the failure message for a failed validation
     * </p>
     *
     * @return String Failure message
     */
    String getMessage();

    /**
     * <p>
     * Return false if validator conditions have not been met
     * </p>
     *
     * @return boolean false if validation conditions have not been met
     */
    boolean hasFailed();

    /**
     * <p>
     * Set the Validator Info Bean
     * </p>
     */
    void setValidatorInfo(ValidatorInfo validatorInfo);

    /**
     * <p>
     * Parse the starting element. Parcel out to appropriate handler method.
     * </p>
     */
    void parseStartElement();

    /**
     * <p>
     * Parse the ending element. Parcel out to appropriate handler method.
     * </p>
     */
    void parseEndElement();
}
