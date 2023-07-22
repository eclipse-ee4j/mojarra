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

package com.sun.faces.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class MetadataWrapperMap<K, V> implements Map<K, V> {

    public MetadataWrapperMap(Map<K, V> toWrap) {
        this.wrapped = toWrap;
        metadata = new ConcurrentHashMap<>();
    }

    protected Map<K, Map<Object, Object>> getMetadata() {
        return metadata;
    }

    private final Map<K, V> wrapped;
    private final Map<K, Map<Object, Object>> metadata;

    @Override
    public void clear() {
        this.wrapped.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.wrapped.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.wrapped.containsValue(value);
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return this.wrapped.entrySet();
    }

    @Override
    public V get(Object key) {
        return this.wrapped.get(key);
    }

    @Override
    public boolean isEmpty() {
        return this.wrapped.isEmpty();
    }

    @Override
    public Set<K> keySet() {
        return this.wrapped.keySet();
    }

    @Override
    public V put(K key, V value) {
        this.onPut(key, value);
        return this.wrapped.put(key, value);
    }

    protected abstract V onPut(K key, V value);

    @Override
    public void putAll(Map m) {
        this.wrapped.putAll(m);
    }

    @Override
    public V remove(Object key) {
        return this.wrapped.remove(key);
    }

    @Override
    public int size() {
        return this.wrapped.size();
    }

    @Override
    public Collection<V> values() {
        return this.wrapped.values();
    }

}
