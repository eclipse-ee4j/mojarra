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

/**
 * <p class="changed_added_2_0">
 * Components that want to leverage the partial state saving feature must implement this interface instead of
 * implementing {@link StateHolder}, from which this interface inherits.
 * </p>
 *
 * @since 2.0
 */
public interface PartialStateHolder extends StateHolder {

    /**
     * <p class="changed_added_2_0">
     * The runtime must ensure that the {@link #markInitialState} method is called on each instance of this interface in the
     * view at the appropriate time to indicate the component is in its initial state. The implementor of the interface must
     * ensure that {@link #initialStateMarked} returns <code>true</code> from the time <code>markInitialState()</code> is
     * called until {@link #clearInitialState} is called, after which time <code>initialStateMarked()</code> must return
     * <code>false</code>. Also, during the time that the instance returns <code>true</code> from
     * <code>initialStateMarked()</code>, the implementation must return only the state that has changed in its
     * implementation of {@link StateHolder#saveState}.
     * </p>
     *
     * @since 2.0
     */
    void markInitialState();

    /**
     * <p class="changed_added_2_0">
     * Return <code>true</code> if delta state changes are being tracked, otherwise <code>false</code>
     * </p>
     *
     * @return <code>true</code> if the initial state is marked, <code>false</code> otherwise.
     * @since 2.0
     */
    boolean initialStateMarked();

    /**
     * <p class="changed_added_2_0">
     * Reset the PartialStateHolder to a non-delta tracking state.
     * </p>
     *
     * @since 2.0
     */
    void clearInitialState();

}
