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

import static com.sun.faces.RIConstants.DYNAMIC_CHILD_COUNT;
import static com.sun.faces.RIConstants.DYNAMIC_COMPONENT;
import static com.sun.faces.util.ComponentStruct.ADD;
import static com.sun.faces.util.ComponentStruct.APPEND;
import static com.sun.faces.util.ComponentStruct.REMOVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sun.faces.util.ComponentStruct;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIPanel;

/**
 * Tests {@link StateContext#pruneDynamicActions(List)}: dynamic add/remove actions are recorded append-only per
 * event and collapsed once here, so the per-client-id net effect and the first-occurrence ordering are those of the
 * actions applied one by one. Also tests how a restored dynamic add marks its child and parent, and which index it
 * restores the child at, as neither marker is saved with the state.
 */
class StateContextTest {

    private static final int BEYOND_CHILDREN = 5;

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
    void distinctClientIdsKeepRecordedOrder() {
        // parent 'p' added before child 'c'; cancelled 'x' drops out without disturbing the order of the rest
        assertNet(actions(add("p"), add("x"), add("c"), remove("x")), "ADD:p", "ADD:c");
    }

    @Test
    void survivingActionsKeepTheirRecordedPosition() {
        assertNet(actions(remove("a"), add("b"), add("a")), "REMOVE:a", "ADD:b", "ADD:a");
    }

    @Test
    void childReAddedUnderNewParentIsReplayedAfterThatParent() {
        assertNet(actions(add("panel1"), add("field"), remove("panel1"), remove("field"), add("panel2"), add("field")),
                "ADD:panel2", "ADD:field");
    }

    @Test
    void childOfReplacedPreExistingParentIsReplayedAfterTheNewParent() {
        assertNet(actions(remove("panel0"), remove("field"), add("panel1"), add("field")),
                "REMOVE:panel0", "REMOVE:field", "ADD:panel1", "ADD:field");
    }

    @Test
    void aDynamicallyAddedChildIsMarkedWithItsIndexAndItsParentAsHavingADynamicChild() {
        UIComponent child = childOf(new UIPanel());

        StateContext.markDynamicallyAdded(child, 3);

        assertEquals(3, child.getAttributes().get(DYNAMIC_COMPONENT));
        assertTrue(child.getParent().getAttributes().containsKey(DYNAMIC_CHILD_COUNT));
    }

    @Test
    void theIndexOfTheActionIsRestoredOverTheMarkerOfTheChild() {
        ComponentStruct struct = add("child");
        struct.setIndex(1);
        UIComponent child = new UIOutput();
        child.getAttributes().put(DYNAMIC_COMPONENT, 2);

        assertEquals(1, StateContext.indexToRestore(struct, child));
    }

    /**
     * State saved before the action carried the index restores the child at the index of its marker.
     */
    @Test
    void anActionWithoutIndexRestoresTheIndexOfTheMarkerOfTheChild() {
        UIComponent child = new UIOutput();
        child.getAttributes().put(DYNAMIC_COMPONENT, 2);

        assertEquals(2, StateContext.indexToRestore(add("child"), child));
    }

    @Test
    void anActionWithoutIndexForAChildWithoutMarkerAppendsTheChild() {
        assertEquals(APPEND, StateContext.indexToRestore(add("child"), new UIOutput()));
    }

    @Test
    void aDynamicChildIsAddedAtTheIndexOfItsActionAndMarkedWithIt() {
        UIComponent parent = parentOf(new UIOutput(), new UIOutput());
        UIComponent child = dynamicChild();
        ComponentStruct struct = add("child");
        struct.setIndex(1);

        StateContext.addDynamicChild(parent, child, struct);

        assertSame(child, parent.getChildren().get(1));
        assertEquals(1, child.getAttributes().get(DYNAMIC_COMPONENT));
        assertTrue(parent.getAttributes().containsKey(DYNAMIC_CHILD_COUNT));
    }

    @Test
    void aDynamicChildWhoseIndexIsBeyondTheChildrenIsAppendedAndMarkedWithItsPosition() {
        UIComponent parent = parentOf(new UIOutput());
        UIComponent child = dynamicChild();
        ComponentStruct struct = add("child");
        struct.setIndex(BEYOND_CHILDREN);

        StateContext.addDynamicChild(parent, child, struct);

        assertSame(child, parent.getChildren().get(1));
        assertEquals(1, child.getAttributes().get(DYNAMIC_COMPONENT));
    }

    @Test
    void aDynamicChildAtTheIndexOfItsActionIsInPlaceAndMarkedWithIt() {
        UIComponent child = new UIOutput();
        UIComponent parent = parentOf(new UIOutput(), child, new UIOutput());

        assertTrue(StateContext.markIfAlreadyInPlace(parent, child, addAt(1)));
        assertEquals(1, child.getAttributes().get(DYNAMIC_COMPONENT));
        assertTrue(parent.getAttributes().containsKey(DYNAMIC_CHILD_COUNT));
    }

    /**
     * A child inserted at index 0 after an earlier dynamic child was inserted there sits behind that child after the
     * facelet refresh, so it must be moved.
     */
    @Test
    void aDynamicChildUnderItsParentButAtAnotherIndexIsNotInPlace() {
        UIComponent earlier = new UIOutput();
        UIComponent child = new UIOutput();
        UIComponent parent = parentOf(earlier, child, new UIOutput());

        assertFalse(StateContext.markIfAlreadyInPlace(parent, child, addAt(0)));
        assertFalse(child.getAttributes().containsKey(DYNAMIC_COMPONENT));
    }

    @Test
    void aDynamicChildToAppendIsInPlaceOnlyWhenLast() {
        UIComponent child = new UIOutput();
        UIComponent first = new UIOutput();

        assertTrue(StateContext.markIfAlreadyInPlace(parentOf(new UIOutput(), child), child, addAt(APPEND)));
        assertFalse(StateContext.markIfAlreadyInPlace(parentOf(first, new UIOutput()), first, addAt(APPEND)));
    }

    @Test
    void aFacetOfTheParentIsNotInPlace() {
        UIComponent facet = new UIOutput();
        UIComponent parent = parentOf(new UIOutput());
        parent.getFacets().put("header", facet);

        assertFalse(StateContext.markIfAlreadyInPlace(parent, facet, addAt(0)));
        assertFalse(facet.getAttributes().containsKey(DYNAMIC_COMPONENT));
    }

    @Test
    void aDynamicChildWhoseIndexIsBeyondTheChildrenIsInPlaceOnlyWhenLast() {
        UIComponent child = new UIOutput();

        assertTrue(StateContext.markIfAlreadyInPlace(parentOf(new UIOutput(), child), child, addAt(BEYOND_CHILDREN)));
        assertEquals(1, child.getAttributes().get(DYNAMIC_COMPONENT));
    }

    @Test
    void aDynamicChildUnderAnotherParentIsNotInPlace() {
        UIComponent child = new UIOutput();
        parentOf(child);

        assertFalse(StateContext.markIfAlreadyInPlace(parentOf(new UIOutput()), child, addAt(0)));
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

    private static List<ComponentStruct> actions(ComponentStruct... structs) {
        return new ArrayList<>(List.of(structs));
    }

    private static ComponentStruct add(String clientId) {
        return new ComponentStruct(ADD, null, "parent", clientId, clientId);
    }

    /**
     * A child which is asked for its client id without a context, as the add resolves it.
     */
    private static UIComponent dynamicChild() {
        return new UIOutput() {
            @Override
            public String getClientId() {
                return getId();
            }
        };
    }

    private static UIComponent parentOf(UIComponent... children) {
        UIComponent parent = new UIPanel();
        for (UIComponent child : children) {
            parent.getChildren().add(child);
        }
        return parent;
    }

    private static UIComponent childOf(UIComponent parent) {
        UIComponent child = new UIOutput();
        child.setParent(parent);
        return child;
    }

    private static ComponentStruct addAt(int index) {
        ComponentStruct struct = add("child");
        struct.setIndex(index);
        return struct;
    }

    private static ComponentStruct remove(String clientId) {
        return new ComponentStruct(REMOVE, null, "parent", clientId, clientId);
    }
}
