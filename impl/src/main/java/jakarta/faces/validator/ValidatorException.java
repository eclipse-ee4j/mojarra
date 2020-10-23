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

package jakarta.faces.validator;

import java.util.Collection;

import jakarta.faces.FacesException;
import jakarta.faces.application.FacesMessage;

/**
 * <p>
 * A <strong class="changed_modified_2_0">ValidatorException</strong> is an exception thrown by the
 * <code>validate()</code> method of a {@link Validator} to indicate that validation failed.
 */
public class ValidatorException extends FacesException {
    // ----------------------------------------------------------- Constructors

    private static final long serialVersionUID = 6459492016772012827L;

    /**
     * <p>
     * Construct a new exception with the specified message and no root cause.
     * </p>
     *
     * @param message The message for this exception
     */
    public ValidatorException(FacesMessage message) {

        super(message.getSummary());
        this.message = message;
    }

    /**
     * <p class="changed_added_2_0">
     * Allow this one exception to contain multiple messages.
     * </p>
     *
     * @param messages the list of messages for this exception
     *
     * @since 2.0
     */

    public ValidatorException(Collection<FacesMessage> messages) {
        this.messages = messages;
    }

    /**
     * <p>
     * Construct a new exception with the specified detail message and root cause.
     * </p>
     *
     * @param message The detail message for this exception
     * @param cause The root cause for this exception
     */
    public ValidatorException(FacesMessage message, Throwable cause) {

        super(message.getSummary(), cause);
        this.message = message;
    }

    /**
     * <p class="changed_added_2_0">
     * Allow this one exception to contain multiple messages, while passing on the root cause to the superclass
     * </p>
     *
     * @param messages the detail messages for this exception
     * @param cause the root cause for this exception
     *
     * @since 2.0
     */

    public ValidatorException(Collection<FacesMessage> messages, Throwable cause) {
        super(messages.isEmpty() ? "" : messages.iterator().next().getSummary(), cause);
        this.messages = messages;
    }

    /**
     * <p class="changed_modified_2_0">
     * Returns the <code>FacesMessage</code> associated with the exception. If this instance was created with a constructor
     * that takes <code>Collection&lt;FacesMessage&gt;</code>, this method returns the first message in the
     * <code>Collection</code>
     * </p>
     *
     * @return the message
     */
    public FacesMessage getFacesMessage() {
        FacesMessage result = message;
        if (null == result && null != messages && !messages.isEmpty()) {
            result = messages.iterator().next();
        }
        return result;
    }

    /**
     * <p class="changed_modified_2_0">
     * If this instance was created with a constructor that takes <code>Collection&lt;FacesMessage&gt;</code>, this method
     * returns the passed collection, otherwise this method returns <code>null</code>.
     * </p>
     *
     * @since 2.0
     *
     * @return the messages
     */

    public Collection<FacesMessage> getFacesMessages() {
        return messages;
    }

    private FacesMessage message;
    private Collection<FacesMessage> messages;
}
