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
package org.glassfish.mojarra.context;

import static java.util.stream.Collectors.toList;
import static org.glassfish.mojarra.util.ComponentStruct.ADD;
import static org.glassfish.mojarra.util.ComponentStruct.REMOVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.glassfish.mojarra.util.ComponentStruct;
import org.junit.jupiter.api.Test;

/**
 * Tests the static helpers of {@link StateContext}: {@link StateContext#pruneDynamicActions(List)} and
 * {@link StateContext#stripIterationIndex(char, String)}.
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

    @Test
    void allAddsIsReturnedAsIsWithoutBuildingTheNetMap() {
        // The common bulk-add postback (all ADDs) must short-circuit: the raw list is already its own pruned
        // form, so pruneDynamicActions returns it unchanged rather than rebuilding it through the per-client-id
        // LinkedHashMap on every save and reapply.
        List<ComponentStruct> raw = actions(add("a"), add("b"), add("c"));
        assertSame(raw, StateContext.pruneDynamicActions(raw));
    }

    @Test
    void allRemovesIsReturnedAsIsWithoutBuildingTheNetMap() {
        // Symmetric to the bulk-add case: a bulk clear (all REMOVEs) is homogeneous, nothing to collapse.
        List<ComponentStruct> raw = actions(remove("a"), remove("b"), remove("c"));
        assertSame(raw, StateContext.pruneDynamicActions(raw));
    }

    @Test
    void aSingleRemoveStillEngagesTheNetCollapse() {
        // Sanity that the short-circuit does not swallow the collapse path: with any REMOVE present the
        // add/remove coalescing still runs (here the trailing REMOVE cancels the earlier ADD of 'a').
        assertNet(actions(add("a"), add("b"), remove("a")), "ADD:b");
    }

    private static void assertNet(List<ComponentStruct> raw, String... expected) {
        List<ComponentStruct> pruned = StateContext.pruneDynamicActions(raw);
        assertEquals(List.of(expected), pruned.stream().map(s -> s.getAction() + ":" + s.getClientId()).collect(toList()));
    }

    @Test
    void stripIterationIndexRemovesRowScopedSegments() {
        // The bug: an add recorded inside a row carries the row index.
        assertEquals("form:table:group", StateContext.stripIterationIndex(':', "form:table:1:group"));
        assertEquals("form:table:group", StateContext.stripIterationIndex(':', "form:table:0:group"));
        assertEquals("form:table:group", StateContext.stripIterationIndex(':', "form:table:10:group"));
        assertEquals("form:table:group:added0", StateContext.stripIterationIndex(':', "form:table:1:group:added0"));
        // Nested tables contribute one index each.
        assertEquals("form:outer:inner:input", StateContext.stripIterationIndex(':', "form:outer:3:inner:12:input"));
        // Leading and trailing indices: the latter must not leave a dangling separator.
        assertEquals("button", StateContext.stripIterationIndex(':', "3:button"));
        assertEquals("form:table", StateContext.stripIterationIndex(':', "form:table:1"));
    }

    @Test
    void stripIterationIndexLeavesComponentIdsAlone() {
        // A component id can never be an iteration index: UIComponent.setId requires a leading letter or underscore.
        assertEquals("form:_1:group", StateContext.stripIterationIndex(':', "form:_1:group"));
        assertEquals("form:a1:group", StateContext.stripIterationIndex(':', "form:a1:group"));
    }

    @Test
    void stripIterationIndexReturnsSameInstanceWhenNothingToStrip() {
        // No allocation and no copy for the common client id which carries no iteration index.
        String clientId = "form:group";
        assertSame(clientId, StateContext.stripIterationIndex(':', clientId));
        assertSame("", StateContext.stripIterationIndex(':', ""));
    }

    @Test
    void stripIterationIndexHonorsSeparatorChar() {
        assertEquals("form_table_group", StateContext.stripIterationIndex('_', "form_table_1_group"));
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
