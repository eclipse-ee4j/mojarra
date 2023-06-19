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

import java.io.IOException;

import jakarta.faces.FacesWrapper;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * <span class="changed_modified_2_3">Provides</span> a simple implementation of {@link StateManager} that can be
 * subclassed by developers wishing to provide specialized behavior to an existing {@link StateManager} instance. The
 * default implementation of all methods is to call through to the wrapped {@link StateManager}.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 1.2
 */
public abstract class StateManagerWrapper extends StateManager implements FacesWrapper<StateManager> {

    private final StateManager wrapped;

    /**
     * <p class="changed_added_2_3">
     * If this state manager has been decorated, the implementation doing the decorating should push the implementation
     * being wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     * @since 2.3
     */
    public StateManagerWrapper(StateManager wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public StateManager getWrapped() {
        return wrapped;
    }

    // ----------------------- Methods from jakarta.faces.application.StateManager

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link StateManager#writeState(jakarta.faces.context.FacesContext, java.lang.Object)} on the wrapped
     * {@link StateManager} object.
     * </p>
     *
     * @see StateManager#writeState(jakarta.faces.context.FacesContext, java.lang.Object)
     * @since 1.2
     */
    @Override
    public void writeState(FacesContext context, Object state) throws IOException {
        getWrapped().writeState(context, state);
    }

    /**
     * <p>
     * The default behavior of this method is to call
     * {@link StateManager#isSavingStateInClient(jakarta.faces.context.FacesContext)} on the wrapped {@link StateManager}
     * object.
     * </p>
     *
     * @see StateManager#isSavingStateInClient(jakarta.faces.context.FacesContext)
     * @since 1.2
     */
    @Override
    public boolean isSavingStateInClient(FacesContext context) {
        return getWrapped().isSavingStateInClient(context);
    }

    /**
     * <p class="changed_added_2_0">
     * The default behavior of this method is to call {@link StateManager#getViewState(jakarta.faces.context.FacesContext)}
     * on the wrapped {@link StateManager} object.
     * </p>
     *
     * @since 2.0
     */
    @Override
    public String getViewState(FacesContext context) {
        return getWrapped().getViewState(context);
    }
}
