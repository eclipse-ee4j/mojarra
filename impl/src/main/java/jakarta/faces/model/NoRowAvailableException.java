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

package jakarta.faces.model;

/**
 * Custom exception to be thrown by the default <code>DataModel</code> implementations. This exception extends
 * IllegalArgumentException and is package private so the TCK will continue to pass, but the exception type is more
 * descriptive.
 */
class NoRowAvailableException extends IllegalArgumentException {

    private static final long serialVersionUID = 3794135774633215459L;

    // ------------------------------------------------------------ Constructors

    public NoRowAvailableException() {
        super();
    }

    public NoRowAvailableException(String s) {
        super(s);
    }

    public NoRowAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoRowAvailableException(Throwable cause) {
        super(cause);
    }

}
