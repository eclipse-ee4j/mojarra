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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.IntPredicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.el.ELContext;
import jakarta.faces.context.FacesContext;

/**
 * Inside an iteration a context generates ids of the form {@code prefix_base[_count][_r<row>...][-repeat]}, the same
 * from the tag id and from the tag's slot, and all distinct within the build: the count of the row, the row path of the
 * enclosing iterations, and the repeat number of a tag applied more than once for the same row. A context which an
 * include creates inside an iteration gets the prefix of its row, whichever rows the build skips.
 */
class DefaultFaceletContextIterationIdTest {

    private static final String SLOTTED = "s";
    private static final String UNSLOTTED = "u";
    private static final int ROWS = 3;

    private FacesContext faces;
    private DefaultFacelet facelet;
    private DefaultFaceletContext context;
    private int slot;
    private String prefix;

    @BeforeEach
    void setUp() {
        faces = mock(FacesContext.class);
        when(faces.getAttributes()).thenReturn(new HashMap<>());
        when(faces.getELContext()).thenReturn(mock(ELContext.class));
        facelet = new DefaultFacelet(mock(DefaultFaceletFactory.class), null, null, "test.xhtml", null);
        context = new DefaultFaceletContext(faces, facelet);
        slot = facelet.getIdSlot(SLOTTED);
        prefix = context.generateUniqueId("p").replace("_p", "");
    }

    @Test
    void theFirstRowOfAnOutermostIterationGeneratesThePlainFirstId() {
        context.startIteration();
        context.startIterationRow(0);

        assertEquals(prefix + "_s", slotted());
        assertEquals(prefix + "_u", unslotted());

        context.stopIteration();
    }

    @Test
    void rowsPathsAndRepeatsMakeTheIdsOfOneBuildDistinct() {
        List<String> generated = new ArrayList<>();
        context.startIteration();

        for (int outer = 0; outer < 2; outer++) {
            context.startIterationRow(outer);
            generated.add(slotted());
            generated.add(slotted());
            generated.add(unslotted());
            context.startIteration();

            for (int inner = 0; inner < 2; inner++) {
                context.startIterationRow(inner);
                generated.add(slotted());
                generated.add(slotted());
                generated.add(unslotted());
            }

            context.stopIteration();
        }

        context.stopIteration();
        generated.add(slotted());
        generated.add(unslotted());

        assertEquals(List.of(
                prefix + "_s", prefix + "_s-1", prefix + "_u",
                prefix + "_s_r0", prefix + "_s_r0-1", prefix + "_u_r0",
                prefix + "_s_1_r0", prefix + "_s_1_r0-1", prefix + "_u_1_r0",
                prefix + "_s_1", prefix + "_s_1-1", prefix + "_u_1",
                prefix + "_s_r1", prefix + "_s_r1-1", prefix + "_u_r1",
                prefix + "_s_1_r1", prefix + "_s_1_r1-1", prefix + "_u_1_r1",
                prefix + "_s_2", prefix + "_u_2"), generated);
        assertEquals(generated.size(), new HashSet<>(generated).size(), "all distinct");
    }

    @Test
    void anIncludedRowGetsThePrefixOfItsRowWhicheverRowsAreSkipped() {
        DefaultFacelet included = new DefaultFacelet(mock(DefaultFaceletFactory.class), null, null, "row.xhtml", null);
        List<String> allRows = includedIds(included, context, row -> true);
        List<String> lastRow = includedIds(included, new DefaultFaceletContext(faces, facelet), row -> row == ROWS - 1);

        assertEquals(ROWS, new HashSet<>(allRows).size(), "every row its own prefix");
        assertEquals(List.of(allRows.get(ROWS - 1)), lastRow);
    }

    @Test
    void anIncludeAfterAnOutermostIterationCountsPastAllItsRows() {
        DefaultFacelet included = new DefaultFacelet(mock(DefaultFaceletFactory.class), null, null, "row.xhtml", null);
        List<String> iterated = includedIds(included, context, row -> true);
        iterated.add(new DefaultFaceletContext(context, included).generateUniqueId(UNSLOTTED));

        DefaultFaceletContext plain = new DefaultFaceletContext(faces, facelet);
        List<String> expected = new ArrayList<>();
        for (int include = 0; include < ROWS + 1; include++) {
            expected.add(new DefaultFaceletContext(plain, included).generateUniqueId(UNSLOTTED));
        }

        assertEquals(expected, iterated, "an iteration including once per row counts as the plain includes");
    }

    private List<String> includedIds(DefaultFacelet included, DefaultFaceletContext outer, IntPredicate applied) {
        List<String> generated = new ArrayList<>();
        outer.startIteration();

        for (int row = 0; row < ROWS; row++) {
            outer.startIterationRow(row);

            if (applied.test(row)) {
                generated.add(new DefaultFaceletContext(outer, included).generateUniqueId(UNSLOTTED));
            }
        }

        outer.stopIteration();
        return generated;
    }

    private String slotted() {
        return context.generateUniqueId(SLOTTED, facelet, slot);
    }

    private String unslotted() {
        return context.generateUniqueId(UNSLOTTED);
    }
}
