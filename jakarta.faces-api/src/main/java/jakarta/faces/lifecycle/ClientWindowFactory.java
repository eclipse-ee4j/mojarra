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

package jakarta.faces.lifecycle;

import jakarta.faces.FacesWrapper;
import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_2">
 * Create {@link ClientWindow} instances based on the incoming request.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 2.2
 */
public abstract class ClientWindowFactory implements FacesWrapper<ClientWindowFactory> {

    private ClientWindowFactory wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public ClientWindowFactory() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this factory has been decorated, the implementation doing the decorating should push the implementation being
     * wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     */
    public ClientWindowFactory(ClientWindowFactory wrapped) {
        this.wrapped = wrapped;
    }

    /**
     * <p class="changed_modified_2_3">
     * If this factory has been decorated, the implementation doing the decorating may override this method to provide
     * access to the implementation being wrapped.
     * </p>
     */
    @Override
    public ClientWindowFactory getWrapped() {
        return wrapped;
    }

    /**
     * <p class="changed_added_2_2">
     * The implementation is responsible for creating the {@link ClientWindow} instance for this request. If
     * {@link ClientWindow#CLIENT_WINDOW_MODE_PARAM_NAME} is "none" or unspecified, this method must return {@code null}. If
     * {@link ClientWindow#CLIENT_WINDOW_MODE_PARAM_NAME} is "url" the implementation must return a
     * <code>ClientWindow</code> instance that implements the url-mode semantics described in {@link ClientWindow}.
     *
     * @param context the {@link FacesContext} for this request.
     * @return the {@link ClientWindow} for this request, or {@code null}
     *
     * @since 2.2
     */

    public abstract ClientWindow getClientWindow(FacesContext context);

}
