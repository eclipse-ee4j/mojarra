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
import java.util.concurrent.locks.ReentrantLock;

/**
 * LRU Cache adapted to the code style of Faces
 *
 * @author Paolo Bernardi
 */
public class LRUCache<K,V> {

    // we can't use a read/write lock because getting an element
    // from a LinkedHashMap with access order creates internal
    // structural changes, so we have a unique lock.
    // We use the fair mode to better serve waiting threads, otherwise
    // we could simply use a synchronized block over an "Object lock"
    private final ReentrantLock lock = new ReentrantLock(true);

    // We use an LRUMap to reuse a common Faces' data structure.
    // this is backed by a LinkedHashMap with access order
    private final LRUMap<K,V> cache;

    // we wrap the old Factory<K,V> class of Faces' Cache<K,V>
    // in a Function<K,V> because it can be used natively with Maps
    private final Cache.Factory<K,V> factory;

    public LRUCache(Cache.Factory<K,V> factory,int capacity) {
        this.cache = new LRUMap<>(capacity);
        this.factory = factory;
    }

    /**
     * get from cache if exists
     * else init the value + save in cache + return created value
     *
     * @param key the key
     *
     * @return the value from cache if exists, otherwise the newly created and saved value
     */
    public V get(K key) {
        Objects.requireNonNull(key);

        // lock
        try {
            // if a waiting thread is interrupted it will release the lock... server shutdown?
            // if this introduces a performance penalty we could use lock.lock() and remove the try-catch
            lock.lockInterruptibly();
        }
        catch (InterruptedException e) {
            // in this case we return null
            // ...it should be ok, isn't it?
            return null;
        }

        // read + (create and store if not exist) atomically
        try {
            return cache.computeIfAbsent( key , factory );
        }
        finally {
            lock.unlock();
        }
    }

    /**
     * clear the cache
     */
    public void clear() {
        // lock
        try {
            // if a waiting thread is interrupted it will release the lock... server shutdown?
            // if this introduces a performance penalty we could use lock.lock() and remove the try-catch
            lock.lockInterruptibly();
        }
        // release if interrupted and return
        catch (InterruptedException ignored) {
            return;
        }
        // clear
        try {
            cache.clear();
        }
        // always unlock
        finally {
            lock.unlock();
        }
    }

    /**
     * remove an element identified by
     * the passed key from the cache
     */
    public V remove(final K key) {
        Objects.requireNonNull(key);

        // lock
        try {
            // if a waiting thread is interrupted it will release the lock... server shutdown?
            // if this introduces a performance penalty we could use lock.lock() and remove the try-catch
            lock.lockInterruptibly();
        }
        // release if interrupted and return
        catch (InterruptedException ignored) {
            return null;
        }
        // remove
        try {
            return cache.remove(key);
        }
        // always unlock
        finally {
            lock.unlock();
        }
    }

}
