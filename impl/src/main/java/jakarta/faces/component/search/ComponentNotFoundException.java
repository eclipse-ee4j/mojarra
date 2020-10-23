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

package jakarta.faces.component.search;

import jakarta.faces.FacesException;

/**
 * <p class="changed_added_2_3">
 * Typed {@link FacesException} for the {@link SearchExpressionHandler}, if a component can't be resolved.
 * </p>
 *
 * @since 2.3
 */
public class ComponentNotFoundException extends FacesException {

    private static final long serialVersionUID = -8962632721771880921L;

    /**
     * <p class="changed_added_2_3">
     * Construct a new exception with no detail message or root cause.
     * </p>
     *
     * @since 2.3
     */
    public ComponentNotFoundException() {
        super();
    }

    /**
     * <p class="changed_added_2_3">
     * Construct a new exception with the specified detail message and no root cause.
     * </p>
     *
     * @param message The detail message for this exception
     *
     * @since 2.3
     */
    public ComponentNotFoundException(String message) {
        super(message);
    }

    /**
     * <p class="changed_added_2_3">
     * Construct a new exception with the specified root cause. The detail message will be set to
     * <code>(cause == null ? null :
     * cause.toString()</code>
     *
     * @param cause The root cause for this exception
     *
     * @since 2.3
     */
    public ComponentNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * <p class="changed_added_2_3">
     * Construct a new exception with the specified detail message and root cause.
     * </p>
     *
     * @param message The detail message for this exception
     * @param cause The root cause for this exception
     *
     * @since 2.3
     */
    public ComponentNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
