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

package jakarta.faces.event;

import jakarta.faces.FacesException;

/**
 * <p>
 * An exception that may be thrown by event listeners to terminate the processing of the current event.
 * </p>
 */

public class AbortProcessingException extends FacesException {

    private static final long serialVersionUID = 7726524187590697427L;

    /**
     * <p>
     * Construct a new exception with no detail message or root cause.
     * </p>
     */
    public AbortProcessingException() {
        super();
    }

    /**
     * <p>
     * Construct a new exception with the specified detail message and no root cause.
     * </p>
     *
     * @param message The detail message for this exception
     */
    public AbortProcessingException(String message) {
        super(message);
    }

    /**
     * <p>
     * Construct a new exception with the specified root cause.
     * </p>
     *
     * @param cause The root cause for this exception
     */
    public AbortProcessingException(Throwable cause) {
        super(cause);
    }

    /**
     * <p>
     * Construct a new exception with the specified detail message and root cause.
     * </p>
     *
     * @param message The detail message for this exception
     * @param cause The root cause for this exception
     */
    public AbortProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

}
