/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

import java.security.GeneralSecurityException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * A bounded pool of crypto objects which are not thread safe, such as a {@link javax.crypto.Cipher} or a
 * {@link javax.crypto.Mac}. Each {@link #apply(Action)} call uses one instance exclusively, and returns it to the pool
 * afterwards, also when the action throws. The action must therefore initialize the instance before using it:
 * {@link javax.crypto.Cipher#init(int, java.security.Key)} discards all previous state of a {@link javax.crypto.Cipher},
 * and the JDK providers of {@link javax.crypto.Mac} reset it on {@link javax.crypto.Mac#init(java.security.Key)}. When
 * the pool is full, a returned instance is dropped.
 *
 * @param <T> the type of the pooled instances
 */
public final class CryptoPool<T> {

    /** Calls using a crypto object are short and CPU bound, so rarely more than this many run at the same time. */
    private static final int DEFAULT_CAPACITY = 2 * Runtime.getRuntime().availableProcessors();

    private final BlockingQueue<T> instances;
    private final Factory<T> factory;

    /**
     * @param factory creates an instance when the pool is empty
     */
    public CryptoPool(Factory<T> factory) {
        this(factory, DEFAULT_CAPACITY);
    }

    CryptoPool(Factory<T> factory, int capacity) {
        this.factory = factory;
        instances = new ArrayBlockingQueue<>(capacity);
    }

    /**
     * Applies the action to an instance which no other call uses meanwhile.
     *
     * @param <R> the type of the result
     * @param action the action, which must initialize the instance before using it
     * @return the result of the action
     * @throws GeneralSecurityException when the instance cannot be created, or the action throws it
     */
    public <R> R apply(Action<T, R> action) throws GeneralSecurityException {
        T instance = instances.poll();
        if (instance == null) {
            instance = factory.create();
        }
        try {
            return action.apply(instance);
        } finally {
            instances.offer(instance);
        }
    }

    int size() {
        return instances.size();
    }

    /**
     * Creates a pooled instance.
     *
     * @param <T> the type of the pooled instances
     */
    @FunctionalInterface
    public interface Factory<T> {
        T create() throws GeneralSecurityException;
    }

    /**
     * Uses a pooled instance.
     *
     * @param <T> the type of the pooled instances
     * @param <R> the type of the result
     */
    @FunctionalInterface
    public interface Action<T, R> {
        R apply(T instance) throws GeneralSecurityException;
    }
}
