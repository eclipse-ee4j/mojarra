/*
 * Copyright (c) Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.test.perf;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import jakarta.faces.event.PhaseId;

/**
 * Thread-safe singleton accumulator for per-scenario per-phase timings.
 * One bucket per (scenario, phaseId) holds count, total/min/max nanos.
 */
public final class PerfStats {

    public static final PerfStats INSTANCE = new PerfStats();

    private final ConcurrentMap<String, ConcurrentMap<PhaseId, Bucket>> data = new ConcurrentHashMap<>();

    private PerfStats() {
    }

    public void record(String scenario, PhaseId phaseId, long nanos) {
        data.computeIfAbsent(scenario, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(phaseId, k -> new Bucket())
            .record(nanos);
    }

    public void reset() {
        data.clear();
    }

    /**
     * Deep snapshot ordered by scenario (alphabetical) then phase ordinal.
     */
    public Map<String, Map<PhaseId, Snapshot>> snapshot() {
        Map<String, Map<PhaseId, Snapshot>> result = new TreeMap<>();
        for (Map.Entry<String, ConcurrentMap<PhaseId, Bucket>> scenarioEntry : data.entrySet()) {
            Map<PhaseId, Snapshot> byPhase = new HashMap<>();
            for (Map.Entry<PhaseId, Bucket> phaseEntry : scenarioEntry.getValue().entrySet()) {
                byPhase.put(phaseEntry.getKey(), phaseEntry.getValue().snapshot());
            }
            result.put(scenarioEntry.getKey(), byPhase);
        }
        return result;
    }

    /**
     * Aggregate (across all scenarios) per phase.
     */
    public Map<PhaseId, Snapshot> totalsByPhase() {
        Map<PhaseId, long[]> agg = new HashMap<>();
        for (Map<PhaseId, Bucket> byPhase : data.values()) {
            for (Map.Entry<PhaseId, Bucket> e : byPhase.entrySet()) {
                Snapshot s = e.getValue().snapshot();
                long[] row = agg.computeIfAbsent(e.getKey(), k -> new long[] { 0, 0, Long.MAX_VALUE, 0 });
                row[0] += s.count;
                row[1] += s.totalNanos;
                row[2] = Math.min(row[2], s.minNanos);
                row[3] = Math.max(row[3], s.maxNanos);
            }
        }
        Map<PhaseId, Snapshot> result = new HashMap<>();
        for (Map.Entry<PhaseId, long[]> e : agg.entrySet()) {
            long[] r = e.getValue();
            result.put(e.getKey(), new Snapshot(r[0], r[1], r[2] == Long.MAX_VALUE ? 0 : r[2], r[3]));
        }
        return result;
    }

    private static final class Bucket {
        private final LongAdder count = new LongAdder();
        private final LongAdder totalNanos = new LongAdder();
        private final AtomicLong minNanos = new AtomicLong(Long.MAX_VALUE);
        private final AtomicLong maxNanos = new AtomicLong(0);

        void record(long nanos) {
            count.increment();
            totalNanos.add(nanos);
            minNanos.accumulateAndGet(nanos, Math::min);
            maxNanos.accumulateAndGet(nanos, Math::max);
        }

        Snapshot snapshot() {
            // Read order matches the write order in record() so we don't observe
            // count>=1 alongside still-sentinel min. We still clamp the sentinel
            // because a concurrent writer between increment() and accumulateAndGet()
            // would otherwise leave min == Long.MAX_VALUE.
            long c = count.sum();
            long total = totalNanos.sum();
            long min = minNanos.get();
            long max = maxNanos.get();
            return new Snapshot(c, total, min == Long.MAX_VALUE ? 0 : min, max);
        }
    }

    public static final class Snapshot {
        public final long count;
        public final long totalNanos;
        public final long minNanos;
        public final long maxNanos;

        Snapshot(long count, long totalNanos, long minNanos, long maxNanos) {
            this.count = count;
            this.totalNanos = totalNanos;
            this.minNanos = minNanos;
            this.maxNanos = maxNanos;
        }

        public long avgNanos() {
            return count == 0 ? 0 : totalNanos / count;
        }
    }
}
