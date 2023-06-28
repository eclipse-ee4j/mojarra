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
 *
 * <p>
 * <span class="changed_modified_2_0_rev_a">This</span> interface is implemented by classes that need to save their
 * state between requests.
 * </p>
 *
 * <p>
 * An implementor <strong>must</strong> implement both {@link #saveState} and {@link #restoreState} methods in this
 * class, since these two methods have a tightly coupled contract between themselves. In other words, if there is an
 * inheritance hierarchy, it is not permissible to have the {@link #saveState} and {@link #restoreState} methods reside
 * at different levels of the hierarchy.
 * </p>
 *
 * <p>
 * An implementor must have a public no-args constructor.
 * </p>
 *
 */

public interface StateHolder {

    /**
     * <p>
     * Gets the state of the instance as a <code>Serializable</code> Object.
     * </p>
     *
     * <p>
     * If the class that implements this interface has references to instances that implement StateHolder (such as a
     * <code>UIComponent</code> with event handlers, validators, etc.) this method must call the {@link #saveState} method
     * on all those instances as well. <strong>This method must not save the state of children and facets.</strong> That is
     * done via the {@link jakarta.faces.application.StateManager}
     * </p>
     *
     * <p>
     * This method must not alter the state of the implementing object. In other words, after executing this code:
     * </p>
     *
     * <pre>
     * <code>
     * Object state = component.saveState(facesContext);
     * </code>
     * </pre>
     *
     * <p>
     * <code>component</code> should be the same as before executing it.
     * </p>
     *
     * <p>
     * The return from this method must be <code>Serializable</code>
     * </p>
     *
     * @param context the Faces context.
     * @return the saved state.
     * @throws NullPointerException if <code>context</code> is null
     */

    Object saveState(FacesContext context);

    /**
     *
     * <p>
     * <span class="changed_modified_2_0_rev_a">Perform</span> any processing required to restore the state from the entries
     * in the state Object.
     * </p>
     *
     * <p>
     * If the class that implements this interface has references to instances that also implement StateHolder (such as a
     * <code>UIComponent</code> with event handlers, validators, etc.) this method must call the {@link #restoreState}
     * method on all those instances as well.
     * </p>
     *
     * <p class="changed_modified_2_0_rev_a">
     * If the <code>state</code> argument is <code>null</code>, take no action and return.
     * </p>
     *
     * @param context the Faces context.
     * @param state the state.
     * @throws NullPointerException if <code>context</code> is null.
     */

    void restoreState(FacesContext context, Object state);

    /**
     *
     * <p>
     * If true, the Object implementing this interface must not participate in state saving or restoring.
     * </p>
     *
     * @return <code>true</code> if transient, <code>false</code> otherwise.
     */

    boolean isTransient();

    /**
     * <p>
     * <span class="changed_modified_2_0_rev_a">Denotes</span> whether or not the Object implementing this interface must or
     * must not participate in state saving or restoring.
     * </p>
     *
     * @param newTransientValue boolean pass <code>true</code> if this Object <span class="changed_modified_2_0_rev_a">will
     * not participate</span> in state saving or restoring, otherwise pass <code>false</code>.
     */
    void setTransient(boolean newTransientValue);

}
