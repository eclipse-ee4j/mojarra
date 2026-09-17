/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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

package com.sun.faces.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;



import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Guards what makes the {@link CdiUtils} bean-resolution caches usable under load: reading them takes no lock.
 *
 * <p>Every {@code Application#createConverter/createValidator/createBehavior} call reads them -- per component,
 * per render, from every request thread -- so a cache that serializes its readers serializes the server. The
 * read path must therefore stay free of {@code synchronized}, of {@code Collections.synchronizedMap}, and of
 * any map whose reads mutate it, {@code WeakHashMap} included.
 */
public class CdiBeanCacheContentionTest {

    private static final String BEHAVIOR_ID = "jakarta.faces.Ajax";
    private static final ThreadMXBean THREADS = ManagementFactory.getThreadMXBean();

    private static final int MINIMUM_READER_THREADS = 8;
    private static final int READER_THREADS_PER_CORE = 2;
    private static final int CACHE_HITS_PER_THREAD = 20_000;
    private static final long BARRIER_TIMEOUT_MINUTES = 1;
    private static final long RUN_TIMEOUT_MINUTES = 5;
    private static final long JOIN_TIMEOUT_SECONDS = 10;

    private TestApplicationScope application;

    @BeforeEach
    public void deploy() {
        application = TestApplicationScope.deploy();
    }

    @AfterEach
    public void undeploy() {
        application.undeploy();
        TestApplicationScope.clearThreadState();
    }

    /**
     * Request threads doing nothing but warm cache hits must not block on a monitor. The count comes from
     * {@link ThreadMXBean}, the same source as the {@code BLOCKED (on object monitor)} lines of a thread dump,
     * and is a delta over the measured window so that thread start-up and class loading do not contribute.
     */
    @Test
    public void warmCacheHits_blockOnNoMonitor() throws Exception {
        int threads = Math.max(MINIMUM_READER_THREADS,
                Runtime.getRuntime().availableProcessors() * READER_THREADS_PER_CORE);

        CountingBeanManager beanManager = new CountingBeanManager();
        CdiUtils.createBehavior(beanManager, BEHAVIOR_ID);
        int lookupsWhenWarm = beanManager.getBeansByType.get();

        CountDownLatch ready = new CountDownLatch(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch looped = new CountDownLatch(threads);
        CountDownLatch measured = new CountDownLatch(1);
        long[] threadIds = new long[threads];
        Thread[] workers = new Thread[threads];

        for (int t = 0; t < threads; t++) {
            int index = t;
            workers[t] = new Thread(() -> {
                try {
                    application.makeCurrent(); // Every request thread serves this application.
                    threadIds[index] = Thread.currentThread().getId();
                    CdiUtils.createBehavior(beanManager, BEHAVIOR_ID); // Link lambdas and load classes first.
                    ready.countDown();
                    start.await();
                    for (int i = 0; i < CACHE_HITS_PER_THREAD; i++) {
                        CdiUtils.createBehavior(beanManager, BEHAVIOR_ID);
                    }
                    looped.countDown();
                    measured.await(); // Stay alive and idle while the counters are read.
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "cache-reader-" + t);
            workers[t].setDaemon(true);
            workers[t].start();
        }

        long[] blockedBefore;
        long[] blockedAfter;

        try {
            assertTrue(ready.await(BARRIER_TIMEOUT_MINUTES, TimeUnit.MINUTES), "workers did not reach the start barrier");
            blockedBefore = blockedCounts(threadIds);

            start.countDown();
            assertTrue(looped.await(RUN_TIMEOUT_MINUTES, TimeUnit.MINUTES), "workers did not finish");

            blockedAfter = blockedCounts(threadIds);
        } finally {
            measured.countDown();
        }

        long blocked = 0;
        for (int t = 0; t < threads; t++) {
            blocked += blockedAfter[t] - blockedBefore[t];
        }
        for (Thread worker : workers) {
            worker.join(TimeUnit.SECONDS.toMillis(JOIN_TIMEOUT_SECONDS));
        }

        assertEquals(lookupsWhenWarm, beanManager.getBeansByType.get(),
                "the whole parallel run was cache hits -- no bean resolution took place");

        // A lock on the read path blocks per operation, which is thousands of events for this workload; the
        // JVM itself contributes a handful whatever the code does, hence a bound rather than zero.
        assertTrue(blocked < threads, "reading the cache must take no lock, but " + threads + " threads were "
                + "BLOCKED " + blocked + " times over " + threads * CACHE_HITS_PER_THREAD + " cache hits");
    }

    /**
     * Reading the counters from this thread while the workers are parked, rather than from the workers
     * themselves, keeps contention inside the JMX implementation out of the measurement.
     */
    private static long[] blockedCounts(long[] threadIds) {
        long[] counts = new long[threadIds.length];

        for (int t = 0; t < threadIds.length; t++) {
            ThreadInfo threadInfo = THREADS.getThreadInfo(threadIds[t]);
            assertNotNull(threadInfo, "worker thread died before the counters were read");
            counts[t] = threadInfo.getBlockedCount();
        }

        return counts;
    }

}
