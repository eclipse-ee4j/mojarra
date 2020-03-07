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

package com.sun.faces.spi;

/**
 * Wraps any exception thrown by an implementation of <code>InjectionProvider</code>.
 */
public class InjectionProviderException extends Exception {

    private static final long serialVersionUID = -9118556608529051203L;

    /**
     * Creates a new <code>InjectionProviderException</code> with the root cause of the error.
     *
     * @param cause the root cause
     */
    public InjectionProviderException(Throwable cause) {
        super(cause);
    }

    /**
     * Creates a new <code>InjectionProviderException</code> with a descriptive message and the root cause of the error.
     *
     * @param message descriptive message
     * @param cause the root cause
     */
    public InjectionProviderException(String message, Throwable cause) {
        super(message, cause);
    }

} // END InjectionProviderException
