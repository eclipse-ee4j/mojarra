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

import jakarta.faces.FacesException;
import jakarta.faces.FacesWrapper;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseListener;

/**
 * <p class="changed_added_2_2">
 * <span class="changed_modified_2_3">Provides</span> a simple implementation of {@link Lifecycle} that can be
 * subclassed by developers wishing to provide specialized behavior to an existing {@link Lifecycle} instance. The
 * default implementation of all methods is to call through to the wrapped {@link Lifecycle}.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 2.2
 */

public abstract class LifecycleWrapper extends Lifecycle implements FacesWrapper<Lifecycle> {

    private Lifecycle wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public LifecycleWrapper() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this lifecycle has been decorated, the implementation doing the decorating should push the implementation being
     * wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     * @since 2.3
     */
    public LifecycleWrapper(Lifecycle wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Lifecycle getWrapped() {
        return wrapped;
    }

    @Override
    public void addPhaseListener(PhaseListener listener) {
        getWrapped().addPhaseListener(listener);
    }

    @Override
    public void execute(FacesContext context) throws FacesException {
        getWrapped().execute(context);
    }

    @Override
    public PhaseListener[] getPhaseListeners() {
        return getWrapped().getPhaseListeners();
    }

    @Override
    public void removePhaseListener(PhaseListener listener) {
        getWrapped().removePhaseListener(listener);
    }

    @Override
    public void render(FacesContext context) throws FacesException {
        getWrapped().render(context);
    }

    @Override
    public void attachWindow(FacesContext context) {
        getWrapped().attachWindow(context);
    }
}
