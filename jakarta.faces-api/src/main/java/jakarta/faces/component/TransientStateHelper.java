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
 * <p class="changed_added_2_1">
 * Define a <code>Map</code>-like contract that makes it easier for components to implement
 * {@link TransientStateHolder}. Each {@link UIComponent} in the view will return an implementation of this interface
 * from its {@link UIComponent#getTransientStateHelper} method.
 * </p>
 *
 * <div class="changed_added_2_1">
 *
 * <p>
 * The values retrieved or saved through {@link #getTransient} or {@link #putTransient} will not be preserved between
 * requests.
 * </p>
 *
 * </div>
 *
 * @since 2.1
 *
 */
public interface TransientStateHelper extends TransientStateHolder {
    /**
     * <p class="changed_added_2_1">
     * Return the value currently associated with the specified <code>key</code> if any.
     * </p>
     *
     * @param key the key for which the value should be returned.
     * @return the stored value.
     * @since 2.1
     */
    Object getTransient(Object key);

    /**
     * <p class="changed_added_2_1">
     * Performs the same logic as {@link #getTransient} } but if no value is found, this will return the specified
     * <code>defaultValue</code>
     * </p>
     *
     * @param key the key for which the value should be returned.
     * @param defaultValue the value to return if no value is found in the call to <code>get()</code>.
     * @return the stored value.
     * @since 2.1
     */
    Object getTransient(Object key, Object defaultValue);

    /**
     * <p class="changed_added_2_1">
     * Return the previously stored value and store the specified key/value pair. This is intended to store data that would
     * otherwise reside in an instance variable on the component.
     * </p>
     *
     * @param key the key for the value
     * @param value the value
     * @return the previously stored value
     * @since 2.1
     */
    Object putTransient(Object key, Object value);
}
