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

import static java.util.Objects.requireNonNull;

/**
 * LRU Cache adapted to the code style of Faces
 * Optimized for modern JVMs and Virtual Threads.
 *
 * @author Paolo Bernardi
 */
public class LRUCache<K,V> {

    // On modern JVMs, the good old synchronized block has been highly optimized
    // and is 100% compatible with virtual threads (no pinning issues post-JEP 491).
    // Also, a plain Object monitor is way lighter than a ReentrantLock on the heap,
    // and we don't need any advanced lock features like timeouts or interruptibility.
    private final Object lock = new Object();
    private final Cache.Factory<K,V> factory;
    private final LRUMap<K,V> cache;

    public LRUCache(Cache.Factory<K,V> factory, int capacity) {
        this.factory = requireNonNull(factory);
        this.cache = new LRUMap<>(capacity);
    }

    /**
     * get from cache if exists
     * else init the value + save in cache + return created value
     */
    public V get(final K key) {
        requireNonNull(key);

        V value;

        // 1. read with lock (LinkedHashMap with access order does internal changes, a lock is required)
        synchronized (lock) {
            value = cache.get(key);
        }

        // if not exists:
        if (value == null) {

            // 2. compute the new value without locking
            V newValue = factory.apply(key);
            if (newValue == null) return null;

            // 3. insert new value with lock
            synchronized (lock) {
                value = cache.putIfAbsent(key, newValue);
                if (value == null) {
                    value = newValue;
                }
            }
        }

        return value;
    }

    /**
     * remove an element identified by the passed key from the cache
     */
    public V remove(final K key) {
        requireNonNull(key);
        synchronized (lock) {
            return cache.remove(key);
        }
    }

    /**
     * clear the cache
     */
    public void clear() {
        synchronized (lock) {
            cache.clear();
        }
    }

}
