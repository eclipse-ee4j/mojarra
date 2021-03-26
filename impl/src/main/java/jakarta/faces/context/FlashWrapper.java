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

package jakarta.faces.context;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import jakarta.faces.FacesWrapper;

/**
 * <p class="changed_added_2_2">
 * <span class="changed_modified_2_3">Provides</span> a simple implementation of {@link Flash} that can be subclassed by
 * developers wishing to provide specialized behavior to an existing {@link Flash} instance. The default implementation
 * of all methods is to call through to the wrapped {@link Flash}.
 * </p>
 *
 * <p class="changed_added_2_3">
 * Usage: extend this class and push the implementation being wrapped to the constructor and use {@link #getWrapped} to
 * access the instance being wrapped.
 * </p>
 *
 * @since 2.2
 */
public abstract class FlashWrapper extends Flash implements FacesWrapper<Flash> {

    private Flash wrapped;

    /**
     * @deprecated Use the other constructor taking the implementation being wrapped.
     */
    @Deprecated
    public FlashWrapper() {

    }

    /**
     * <p class="changed_added_2_3">
     * If this flash has been decorated, the implementation doing the decorating should push the implementation being
     * wrapped to this constructor. The {@link #getWrapped()} will then return the implementation being wrapped.
     * </p>
     *
     * @param wrapped The implementation being wrapped.
     * @since 2.3
     */
    public FlashWrapper(Flash wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public Flash getWrapped() {
        return wrapped;
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Flash#doPostPhaseActions(FacesContext)} on the wrapped
     * {@link Flash} object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public void doPostPhaseActions(FacesContext ctx) {
        getWrapped().doPostPhaseActions(ctx);

    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Flash#doPrePhaseActions(FacesContext)} on the wrapped
     * {@link Flash} object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public void doPrePhaseActions(FacesContext ctx) {
        getWrapped().doPrePhaseActions(ctx);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Flash#isKeepMessages()} on the wrapped {@link Flash} object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public boolean isKeepMessages() {
        return getWrapped().isKeepMessages();
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Flash#isRedirect()} on the wrapped {@link Flash} object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public boolean isRedirect() {
        return getWrapped().isRedirect();
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Flash#keep(String)} on the wrapped {@link Flash} object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public void keep(String key) {
        getWrapped().keep(key);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Flash#putNow(String, Object)} on the wrapped {@link Flash}
     * object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public void putNow(String key, Object value) {
        getWrapped().putNow(key, value);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Flash#setKeepMessages(boolean)} on the wrapped {@link Flash}
     * object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public void setKeepMessages(boolean newValue) {
        getWrapped().setKeepMessages(newValue);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Flash#setRedirect(boolean)} on the wrapped {@link Flash}
     * object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public void setRedirect(boolean newValue) {
        getWrapped().setRedirect(newValue);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Flash#clear()} on the wrapped {@link Flash} object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public void clear() {
        getWrapped().clear();
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Flash#containsKey(Object)} on the wrapped {@link Flash} object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public boolean containsKey(Object key) {
        return getWrapped().containsKey(key);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Flash#containsValue(Object)} on the wrapped {@link Flash}
     * object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public boolean containsValue(Object value) {
        return getWrapped().containsValue(value);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Flash#entrySet()} on the wrapped {@link Flash} object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return getWrapped().entrySet();
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Flash#get(Object)} on the wrapped {@link Flash} object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public Object get(Object key) {
        return getWrapped().get(key);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Flash#isEmpty()} on the wrapped {@link Flash} object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public boolean isEmpty() {
        return getWrapped().isEmpty();
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Flash#keySet()} on the wrapped {@link Flash} object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public Set<String> keySet() {
        return getWrapped().keySet();
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Flash#put} on the wrapped {@link Flash} object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public Object put(String key, Object value) {
        return getWrapped().put(key, value);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Flash#putAll(Map)} on the wrapped {@link Flash} object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public void putAll(Map<? extends String, ? extends Object> m) {
        getWrapped().putAll(m);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Flash#remove(Object)} on the wrapped {@link Flash} object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public Object remove(Object key) {
        return getWrapped().remove(key);
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Flash#size()} on the wrapped {@link Flash} object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public int size() {
        return getWrapped().size();
    }

    /**
     * <p class="changed_added_2_2">
     * The default behavior of this method is to call {@link Flash#values()} on the wrapped {@link Flash} object.
     * </p>
     *
     * @since 2.2
     */
    @Override
    public Collection<Object> values() {
        return getWrapped().values();
    }

}
