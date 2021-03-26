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

package jakarta.faces.convert;

import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;

/**
 * <p>
 * <strong>ConverterException</strong> is an exception thrown by the <code>getAsObject()</code> or
 * <code>getAsText()</code> method of a {@link Converter}, to indicate that the requested conversion cannot be
 * performed.
 * </p>
 */

public class ConverterException extends FacesException {

    // ----------------------------------------------------------- Constructors

    private static final long serialVersionUID = 7371795075636746246L;

    /**
     * <p>
     * Construct a new exception with no detail message or root cause.
     * </p>
     */
    public ConverterException() {

        super();

    }

    /**
     * <p>
     * Construct a new exception with the specified detail message and no root cause.
     * </p>
     *
     * @param message The detail message for this exception
     */
    public ConverterException(String message) {

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
    public ConverterException(Throwable cause) {

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
    public ConverterException(String message, Throwable cause) {

        super(message, cause);

    }

    /**
     * <p>
     * Construct a new exception with the specified detail message and no root cause.
     * </p>
     *
     * @param message The detail message for this exception
     */
    public ConverterException(FacesMessage message) {

        super(message.getSummary());
        facesMessage = message;
    }

    /**
     * <p>
     * Construct a new exception with the specified detail message and root cause.
     * </p>
     *
     * @param message The detail message for this exception
     * @param cause The root cause for this exception
     */
    public ConverterException(FacesMessage message, Throwable cause) {

        super(message.getSummary(), cause);
        facesMessage = message;

    }

    /**
     * <p>
     * Returns the FacesMessage associated with this exception; this will only be available if the converter that thew this
     * exception instance placed it there.
     *
     * @return the message
     */
    public FacesMessage getFacesMessage() {
        return facesMessage;
    }

    private FacesMessage facesMessage;
}
