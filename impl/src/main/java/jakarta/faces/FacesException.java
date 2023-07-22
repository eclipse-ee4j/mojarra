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

package jakarta.faces;

/**
 * <p>
 * This class encapsulates general Jakarta Faces exceptions.
 * </p>
 */

public class FacesException extends RuntimeException {

    // ----------------------------------------------------------- Constructors

    private static final long serialVersionUID = 3501800507902565991L;

    /**
     * <p>
     * Construct a new exception with no detail message or root cause.
     * </p>
     */
    public FacesException() {
        super();
    }

    /**
     * <p>
     * Construct a new exception with the specified detail message and no root cause.
     * </p>
     *
     * @param message The detail message for this exception
     */
    public FacesException(String message) {
        super(message);
    }

    /**
     * <p>
     * Construct a new exception with the specified root cause. The detail message will be set to
     * <code>(cause == null ? null :
     * cause.toString()</code>
     *
     * @param cause The root cause for this exception
     */
    public FacesException(Throwable cause) {
        super(cause == null ? null : cause.toString());
        this.cause = cause;
    }

    /**
     * <p>
     * Construct a new exception with the specified detail message and root cause.
     * </p>
     *
     * @param message The detail message for this exception
     * @param cause The root cause for this exception
     */
    public FacesException(String message, Throwable cause) {
        super(message);
        this.cause = cause;
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * <p>
     * The underlying exception that caused this exception.
     * </p>
     */
    private Throwable cause = null;

    // --------------------------------------------------------- Public Methods

    /**
     * <p>
     * Return the cause of this exception, or <code>null</code> if the cause is nonexistent or unknown.
     * </p>
     */
    @Override
    public Throwable getCause() {

        return cause;

    }

}
