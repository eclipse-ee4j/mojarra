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

package jakarta.faces.component;

import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;

/**
 * <p class="changed_added_2_0">
 * This exception indicates a failure to update the model and is created to wrap any exception that occurs during
 * {@link UIInput#updateModel}. The exception is then passed to
 * {@link jakarta.faces.context.ExceptionHandler#processEvent}, where the <code>ExceptionHandler</code> has an
 * opportunity to handle it.
 * </p>
 *
 * @since 2.0
 */
public class UpdateModelException extends FacesException {

    private static final long serialVersionUID = 6081145672680351218L;

    private final FacesMessage facesMessage;

    // ------------------------------------------------------------ Constructors

    /**
     * <p class="changed_added_2_0">
     * Store the argument <code>facesMessage</code> so it may be returned from {@link #getFacesMessage} and pass the
     * argument <code>cause</code> to the super constructor.
     * </p>
     *
     * @param facesMessage the message for the exception
     * @param cause the cause of this exception
     *
     * @since 2.0
     */
    public UpdateModelException(FacesMessage facesMessage, Throwable cause) {

        super(cause);
        this.facesMessage = facesMessage;

    }

    // ---------------------------------------------------------- Public Methods
    /**
     * <p class="changed_added_2_0">
     * Return the <code>FacesMessage</code> passed to the constructor.
     * </p>
     *
     * @return the message of this exception.
     *
     * @since 2.0
     */

    public FacesMessage getFacesMessage() {

        return facesMessage;

    }

}
