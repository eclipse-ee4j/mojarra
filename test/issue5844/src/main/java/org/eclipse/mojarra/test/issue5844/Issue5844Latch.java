/*
 * Copyright (c) Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GPL-2.0 with Classpath-exception-2.0 which
 * is available at https://openjdk.java.net/legal/gplv2+ce.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 or Apache-2.0
 */
package org.eclipse.mojarra.test.issue5844;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.CountDownLatch;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Sequences two waves of concurrent requests so that the eviction is deterministic.
 * <p>
 * Wave A first registers its view maps and lets f:viewParam put a value in its @ViewScoped beans, and then waits. Only
 * then does wave B register its view maps, which overflows the LRU map of active view maps and thereby evicts wave A's
 * view maps while wave A is still in flight. Wave A is released once wave B has registered, and then renders.
 * <p>
 * Both waves must fit in GlassFish's default http-thread-pool size of 5, else they cannot be in flight at once.
 */
@ApplicationScoped
public class Issue5844Latch {

    /** The number of requests per wave, against a maximum of 2 active view maps. */
    public static final int REQUESTS_PER_WAVE = 2;

    private static final int TIMEOUT_IN_SECONDS = 30;

    private final CountDownLatch waveAReady = new CountDownLatch(REQUESTS_PER_WAVE);
    private final CountDownLatch waveBRegistered = new CountDownLatch(REQUESTS_PER_WAVE);

    /**
     * Invoked by wave A right after f:viewParam has put a value in its @ViewScoped bean. Signals that wave A holds its
     * value and then waits until wave B has evicted it.
     */
    public void awaitWaveB() {
        waveAReady.countDown();
        await(waveBRegistered);
    }

    /**
     * Invoked by wave B before it touches the @ViewScoped bean, hence before it registers its view map. Waits until
     * wave A holds its value.
     */
    public void awaitWaveA() {
        await(waveAReady);
    }

    /**
     * Invoked by wave B right after it has registered its view map, which is what evicts wave A.
     */
    public void signalWaveBRegistered() {
        waveBRegistered.countDown();
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await(TIMEOUT_IN_SECONDS, SECONDS);
        }
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }
}
