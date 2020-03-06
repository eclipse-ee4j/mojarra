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

package com.sun.faces.mgbean;

import jakarta.faces.FacesException;

/**
 * <p>
 * Represents errors from managed bean pre-processing.
 */
public class ManagedBeanPreProcessingException extends FacesException {

    private static final long serialVersionUID = 5355477597212764294L;

    public enum Type {
        CHECKED, UNCHECKED
    }

    private Type type = Type.CHECKED;

    // ------------------------------------------------------------ Constructors

    public ManagedBeanPreProcessingException() {
        super();
    }

    public ManagedBeanPreProcessingException(Type type) {
        super();
        this.type = type;
    }

    public ManagedBeanPreProcessingException(String message) {
        super(message);
    }

    public ManagedBeanPreProcessingException(String message, Type type) {
        super(message);
        this.type = type;
    }

    public ManagedBeanPreProcessingException(Throwable t) {
        super(t);
    }

    public ManagedBeanPreProcessingException(Throwable t, Type type) {
        super(t);
        this.type = type;
    }

    public ManagedBeanPreProcessingException(String message, Throwable t) {
        super(message, t);
    }

    public ManagedBeanPreProcessingException(String message, Throwable t, Type type) {
        super(message, t);
        this.type = type;
    }

    // ---------------------------------------------------------- Public Methods

    public Type getType() {

        return type;

    }

}
