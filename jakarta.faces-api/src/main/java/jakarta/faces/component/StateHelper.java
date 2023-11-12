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

package jakarta.faces.component;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * <p class="changed_added_2_0">
 * Define a <code>Map</code>-like contract that makes it easier for components to implement {@link PartialStateHolder}.
 * Each {@link UIComponent} in the view will return an implementation of this interface from its
 * {@link UIComponent#getStateHelper} method.
 * </p>
 *
 * @since 2.0
 */
public interface StateHelper extends StateHolder {

    /**
     * <p class="changed_added_2_0">
     * Return the previously stored value and store the specified key/value pair. This is intended to store data that would
     * otherwise reside in an instance variable on the component.
     * </p>
     *
     * @param key the key for the value
     * @param value the value
     * @return the previously stored value
     * @since 2.0
     */
    Object put(Serializable key, Object value);

    /**
     * <p class="changed_added_2_0">
     * Remove the key/value pair from the helper, returning the value previously stored under this key.
     * </p>
     *
     * @param key the key to remove
     * @return the removed value.
     * @since 2.0
     */
    Object remove(Serializable key);

    /**
     * <p class="changed_added_2_0">
     * Store the specified <code>mapKey</code>/<code>value</code> in a <code>Map</code> that is internal to the helper, and
     * return the previously stored value. The <code>Map</code> will then be associated with <code>key</code>.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <p>
     * It's important to note for delta tracking that any modifications to the internal <code>Map</code> be made through
     * this method or {@link StateHelper#remove(java.io.Serializable, Object)}.
     * </p>
     *
     * </div>
     *
     * @param key the key of the map itself
     * @param mapKey the key within the internal map
     * @param value the value for the key in the internal map
     * @return the value.
     * @since 2.0
     */
    Object put(Serializable key, String mapKey, Object value);

    /**
     * <p class="changed_added_2_0">
     * Return the value currently associated with the specified <code>key</code> if any.
     * </p>
     *
     * @param key the key for which the value should be returned.
     * @return the value.
     * @since 2.0
     */
    Object get(Serializable key);

    /**
     * <p class="changed_added_2_0">
     * Attempts to find a value associated with the specified key, using the value expression collection from the component
     * if no such value is found.
     * </p>
     *
     * @param key the name of the value in the internal map, or the name of a value expression in the components value
     * expression collection.
     *
     * @return the evaluated value.
     * @since 2.0
     */
    Object eval(Serializable key);

    /**
     * <p class="changed_added_2_0">
     * Performs the same logic as {@link #eval(java.io.Serializable)} } but if no value is found, this will return the
     * specified <code>defaultValue</code>
     * </p>
     *
     * @param key the key for which the value should be returned.
     * @param defaultValue the value to return if no value is found in the call to <code>eval()</code>.
     * @return the evaluated value.
     * @since 2.0
     */
    Object eval(Serializable key, Object defaultValue);

    /**
     * <p class="changed_added_4_0">
     * Performs the same logic as {@link #eval(java.io.Serializable)} } but if no value is found, this will return the
     * return-value of the <code>defaultValueSupplier</code>
     * </p>
     *
     * @param key the key for which the value should be returned.
     * @param defaultValueSupplier the supplier used to evaluate the default value if no value is found in the call to <code>eval()</code>.
     * @return the evaluated value.
     * @since 4.0
     */
    Object eval(Serializable key, Supplier<Object> defaultValueSupplier);

    /**
     * <p class="changed_added_2_0">
     * Store the specified <code>value</code> in a <code>List</code> that is internal to the <code>StateHelper</code>.
     * </p>
     *
     * <p class="changed_added_2_0">
     * It's important to note for delta tracking that any modifications to the internal <code>List</code> be made through
     * this method or {@link StateHelper#remove(java.io.Serializable, Object)}.
     * </p>
     *
     * @param key the key for which the value should be returned.
     * @param value the value to add
     * @since 2.0
     */
    void add(Serializable key, Object value);

    /**
     * <p class="changed_added_2_0">
     * Remove a value from the inner data structure. Look in the inner data structure for the value at the given
     * <code>key</code>. If the value is a <code>Map</code>, remove and return the value under the key given by the
     * <code>valueOrKey</code> argument. If the value is a <code>Collection</code>, simply remove the value given by the
     * argument <code>valueOrKey</code> and return null.
     * </p>
     *
     * @param key the key of in the inner data structure whose value is a <code>Collection</code> or <code>Map</code>
     * @param valueOrKey the value or key to be removed.
     * @return the removed value.
     */
    Object remove(Serializable key, Object valueOrKey);

}
