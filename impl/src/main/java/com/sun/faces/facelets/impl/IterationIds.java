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

import java.util.Arrays;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Counts, per row of an iteration, the unique ids of the tags in its body, so that a tag generates the same id for the
 * same row in every build of a view, whichever rows the build skips.
 * <p>
 * Outside an iteration a tag's n-th application generates its n-th id. Inside the outermost iteration, the application
 * for row r generates the id the counter of that tag stood at when the iteration first applied it, plus r, and the
 * iteration moves the counter past all its rows when it stops. An outermost iteration which applies a tag once per row
 * therefore generates exactly the ids the plain counter generates. Inside a nested iteration, the application for row r
 * generates count r, qualified by the {@link #path() row path} of the enclosing iterations, so that its ids depend on
 * neither the rows nor the inner iterations which the enclosing ones skip. A tag applied more than once for the same row
 * generates a {@link #repeat() repeat number} beside the row's id.
 * <p>
 * The prefix of a context created inside an iteration, as an include, a template, an insert, a composite component
 * or a tag file creates one, is counted the same way, per row, so that the ids of such a row do not depend on the rows
 * the build skips either.
 * <p>
 * One instance serves a whole build, shared by every context of the chain like the counters it advances.
 */
final class IterationIds {

    private static final int EPOCH = 0;
    private static final int START = 1;
    private static final int ROW = 2;
    private static final int REPEAT = 3;
    private static final int FIELDS = 4;

    private static final int NO_ROW = -1;

    private static final String ROW_PATH_SEPARATOR = "_r";

    private final Map<DefaultFacelet, int[]> counters;
    private final KeyedCounter<String> ids;
    private final KeyedCounter<Integer> prefixes;
    private final Map<DefaultFacelet, int[]> slotStates = new IdentityHashMap<>();

    private DefaultFacelet cachedOwner;
    private int[] cachedState;
    private Iteration iteration;
    private int epochs;
    private int repeat;

    /**
     * @param counters the per-slot counters of the build, by Facelet, each holding the next count of its slot
     * @param ids the per-tag-id counters of the build, each holding the last count its tag id generated
     * @param prefixes the per-prefix counters of the build, by hash of the Facelet hierarchy, each holding the last
     *        count its prefix generated
     */
    IterationIds(Map<DefaultFacelet, int[]> counters, Map<String, Integer> ids, Map<Integer, Integer> prefixes) {
        this.counters = counters;
        this.ids = new KeyedCounter<>(ids);
        this.prefixes = new KeyedCounter<>(prefixes);
    }

    /**
     * @return whether an iteration is applying its body
     */
    boolean isIterating() {
        return iteration != null;
    }

    /**
     * Starts an iteration, inside the current one if any.
     */
    void start() {
        iteration = new Iteration(iteration, ++epochs);
    }

    /**
     * Starts a row of the current iteration.
     *
     * @param row the zero based number of the row among those the iteration walks, ascending
     */
    void row(int row) {
        iteration.row = row;
        iteration.rows = row + 1;
    }

    /**
     * Stops the current iteration. An outermost one moves the counter of every tag and prefix it applied past all its
     * rows.
     */
    void stop() {
        Iteration stopped = iteration;
        iteration = stopped.outer;

        for (int i = 0; i < stopped.touchedSlots; i++) {
            int[] slotCounters = counters.get(stopped.owners[i]);
            int slot = stopped.slots[i];
            slotCounters[slot] = Math.max(slotCounters[slot], stopped.slotStarts[i] + stopped.rows);
        }

        for (int i = 0; i < stopped.touchedKeys; i++) {
            stopped.keyCounters[i].moveTo(stopped.keys[i], stopped.keyStarts[i] + stopped.rows - 1);
        }
    }

    /**
     * Returns the count the tag holding {@code slot} generates its id from for the current row.
     *
     * @param owner the Facelet the slot was reserved from
     * @param slotCounters the counters of {@code owner}, covering {@code slot}
     * @param slot the slot of the tag
     * @return the count to generate the id from
     */
    int count(DefaultFacelet owner, int[] slotCounters, int slot) {
        int[] state = slotState(owner, slotCounters.length);
        int offset = slot * FIELDS;

        if (state[offset + EPOCH] != iteration.epoch) {
            enter(state, offset, iteration.outer == null ? iteration.touch(owner, slot, slotCounters[slot]) : 0);
        }

        return count(state, offset);
    }

    /**
     * Returns the count {@code base} generates its id from for the current row.
     *
     * @param base the tag id
     * @return the count to generate the id from
     */
    int count(String base) {
        return count(ids, base);
    }

    /**
     * Returns the count the prefix of a context generates for the current row.
     *
     * @param hash the hash of the Facelet hierarchy of the context
     * @return the count to generate the prefix from
     */
    int countPrefix(Integer hash) {
        return count(prefixes, hash);
    }

    /**
     * @return how many times the tag counted last was applied for the current row before, which is 0 for its first
     *         application
     */
    int repeat() {
        return repeat;
    }

    /**
     * @return the row path of the iterations enclosing the current one, which is empty for the outermost iteration
     */
    String path() {
        return iteration.path;
    }

    private <K> int count(KeyedCounter<K> counter, K key) {
        int[] state = counter.state(key);

        if (state[EPOCH] != iteration.epoch) {
            enter(state, 0, iteration.outer == null ? iteration.touch(counter, key, counter.next(key)) : 0);
        }

        return count(state, 0);
    }

    private void enter(int[] state, int offset, int start) {
        state[offset + EPOCH] = iteration.epoch;
        state[offset + START] = start;
        state[offset + ROW] = NO_ROW;
    }

    private int count(int[] state, int offset) {
        if (state[offset + ROW] == iteration.row) {
            repeat = ++state[offset + REPEAT];
        } else {
            state[offset + ROW] = iteration.row;
            state[offset + REPEAT] = 0;
            repeat = 0;
        }

        return state[offset + START] + iteration.row;
    }

    private int[] slotState(DefaultFacelet owner, int slotCount) {
        int[] state = owner == cachedOwner ? cachedState : slotStates.get(owner);
        int length = slotCount * FIELDS;

        if (state == null || state.length < length) {
            state = state == null ? new int[length] : Arrays.copyOf(state, length);
            slotStates.put(owner, state);
        }

        cachedOwner = owner;
        cachedState = state;
        return state;
    }

    private static final class Iteration {

        private static final int INITIAL_CAPACITY = 4;

        private final Iteration outer;
        private final int epoch;
        private final String path;
        private int row;
        private int rows;

        private DefaultFacelet[] owners;
        private int[] slots;
        private int[] slotStarts;
        private int touchedSlots;

        private KeyedCounter<?>[] keyCounters;
        private Object[] keys;
        private int[] keyStarts;
        private int touchedKeys;

        private Iteration(Iteration outer, int epoch) {
            this.outer = outer;
            this.epoch = epoch;
            path = outer == null ? "" : outer.path + ROW_PATH_SEPARATOR + outer.row;
        }

        private int touch(DefaultFacelet owner, int slot, int start) {
            if (owners == null) {
                owners = new DefaultFacelet[INITIAL_CAPACITY];
                slots = new int[INITIAL_CAPACITY];
                slotStarts = new int[INITIAL_CAPACITY];
            } else if (touchedSlots == slots.length) {
                owners = Arrays.copyOf(owners, touchedSlots * 2);
                slots = Arrays.copyOf(slots, touchedSlots * 2);
                slotStarts = Arrays.copyOf(slotStarts, touchedSlots * 2);
            }

            owners[touchedSlots] = owner;
            slots[touchedSlots] = slot;
            slotStarts[touchedSlots++] = start;
            return start;
        }

        private <K> int touch(KeyedCounter<K> counter, K key, int start) {
            if (keys == null) {
                keyCounters = new KeyedCounter<?>[INITIAL_CAPACITY];
                keys = new Object[INITIAL_CAPACITY];
                keyStarts = new int[INITIAL_CAPACITY];
            } else if (touchedKeys == keys.length) {
                keyCounters = Arrays.copyOf(keyCounters, touchedKeys * 2);
                keys = Arrays.copyOf(keys, touchedKeys * 2);
                keyStarts = Arrays.copyOf(keyStarts, touchedKeys * 2);
            }

            keyCounters[touchedKeys] = counter;
            keys[touchedKeys] = key;
            keyStarts[touchedKeys++] = start;
            return start;
        }
    }

    /**
     * A counter per key which holds the last count each key generated, with the iteration state of each key.
     */
    private static final class KeyedCounter<K> {

        private final Map<K, Integer> lastCounts;
        private final Map<K, int[]> states = new HashMap<>();

        private KeyedCounter(Map<K, Integer> lastCounts) {
            this.lastCounts = lastCounts;
        }

        private int[] state(K key) {
            return states.computeIfAbsent(key, k -> new int[FIELDS]);
        }

        private int next(K key) {
            Integer last = lastCounts.get(key);
            return last == null ? 0 : last + 1;
        }

        @SuppressWarnings("unchecked")
        private void moveTo(Object key, int last) {
            Integer current = lastCounts.get(key);

            if (current == null || current < last) {
                lastCounts.put((K) key, last);
            }
        }
    }
}
