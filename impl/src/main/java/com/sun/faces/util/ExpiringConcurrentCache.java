/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.faces.FacesException;

/**
 * This class implements an abstract ConcurrentCache with objects in the cache potentially expiring. Only non-expired
 * objects will be returned from the cache or considered to be contained in the cache The cache is self-managing, so no
 * remove() method is defined
 */
public final class ExpiringConcurrentCache<K, V> extends ConcurrentCache<K, V> {

    /**
     * Interface for checking whether a cached object expired
     */
    public interface ExpiryChecker<K, V> {
        /**
         * Checks whether a cached object expired
         *
         * @param key cache key
         * @param value cached value
         * @return true if the value expired and should be removed from the cache, false otherwise
         */
        boolean isExpired(K key, V value);
    }

    /**
     * Public constructor.
     *
     * @param f used to create new instances of objects that are not already available
     * @param checker used to check whether an object in the cache has expired
     */
    public ExpiringConcurrentCache(Factory<K, V> f, ExpiryChecker<K, V> checker) {
        super(f);
        _checker = checker;
    }

    @Override
    public V get(final K key) throws ExecutionException {
        // This method uses a design pattern from "Java concurrency in practice".
        // The pattern ensures that only one thread gets to create an object missing in the cache,
        // while the all the other threads tring to get it are waiting
        while (true) {
            boolean newlyCached = false;

            Future<V> f = _cache.get(key);
            if (f == null) {
                Callable<V> callable = () -> getFactory().newInstance(key);
                FutureTask<V> ft = new FutureTask<>(callable);
                // here is the real beauty of the concurrent utilities.
                // 1. putIfAbsent() is atomic
                // 2. putIfAbsent() will return the value already associated
                // with the specified key
                // So, if multiple threads make it to this point
                // they will all be calling f.get() on the same
                // FutureTask instance, so this guarantees that the instances
                // that the invoked Callable will return will be created once
                f = _cache.putIfAbsent(key, ft);
                if (f == null) {
                    f = ft;
                    ft.run();
                    newlyCached = true;
                }
            }
            try {
                V obj = f.get();
                if (!newlyCached && _getExpiryChecker().isExpired(key, obj)) {

                    // Note that we are using both key and value in remove() call to ensure
                    // that we are not removing the Future added after expiry check by a different thread
                    _cache.remove(key, f);
                } else {
                    return obj;
                }
            } catch (CancellationException ce) {
                if (_LOGGER.isLoggable(Level.SEVERE)) {
                    _LOGGER.log(Level.SEVERE, ce.toString(), ce);
                }
                _cache.remove(key, f);
            } catch (ExecutionException ee) {
                _cache.remove(key, f);
                throw ee;
            } catch (InterruptedException ie) {
                throw new FacesException(ie);

            }
        }
    }

    @Override
    public boolean containsKey(final K key) {

        Future<V> f = _cache.get(key);

        if (f != null && f.isDone() && !f.isCancelled()) {

            try {
                // Call get() with a 0 timeout to avoid any wait
                V obj = f.get(0, TimeUnit.MILLISECONDS);
                if (_getExpiryChecker().isExpired(key, obj)) {

                    // Note that we are using both key and value in remove() call to ensure
                    // that we are not removing the Future added after expiry check by a different thread
                    _cache.remove(key, f);
                } else {

                    return true;
                }
            } catch (TimeoutException | ExecutionException ce) {
            } catch (CancellationException ce) {
                if (_LOGGER.isLoggable(Level.SEVERE)) {
                    _LOGGER.log(Level.SEVERE, ce.toString(), ce);
                }
            } catch (InterruptedException ie) {
                throw new FacesException(ie);

            }
        }

        return false;
    }

    private ExpiryChecker<K, V> _getExpiryChecker() {
        return _checker;
    }

    private final ExpiryChecker<K, V> _checker;
    private final ConcurrentMap<K, Future<V>> _cache = new ConcurrentHashMap<>();

    private static final Logger _LOGGER = FacesLogger.UTIL.getLogger();
}
