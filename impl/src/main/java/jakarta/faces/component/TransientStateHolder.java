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

import jakarta.faces.context.FacesContext;

/**
 * <p class="changed_added_2_1">
 * This interface is implemented by classes that need to save state that is expected to be available only within the
 * scope of the current request.
 * </p>
 *
 * <div class="changed_added_2_1">
 *
 * <p>
 * An implementor <strong>must</strong> implement both {@link #saveTransientState} and {@link #restoreTransientState}
 * methods in this class, since these two methods have a tightly coupled contract between themselves. In other words, if
 * there is an inheritance hierarchy, it is not permissible to have the {@link #saveTransientState} and
 * {@link #restoreTransientState} methods reside at different levels of the hierarchy.
 * </p>
 *
 * <p>
 * An example of transient state is the "submitted" property on forms.
 * </p>
 *
 * </div>
 *
 * @since 2.1
 */
public interface TransientStateHolder {

    /**
     * <p class="changed_added_2_1">
     * Return the object containing related "transient states". that could be used later to restore the "transient state".
     * </p>
     *
     * @param context the Faces context.
     * @return object containing transient values
     * @since 2.1
     */
    Object saveTransientState(FacesContext context);

    /**
     * <p class="changed_added_2_1">
     * Restore the "transient state" using the object passed as state.
     * </p>
     *
     * <p class="changed_added_2_1">
     * If the <code>state</code> argument is <code>null</code> clear any previous transient state if any and return.
     * </p>
     *
     * @param context the Faces context
     * @param state the object containing transient values
     * @since 2.1
     */
    void restoreTransientState(FacesContext context, Object state);
}
