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
package com.sun.faces.context;

import static com.sun.faces.util.ComponentStruct.ADD;
import static com.sun.faces.util.ComponentStruct.REMOVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sun.faces.util.ComponentStruct;

/**
 * Tests {@link StateContext#pruneDynamicActions(List)}: dynamic add/remove actions are recorded append-only per
 * event and collapsed once here, so the per-client-id net effect and the first-occurrence ordering must match what
 * the former per-event prune produced.
 */
class StateContextTest {

    @Test
    void nullAndEmptyArePassedThrough() {
        assertNull(StateContext.pruneDynamicActions(null));
        assertTrue(StateContext.pruneDynamicActions(new ArrayList<>()).isEmpty());
    }

    @Test
    void singleAddSurvives() {
        assertNet(actions(add("a")), "ADD:a");
    }

    @Test
    void addThenRemoveCancels() {
        assertNet(actions(add("a"), remove("a")));
    }

    @Test
    void preExistingRemoveSurvives() {
        assertNet(actions(remove("a")), "REMOVE:a");
    }

    @Test
    void preExistingRemoveThenReAddKeepsBothInOrder() {
        assertNet(actions(remove("a"), add("a")), "REMOVE:a", "ADD:a");
    }

    @Test
    void removeAddRemoveCollapsesToRemove() {
        assertNet(actions(remove("a"), add("a"), remove("a")), "REMOVE:a");
    }

    @Test
    void removeAddRemoveAddKeepsBoth() {
        assertNet(actions(remove("a"), add("a"), remove("a"), add("a")), "REMOVE:a", "ADD:a");
    }

    @Test
    void addRemoveReAddCollapsesToAdd() {
        assertNet(actions(add("a"), remove("a"), add("a")), "ADD:a");
    }

    @Test
    void distinctClientIdsKeepFirstOccurrenceOrder() {
        // parent 'p' added before child 'c'; cancelled 'x' drops out without disturbing the order of the rest
        assertNet(actions(add("p"), add("x"), add("c"), remove("x")), "ADD:p", "ADD:c");
    }

    @Test
    void perClientIdEntriesAreAdjacentInFirstOccurrenceOrder() {
        // 'a' (re-added) keeps its original slot ahead of 'b', and emits REMOVE before ADD
        assertNet(actions(remove("a"), add("b"), add("a")), "REMOVE:a", "ADD:a", "ADD:b");
    }

    // ------------------------------------------------------------------ helpers

    private static void assertNet(List<ComponentStruct> raw, String... expected) {
        List<ComponentStruct> pruned = StateContext.pruneDynamicActions(raw);
        assertEquals(List.of(expected), pruned.stream().map(s -> s.getAction() + ":" + s.getClientId()).toList());
    }

    private static List<ComponentStruct> actions(ComponentStruct... structs) {
        return new ArrayList<>(List.of(structs));
    }

    private static ComponentStruct add(String clientId) {
        return new ComponentStruct(ADD, null, "parent", clientId, clientId);
    }

    private static ComponentStruct remove(String clientId) {
        return new ComponentStruct(REMOVE, null, "parent", clientId, clientId);
    }
}
