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

package com.sun.faces.context;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * This is the base Map for all Map instances required by @{link ExternalContext}.
 * </p>
 */
abstract class BaseContextMap<V> extends AbstractMap<String, V> {

    private Set<Map.Entry<String, V>> entrySet;
    private Set<String> keySet;
    private Collection<V> values;

    // -------------------------------------------------------- Methods from Map

    // Supported by maps if overridden
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    // Supported by maps if overridden
    @Override
    public void putAll(Map t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Map.Entry<String, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySet();
        }

        return entrySet;
    }

    @Override
    public Set<String> keySet() {
        if (keySet == null) {
            keySet = new KeySet();
        }

        return keySet;
    }

    @Override
    public Collection<V> values() {
        if (values == null) {
            values = new ValueCollection();
        }

        return values;
    }

    // Supported by maps if overridden
    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    // ------------------------------------------------------- Protected Methods

    protected boolean removeKey(Object key) {
        return this.remove(key) != null;
    }

    protected boolean removeValue(Object value) {
        boolean valueRemoved = false;
        if (value == null) {
            return false;
        }
        if (containsValue(value)) {
            for (Iterator i = entrySet().iterator(); i.hasNext();) {
                Map.Entry e = (Map.Entry) i.next();
                if (value.equals(e.getValue())) {
                    valueRemoved = remove(e.getKey()) != null;
                }
            }
        }
        return valueRemoved;
    }

    protected abstract Iterator<Map.Entry<String, V>> getEntryIterator();

    protected abstract Iterator<String> getKeyIterator();

    protected abstract Iterator<V> getValueIterator();

    // ----------------------------------------------------------- Inner Classes

    abstract static class BaseSet<E> extends AbstractSet<E> {

        @Override
        public int size() {
            int size = 0;
            for (Iterator<E> i = iterator(); i.hasNext(); size++) {
                i.next();
            }
            return size;
        }

    }

    class EntrySet extends BaseSet<Map.Entry<String, V>> {

        @Override
        public Iterator<Map.Entry<String, V>> iterator() {
            return getEntryIterator();
        }

        @Override
        public boolean remove(Object o) {
            return o instanceof Map.Entry && removeKey(((Map.Entry) o).getKey());
        }

    }

    class KeySet extends BaseSet<String> {

        @Override
        public Iterator<String> iterator() {
            return getKeyIterator();
        }

        @Override
        public boolean contains(Object o) {
            return containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            return o instanceof String && removeKey(o);
        }
    }

    class ValueCollection extends AbstractCollection<V> {

        @Override
        public int size() {
            int size = 0;
            for (Iterator<V> i = iterator(); i.hasNext(); size++) {
                i.next();
            }
            return size;
        }

        @Override
        public Iterator<V> iterator() {
            return getValueIterator();
        }

        @Override
        public boolean remove(Object o) {
            return removeValue(o);
        }
    }

    abstract static class BaseIterator<E> implements Iterator<E> {

        protected Enumeration e;
        protected String currentKey;
        protected boolean removeCalled = false;

        BaseIterator(Enumeration e) {
            this.e = e;
        }

        @Override
        public boolean hasNext() {
            return e.hasMoreElements();
        }

        public String nextKey() {
            removeCalled = false;
            currentKey = (String) e.nextElement();
            return currentKey;
        }
    }

    class EntryIterator extends BaseIterator<Map.Entry<String, V>> {

        EntryIterator(Enumeration e) {
            super(e);
        }

        @Override
        public void remove() {
            if (currentKey != null && !removeCalled) {
                removeCalled = true;
                removeKey(currentKey);
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public Map.Entry<String, V> next() {
            nextKey();
            return new Entry<>(currentKey, get(currentKey));
        }
    }

    class KeyIterator extends BaseIterator<String> {

        KeyIterator(Enumeration e) {
            super(e);
        }

        @Override
        public void remove() {
            if (currentKey != null && !removeCalled) {
                removeCalled = true;
                removeKey(currentKey);
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public String next() {
            return nextKey();
        }
    }

    class ValueIterator extends BaseIterator<V> {

        ValueIterator(Enumeration e) {
            super(e);
        }

        @Override
        public void remove() {
            if (currentKey != null && !removeCalled) {
                removeCalled = true;
                removeValue(get(currentKey));
            } else {
                throw new IllegalStateException();
            }
        }

        @Override
        public V next() {
            nextKey();
            return get(currentKey);
        }
    }

    static class Entry<V> implements Map.Entry<String, V> {

        // immutable Entry
        private final String key;
        private final V value;

        Entry(String key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        // No support of setting the value
        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int hashCode() {
            return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
        }

        @Override
        public boolean equals(Object obj) {
            if ( !(obj instanceof Map.Entry) ) {
                return false;
            }

            Map.Entry<String,V> input = (Map.Entry<String,V>) obj;
            Object key = input.getKey();
            Object value = input.getValue();

            return Objects.equals(key, this.key) &&
                   Objects.equals(value, this.value);
        }
    }

}
