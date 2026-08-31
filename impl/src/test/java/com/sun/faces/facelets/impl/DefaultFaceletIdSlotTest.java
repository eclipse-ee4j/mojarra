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

package com.sun.faces.facelets.impl;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

/**
 * A build keeps its per-tag unique-id counters in an {@code int[]} sized from {@link DefaultFacelet#getIdSlotCount()}
 * and indexed by the slot a tag reserved, and {@link FirstIdCache} caches the first id a tag generates against that
 * same slot. The slots a Facelet hands out therefore have to stay dense and one-to-one however many builds reserve
 * them at once: a slot at or past the count indexes past that array, and two tags sharing a slot share both a counter
 * and a cached id, so one of them generates the other's id.
 */
class DefaultFaceletIdSlotTest {

    private static final int TAGS = 512;

    @Test
    void aTagKeepsOneSlot() {
        DefaultFacelet facelet = facelet();

        assertEquals(facelet.getIdSlot("form"), facelet.getIdSlot("form"), "asking twice reserves once");
        assertNotEquals(facelet.getIdSlot("form"), facelet.getIdSlot("input"), "a second tag reserves its own");
        assertEquals(2, facelet.getIdSlotCount(), "and the count covers both");
    }

    @Test
    void concurrentReservationKeepsSlotsDenseAndUnique() throws Exception {
        DefaultFacelet facelet = facelet();
        int threads = Math.max(4, Runtime.getRuntime().availableProcessors());
        CyclicBarrier gate = new CyclicBarrier(threads);
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        try {
            List<Future<Integer>> builds = new ArrayList<>();

            for (int thread = 0; thread < threads; thread++) {
                int offset = thread * (TAGS / threads);
                builds.add(executor.submit(() -> {
                    gate.await();
                    int beyondTheCount = 0;

                    for (int tag = 0; tag < TAGS; tag++) {
                        if (facelet.getIdSlot("tag" + ((offset + tag) % TAGS)) >= facelet.getIdSlotCount()) {
                            beyondTheCount++;
                        }
                    }

                    return beyondTheCount;
                }));
            }

            int beyondTheCount = 0;

            for (Future<Integer> build : builds) {
                beyondTheCount += build.get();
            }

            Set<Integer> slots = IntStream.range(0, TAGS).mapToObj(tag -> facelet.getIdSlot("tag" + tag)).collect(toSet());

            assertEquals(0, beyondTheCount, "every slot stays within the count a build sizes its counters from");
            assertEquals(TAGS, slots.size(), "every tag has a slot to itself");
            assertEquals(TAGS, facelet.getIdSlotCount(), "and the count covers exactly the slots handed out");
        } finally {
            executor.shutdownNow();
        }
    }

    private static DefaultFacelet facelet() {
        return new DefaultFacelet(mock(DefaultFaceletFactory.class), null, null, "test.xhtml", null);
    }
}
