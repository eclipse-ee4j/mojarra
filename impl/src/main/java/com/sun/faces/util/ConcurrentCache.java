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

import java.util.concurrent.ExecutionException;

/**
 * Defines a concurrent cache with a factory for creating new object instances.
 *
 * This (combined with ExpiringConcurrentCache) offers functionality similar to com.sun.faces.util.Cache. Two
 * differences:
 *
 * 1. Cache is concrete/assumes a particular implementation. ConcurrentCache is abstract/allows subclasses to provide
 * the implementation. This facilitates alternative implementations, such as DefaultFaceletCache's NoCache. 2.
 * ConcurrentCache does not provide remove() as part of its contract, since remove behavior may be subclass-specific.
 * For example, ExpiringConcurentCache automatically removes items by checking for expiration rather than requiring
 * manual removes.
 *
 * We should consider consolidating Cache and ConcurrentCache + ExpiringConcurrentCache into a single class hierarchy so
 * that we do not need to duplicate the JCIP scalable result cache code.
 */
public abstract class ConcurrentCache<K, V> {

    /**
     * Factory interface for creating various cacheable objects.
     */
    public interface Factory<K, V> {
        V newInstance(final K arg) throws Exception;
    }

    /**
     * Constructs this cache using the specified <code>Factory</code>.
     *
     * @param f
     */
    public ConcurrentCache(Factory<K, V> f) {
        _f = f;
    }

    /**
     * Retrieves a value for the specified key. If the value is not already present in the cache, a new instance will be
     * allocated using the <code>Factory</code> interface
     *
     * @param key the key the value is associated with
     * @return the value for the specified key
     */
    public abstract V get(final K key) throws ExecutionException;

    /**
     * Tests whether the cache contains a value for the specified key
     *
     * @param key key to test
     * @return true if the value for the specified key is already cached, false otherwise
     */
    public abstract boolean containsKey(final K key);

    /**
     * Retrieves a <code>Factory</code> instance aasociated with this cache
     *
     * @return <code>Factory</code> instance
     */
    protected final Factory<K, V> getFactory() {
        return _f;
    }

    private final Factory<K, V> _f;
}
