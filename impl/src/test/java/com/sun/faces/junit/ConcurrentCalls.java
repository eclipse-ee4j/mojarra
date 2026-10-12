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

package com.sun.faces.junit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Runs a call concurrently from several threads, to test that an object shared by all requests does not let them
 * interfere.
 */
public final class ConcurrentCalls {

    private static final int THREADS = 8;
    private static final int CALLS_PER_THREAD = 500;

    private ConcurrentCalls() {
    }

    /**
     * Invokes the call {@value #CALLS_PER_THREAD} times on each of {@value #THREADS} threads, every time with a distinct
     * id, and rethrows the first failure.
     *
     * @param call the call, which asserts its own result
     * @throws Exception the first failure, wrapped in an {@link java.util.concurrent.ExecutionException}
     */
    public static void run(Call call) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        try {
            List<Future<Void>> results = new ArrayList<>();
            for (int t = 0; t < THREADS; t++) {
                String prefix = "thread" + t + "_";
                results.add(executor.submit(() -> {
                    for (int i = 0; i < CALLS_PER_THREAD; i++) {
                        call.call(prefix + i);
                    }
                    return null;
                }));
            }
            for (Future<Void> result : results) {
                result.get();
            }
        } finally {
            executor.shutdownNow();
        }
    }

    /**
     * A call with the given distinct id.
     */
    @FunctionalInterface
    public interface Call {
        void call(String id) throws Exception;
    }
}
