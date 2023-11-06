/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.faces.application;

import jakarta.faces.FacesException;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.StateManagementStrategy;

/**
 * <p>
 * Implementations must throw this {@link FacesException} when attempting to restore the view
 * {@link StateManagementStrategy#restoreView(FacesContext, String, String)} results in failure on postback.
 * </p>
 *
 * @since 1.2
 */
public class ViewExpiredException extends FacesException {

    private static final long serialVersionUID = 5175808310270035833L;

    // ----------------------------------------------------- Instance Variables

    /**
     * <p>
     * The view identifier of the view that could not be restored.
     * </p>
     */
    private String viewId;

    // ----------------------------------------------------------- Constructors

    /**
     * <p>
     * Construct a new exception with no detail message or root cause.
     * </p>
     */
    public ViewExpiredException() {
        super();
    }

    /**
     * <p>
     * Construct a new exception with the specified view identifier.
     * </p>
     *
     * @param viewId The view identifier for this exception
     */
    public ViewExpiredException(String viewId) {
        this.viewId = viewId;
    }

    /**
     * <p>
     * Construct a new exception with the specified detail message and no root cause.
     * </p>
     *
     * @param message The detail message for this exception
     * @param viewId The view identifier for this exception
     */
    public ViewExpiredException(String message, String viewId) {
        super(message);
        this.viewId = viewId;
    }

    /**
     * <p>
     * Construct a new exception with the specified root cause. The detail message will be set to
     * <code>(cause == null ? null :
     * cause.toString()</code>
     *
     * @param cause The root cause for this exception
     * @param viewId The view identifier for this exception
     */
    public ViewExpiredException(Throwable cause, String viewId) {
        super(cause);
        this.viewId = viewId;
    }

    /**
     * <p>
     * Construct a new exception with the specified detail message and root cause.
     * </p>
     *
     * @param message The detail message for this exception
     * @param cause The root cause for this exception
     * @param viewId The view identifier for this exception
     */
    public ViewExpiredException(String message, Throwable cause, String viewId) {
        super(message, cause);
        this.viewId = viewId;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * <p>
     * Return the view identifier of this exception, or <code>null</code> if the view identifier is nonexistent or unknown.
     * </p>
     *
     * @return the view id, or <code>null</code>.
     */
    public String getViewId() {
        return viewId;
    }

    /**
     * <p>
     * Return the message for this exception prepended with the view identifier if the view identifier is not
     * <code>null</code>, otherwise, return the message.
     * </p>
     *
     * @return the message.
     */
    @Override
    public String getMessage() {
        if (viewId != null) {
            return "viewId:" + viewId + " - " + super.getMessage();
        }

        return super.getMessage();
    }

}
