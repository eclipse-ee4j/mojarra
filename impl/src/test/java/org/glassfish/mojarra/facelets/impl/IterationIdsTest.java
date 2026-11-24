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
package org.glassfish.mojarra.facelets.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.IntPredicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Inside an iteration a tag generates its id from the row it is applied for, so that the same row gets the same id in every build of a view, whichever rows a
 * build skips, while an outermost iteration which applies a tag once per row generates the same ids as the plain per-application counter. A nested iteration
 * counts by its own row under the row path of the enclosing ones, and a tag applied twice for one row is told apart by a repeat number.
 */
class IterationIdsTest {

    private static final int SLOT = 0;
    private static final String BASE = "j_id1";

    private DefaultFacelet facelet;
    private int[] counters;
    private Map<String, Integer> ids;
    private IterationIds iterationIds;

    @BeforeEach
    void setUp() {
        facelet = new DefaultFacelet(mock(DefaultFaceletFactory.class), null, null, "test.xhtml", null);
        counters = new int[1];
        Map<DefaultFacelet, int[]> countersByFacelet = new IdentityHashMap<>();
        countersByFacelet.put(facelet, counters);
        ids = new HashMap<>();
        iterationIds = new IterationIds(countersByFacelet, ids, new HashMap<>());
    }

    @Test
    void everyRowAppliedCountsAsThePlainCounter() {
        counters[SLOT] = 2;

        assertEquals("2 3 4", iterate(3, row -> true));
        assertEquals(5, counters[SLOT], "the counter continues past the rows");
    }

    @Test
    void aSkippedRowLeavesTheCountsOfTheOtherRows() {
        assertEquals("1 3 5", iterate(6, row -> row % 2 == 1));
        assertEquals(6, counters[SLOT], "the counter continues past all rows, skipped ones included");
    }

    @Test
    void theSameRowCountsTheSameWhicheverRowsAreSkipped() {
        String all = iterate(4, row -> true);
        counters[SLOT] = 0;
        String some = iterate(4, row -> row >= 2);

        assertEquals("0 1 2 3", all);
        assertEquals("2 3", some);
    }

    @Test
    void aNestedIterationCountsByItsOwnRowUnderThePathOfTheEnclosingRows() {
        assertEquals("_r0:0 _r0:1 _r0:2 _r1:0 _r1:1 _r1:2", iterateNested(2, 3, outer -> true));
    }

    @Test
    void aNestedIterationKeepsItsIdsWhenTheEnclosingOneSkipsAWholeRow() {
        assertEquals("_r1:0 _r1:1 _r1:2", iterateNested(2, 3, outer -> outer == 1));
    }

    @Test
    void aTagAppliedTwiceForOneRowRepeatsItsCount() {
        iterationIds.start();
        iterationIds.row(0);

        assertEquals(0, iterationIds.count(facelet, counters, SLOT));
        assertEquals(0, iterationIds.repeat());
        assertEquals(0, iterationIds.count(facelet, counters, SLOT));
        assertEquals(1, iterationIds.repeat(), "the second application is told apart by its repeat");

        iterationIds.row(1);

        assertEquals(1, iterationIds.count(facelet, counters, SLOT));
        assertEquals(0, iterationIds.repeat(), "a next row starts without repeat");

        iterationIds.stop();
    }

    @Test
    void aTagAppliedTwiceForOneNestedRowRepeatsUnderItsPath() {
        iterationIds.start();
        iterationIds.row(1);
        iterationIds.start();
        iterationIds.row(0);

        iterationIds.count(facelet, counters, SLOT);
        assertEquals(0, iterationIds.count(facelet, counters, SLOT));
        assertEquals(1, iterationIds.repeat());
        assertEquals("_r1", iterationIds.path());

        iterationIds.stop();
        iterationIds.stop();
    }

    @Test
    void aNestedIterationLeavesTheCounterWhereItStood() {
        counters[SLOT] = 1;
        iterationIds.start();
        iterationIds.row(0);
        iterationIds.start();

        for (int row = 0; row < 3; row++) {
            iterationIds.row(row);
            iterationIds.count(facelet, counters, SLOT);
        }

        iterationIds.stop();

        assertEquals(1, counters[SLOT], "only the outermost iteration moves the counter");

        iterationIds.stop();
    }

    @Test
    void aTagIdInANestedIterationCountsByItsOwnRow() {
        ids.put(BASE, 4);
        iterationIds.start();
        iterationIds.row(2);
        iterationIds.start();
        iterationIds.row(1);

        assertEquals(1, iterationIds.count(BASE));
        assertEquals("_r2", iterationIds.path());

        iterationIds.stop();
        iterationIds.stop();

        assertEquals(4, ids.get(BASE), "only the outermost iteration moves the counter");
    }

    @Test
    void aTagIdCountsByRowFromWhereItsCounterStood() {
        ids.put(BASE, 1);
        iterationIds.start();

        for (int row = 0; row < 3; row++) {
            iterationIds.row(row);

            if (row != 1) {
                assertEquals(row + 2, iterationIds.count(BASE));
            }
        }

        iterationIds.stop();

        assertEquals(4, ids.get(BASE), "the counter holds the count of the last row");
    }

    private String iterateNested(int outerRows, int innerRows, IntPredicate outerApplied) {
        StringBuilder counts = new StringBuilder();
        iterationIds.start();

        for (int outer = 0; outer < outerRows; outer++) {
            iterationIds.row(outer);

            if (outerApplied.test(outer)) {
                iterationIds.start();

                for (int inner = 0; inner < innerRows; inner++) {
                    iterationIds.row(inner);
                    int count = iterationIds.count(facelet, counters, SLOT);
                    counts.append(iterationIds.path()).append(':').append(count).append(' ');
                }

                iterationIds.stop();
            }
        }

        iterationIds.stop();
        return counts.toString().trim();
    }

    private String iterate(int rows, IntPredicate applied) {
        StringBuilder counts = new StringBuilder();
        iterationIds.start();

        for (int row = 0; row < rows; row++) {
            iterationIds.row(row);

            if (applied.test(row)) {
                counts.append(iterationIds.count(facelet, counters, SLOT)).append(' ');
            }
        }

        iterationIds.stop();
        return counts.toString().trim();
    }

}
