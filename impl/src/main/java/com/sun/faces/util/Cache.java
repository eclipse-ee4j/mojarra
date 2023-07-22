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

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * A concurrent caching mechanism.
 */
public class Cache<K, V> {

    /**
     * Factory interface for creating various cacheable objects.
     */
    public interface Factory<K,V> extends Function<K,V> {

        V newInstance(final K arg) throws InterruptedException;

        @Override
        default V apply(K key) {
            try {
                return newInstance( key );
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private final ConcurrentMap<K, V> cache = new ConcurrentHashMap<>();
    private final Factory<K, V> factory;

    // -------------------------------------------------------- Constructors

    /**
     * Constructs this cache using the specified <code>Factory</code>.
     *
     * @param factory a factory to create or retrieve the element that need to be cached
     */
    public Cache(Factory<K,V> factory) {

        this.factory = factory;
    }

    // ------------------------------------------------------ Public Methods

    /**
     * If a value isn't associated with the specified key, a new {@link java.util.concurrent.Callable} will be created
     * wrapping the <code>Factory</code> specified via the constructor and passed to a
     * {@link java.util.concurrent.FutureTask}. This task will be passed to the backing ConcurrentMap. When
     * {@link java.util.concurrent.FutureTask#get()} is invoked, the Factory will return the new Value which will be cached
     * by the {@link java.util.concurrent.FutureTask}.
     *
     * @param key the key the value is associated with
     * @return the value for the specified key, if any
     */
    public V get(final K key) {
        Objects.requireNonNull(key);

        return cache.computeIfAbsent( key , factory );
    }

    public V remove(final K key) {
        return cache.remove(key);

    }

}
