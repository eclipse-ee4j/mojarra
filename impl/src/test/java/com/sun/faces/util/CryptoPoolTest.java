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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.GeneralSecurityException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class CryptoPoolTest {

    @Test
    void emptyPoolCreatesInstance() throws Exception {
        AtomicInteger created = new AtomicInteger();
        CryptoPool<Object> pool = new CryptoPool<>(() -> {
            created.incrementAndGet();
            return new Object();
        }, 2);

        pool.apply(instance -> instance);
        assertEquals(1, created.get());
    }

    @Test
    void instanceIsReusedAfterAction() throws Exception {
        CryptoPool<Object> pool = new CryptoPool<>(Object::new, 2);

        Object first = pool.apply(instance -> instance);

        assertSame(first, pool.apply(instance -> instance));
    }

    @Test
    void instanceIsReturnedWhenActionThrows() throws Exception {
        CryptoPool<Object> pool = new CryptoPool<>(Object::new, 2);
        Object[] used = new Object[1];

        assertThrows(GeneralSecurityException.class, () -> pool.apply(instance -> {
            used[0] = instance;
            throw new GeneralSecurityException("failed");
        }));

        assertSame(used[0], pool.apply(instance -> instance));
    }

    @Test
    void nestedCallsUseDifferentInstances() throws Exception {
        CryptoPool<Object> pool = new CryptoPool<>(Object::new, 2);

        pool.apply(outer -> {
            assertNotSame(outer, pool.apply(inner -> inner));
            return null;
        });
    }

    @Test
    void factoryFailurePropagates() {
        CryptoPool<Object> pool = new CryptoPool<>(() -> {
            throw new GeneralSecurityException("unavailable");
        }, 2);

        assertThrows(GeneralSecurityException.class, () -> pool.apply(instance -> instance));
        assertEquals(0, pool.size());
    }

    @Test
    void poolNeverHoldsMoreThanItsCapacity() throws Exception {
        CryptoPool<Object> pool = new CryptoPool<>(Object::new, 2);

        pool.apply(first -> pool.apply(second -> pool.apply(third -> null)));

        assertEquals(2, pool.size());
    }

}
