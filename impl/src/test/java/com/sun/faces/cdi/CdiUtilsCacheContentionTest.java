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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.annotation.Annotation;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.Bean;
import jakarta.enterprise.inject.spi.BeanManager;

import com.sun.faces.mock.MockBeanManager;

import org.junit.jupiter.api.Test;

/**
 * Guards the property that made {@link CdiUtils}'s bean-resolution caches usable under load: reading
 * them takes no lock.
 *
 * <p>The caches used to be {@code synchronizedMap(new WeakHashMap<>())}, whose monitor is taken for
 * every operation including a plain read. Because every
 * {@code Application#createConverter/createValidator/createBehavior} call passes through them -- per
 * component, per render, from every request thread -- that single monitor serialized request
 * processing, which production thread dumps showed as:
 *
 * <pre>
 * "default task-125" ... waiting for monitor entry
 *    java.lang.Thread.State: BLOCKED (on object monitor)
 *         at java.util.Collections$SynchronizedMap.computeIfAbsent(Collections.java:3077)
 *         - waiting to lock &lt;0x000000074f21a790&gt; (a java.util.Collections$SynchronizedMap)
 *         at com.sun.faces.cdi.CdiUtils.cachedBean(CdiUtils.java:230)
 *         at com.sun.faces.cdi.CdiUtils.createBehavior(CdiUtils.java:252)
 * </pre>
 *
 * <p>Entries are now added and removed only at deployment boundaries, so the read path is a volatile
 * read of an immutable snapshot. These tests fail if a lock returns to it, if the snapshot field
 * loses its {@code volatile}, or if the registration invariant is broken.
 */
public class CdiUtilsCacheContentionTest {

    private static final String BEHAVIOR_ID = "contentionTestBehavior";
    private static final ThreadMXBean THREADS = ManagementFactory.getThreadMXBean();

    /**
     * The snapshot field must stay {@code volatile}: readers deliberately never take the lock that
     * {@code registerBeanManager}/{@code unregisterBeanManager} hold, so without it there is no
     * happens-before edge and a request thread could keep using a stale snapshot indefinitely --
     * including one still holding an undeployed application's beans.
     */
    @Test
    public void cacheSnapshotField_isVolatile() throws Exception {
        Field field = CdiUtils.class.getDeclaredField("CACHES");

        assertTrue(Modifier.isVolatile(field.getModifiers()),
                "CdiUtils.CACHES must be volatile; readers never synchronize with the writers");
        assertTrue(Modifier.isStatic(field.getModifiers()), "CdiUtils.CACHES must be static");
    }

    /**
     * The point of the exercise: threads doing nothing but warm cache hits must not block on a monitor.
     * Counts come from {@link ThreadMXBean}, the same source as the {@code BLOCKED (on object monitor)}
     * lines of a thread dump, and are taken as a delta across the measured window so that thread
     * start-up and class loading do not contribute.
     */
    @Test
    public void parallelWarmCacheHits_blockOnNothing() throws Exception {
        int threads = Math.max(8, Runtime.getRuntime().availableProcessors() * 2);
        int iterationsPerThread = 20_000;

        CountingBeanManager beanManager = new CountingBeanManager();
        CdiUtils.createBehavior(beanManager, BEHAVIOR_ID);
        int lookupsAfterWarmup = beanManager.beanManagerCalls.get();

        Contention measured = measure(threads, iterationsPerThread,
                () -> CdiUtils.createBehavior(beanManager, BEHAVIOR_ID));

        System.out.printf("%nCdiUtils cache reads -- %d threads x %d warm cache hits: "
                + "%d blocked events, %d ms elapsed%n%n",
                threads, iterationsPerThread, measured.blockedCount, measured.elapsedMillis);

        assertEquals(lookupsAfterWarmup, beanManager.beanManagerCalls.get(),
                "the whole parallel run was cache hits -- no CDI resolution took place");
        assertEquals(0, measured.blockedCount,
                "reading the cache must take no lock, but threads were BLOCKED on a monitor "
                        + measured.blockedCount + " times");
    }

    /**
     * Copy-on-write means a deployment or undeployment replaces the snapshot wholesale. Readers of an
     * unrelated application must neither block on that nor lose their entry, which is what makes the
     * lock-free read path safe to combine with registration.
     */
    @Test
    public void cacheReads_areUnaffectedByConcurrentRegistrations() throws Exception {
        CountingBeanManager beanManager = new CountingBeanManager();
        CdiUtils.createBehavior(beanManager, BEHAVIOR_ID);
        int lookupsAfterWarmup = beanManager.beanManagerCalls.get();

        AtomicBoolean keepChurning = new AtomicBoolean(true);
        AtomicLong registrations = new AtomicLong();
        Thread churn = new Thread(() -> {
            while (keepChurning.get()) {
                // Deploy and undeploy other applications while the reader threads run.
                MockBeanManager other = new MockBeanManager();
                CdiUtils.registerBeanManager(other);
                CdiUtils.unregisterBeanManager(other);
                registrations.incrementAndGet();
            }
        }, "deployment-churn");
        churn.setDaemon(true);
        churn.start();

        Contention measured;
        try {
            measured = measure(8, 20_000, () -> CdiUtils.createBehavior(beanManager, BEHAVIOR_ID));
        } finally {
            keepChurning.set(false);
            churn.join(TimeUnit.SECONDS.toMillis(10));
        }

        assertTrue(registrations.get() > 0, "the churn thread should have rewritten the snapshot at least once");
        assertEquals(lookupsAfterWarmup, beanManager.beanManagerCalls.get(),
                "the reader's entry survived every snapshot rewrite -- all reads stayed cache hits");
        assertEquals(0, measured.blockedCount,
                "readers must not block behind the registration lock, but were BLOCKED "
                        + measured.blockedCount + " times");
    }

    /**
     * The registration invariant: a bean manager that was never registered -- or whose application has
     * been undeployed -- resolves without caching and never enters the snapshot. This is what makes an
     * unbounded map and a retained class loader structurally impossible rather than merely unlikely.
     */
    @Test
    public void unregisteredBeanManager_resolvesWithoutEverEnteringTheCache() throws Exception {
        CountingBeanManager stranger = new CountingBeanManager(false); // never registered

        for (int i = 0; i < 10; i++) {
            assertNull(CdiUtils.createBehavior(stranger, BEHAVIOR_ID), "resolution still works");
            assertNull(CdiUtils.createConverter(stranger, "someConverter"));
            assertNull(CdiUtils.createValidator(stranger, "someValidator"));
            assertNull(CdiUtils.getViewFacelet(stranger, "/someView.xhtml"));
            assertNull(CdiUtils.resolveBeanByName(stranger, "someBeanName"));
            assertNull(CdiUtils.resolveFacesContextProducerBean(stranger));
        }

        assertFalse(caches().containsKey(stranger),
                "an unregistered bean manager must never be added to the cache by a lookup");
        assertTrue(stranger.beanManagerCalls.get() >= 10,
                "every call resolved for real, because nothing was cached for it");
    }

    // --- helpers ---------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private static Map<BeanManager, ?> caches() throws Exception {
        Field field = CdiUtils.class.getDeclaredField("CACHES");
        field.setAccessible(true);
        return (Map<BeanManager, ?>) field.get(null);
    }

    /**
     * Runs {@code body} on {@code threads} threads and reports how many times they were blocked on a
     * monitor while doing so.
     *
     * <p>The count is a delta across the measured loop only: each worker first runs {@code body} once to
     * link lambdas, load classes and let the path be compiled, and every {@link ThreadMXBean} call is made
     * from this thread while the workers are parked. Querying the bean from forty threads at once would
     * otherwise contend inside the JMX implementation and be counted as if it were the workload's.
     */
    private static Contention measure(int threads, int iterationsPerThread, Runnable body) throws Exception {
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
                    threadIds[index] = Thread.currentThread().getId();
                    body.run();
                    ready.countDown();
                    start.await();
                    for (int i = 0; i < iterationsPerThread; i++) {
                        body.run();
                    }
                    looped.countDown();
                    measured.await(); // stay alive and idle while the counters are read
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "cache-reader-" + t);
            workers[t].setDaemon(true);
            workers[t].start();
        }

        assertTrue(ready.await(30, TimeUnit.SECONDS), "workers did not reach the start barrier");
        long[] blockedBefore = blockedCounts(threadIds);

        long startedAt = System.nanoTime();
        start.countDown();
        assertTrue(looped.await(5, TimeUnit.MINUTES), "workers did not finish");
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);

        long[] blockedAfter = blockedCounts(threadIds);
        measured.countDown();

        long blocked = 0;
        for (int t = 0; t < threads; t++) {
            blocked += blockedAfter[t] - blockedBefore[t];
        }
        for (Thread worker : workers) {
            worker.join(TimeUnit.SECONDS.toMillis(10));
        }

        return new Contention(blocked, elapsedMillis);
    }

    private static long[] blockedCounts(long[] threadIds) {
        long[] counts = new long[threadIds.length];
        for (int t = 0; t < threadIds.length; t++) {
            ThreadInfo info = THREADS.getThreadInfo(threadIds[t]);
            counts[t] = info == null ? 0 : info.getBlockedCount();
        }
        return counts;
    }

    private static final class Contention {
        final long blockedCount;
        final long elapsedMillis;

        Contention(long blockedCount, long elapsedMillis) {
            this.blockedCount = blockedCount;
            this.elapsedMillis = elapsedMillis;
        }
    }

    /**
     * A {@link BeanManager} that resolves nothing and counts how often it was consulted, so the tests can
     * tell a cache hit from a resolution. Registers itself, standing in for a deployed application.
     */
    private static class CountingBeanManager extends MockBeanManager {

        final AtomicInteger beanManagerCalls = new AtomicInteger();

        CountingBeanManager() {
            this(true);
        }

        CountingBeanManager(boolean registered) {
            if (registered) {
                CdiUtils.registerBeanManager(this);
            }
        }

        @Override
        public Set<Bean<?>> getBeans(Type beanType, Annotation... qualifiers) {
            beanManagerCalls.incrementAndGet();
            return Collections.emptySet();
        }

        @Override
        public Set<Bean<?>> getBeans(String name) {
            beanManagerCalls.incrementAndGet();
            return Collections.emptySet();
        }

        @Override
        public <X> Bean<? extends X> resolve(Set<Bean<? extends X>> beans) {
            return null;
        }

        @Override
        public Object getReference(Bean<?> bean, Type beanType, CreationalContext<?> ctx) {
            return null;
        }
    }
}
