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
package com.sun.faces.application.view;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.sun.faces.context.StateContext;
import com.sun.faces.util.ComponentStruct;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.context.FacesContext;

class FaceletViewHandlingStrategyTest {

    /**
     * A view root that records whether its subtree was walked, and how the visit was configured. It does not walk:
     * these tests turn on whether a walk happens and how it is set up, not on what it finds.
     */
    private static final class RecordingViewRoot extends UIViewRoot {
        boolean visited;
        Set<VisitHint> hints;

        @Override
        public boolean visitTree(VisitContext context, VisitCallback callback) {
            visited = true;
            hints = context.getHints();
            return false;
        }
    }

    /**
     * Regression guard for the dynamic-action restore O(n) fix (PR #5783). A postback with no dynamic
     * add/remove must not build the {@code clientId -> component} index, i.e. must not walk the view.
     *
     * <p>Once the {@code AddRemoveListener} is installed (every partial-state-saving postback),
     * {@code getDynamicActions()} returns a non-null <em>empty</em> list and {@code pruneDynamicActions}
     * preserves that, so the guard in {@code reapplyDynamicActions} must be {@code !isEmpty(actions)} —
     * a bare {@code actions != null} walks the whole tree on every postback for nothing.
     */
    @Test
    void reapplyDynamicActionsSkipsTreeWalkWhenNoDynamicActions() throws Exception {
        RecordingViewRoot viewRoot = new RecordingViewRoot();
        FacesContext context = mock(FacesContext.class);
        when(context.getViewRoot()).thenReturn(viewRoot);

        StateContext stateContext = mock(StateContext.class);
        when(stateContext.getDynamicActions()).thenReturn(new ArrayList<ComponentStruct>());

        // Constructor-free instance: the real constructor wires up webConfig/factories we don't need here.
        FaceletViewHandlingStrategy strategy = mock(FaceletViewHandlingStrategy.class);
        Method reapplyDynamicActions = FaceletViewHandlingStrategy.class
                .getDeclaredMethod("reapplyDynamicActions", FacesContext.class);
        reapplyDynamicActions.setAccessible(true);

        try (MockedStatic<StateContext> mocked = mockStatic(StateContext.class)) {
            mocked.when(() -> StateContext.getStateContext(context)).thenReturn(stateContext);
            // Exercise the real prune so the test sees the genuine non-null empty list that caused the bug.
            mocked.when(() -> StateContext.pruneDynamicActions(any())).thenCallRealMethod();

            reapplyDynamicActions.invoke(strategy, context);
        }

        assertFalse(viewRoot.visited, "reapplyDynamicActions walked the view despite having no dynamic actions");
    }

    /**
     * Regression guard for #5864. When there are dynamic actions to replay the view does get indexed, and that
     * walk must skip iteration.
     *
     * <p>A visit without {@link VisitHint#SKIP_ITERATION} sets the row index on every iterating component it
     * passes, which components observe: a data table backed by a lazily loaded model fetches a page of data per
     * iteration, so the walk provokes a duplicate query on every postback which renders one. Indexing exists to
     * find components by client id and has no business iterating anything to do it.
     *
     * <p>The guard above is not a substitute for this one: it only holds when the view has no dynamic actions at
     * all, and every view relocates its component resources to the head, so in practice there is always an action
     * to replay and this walk always runs.
     *
     * <p>This lives here rather than in the TCK because the spec does not mandate it. The spec defines what
     * {@code SKIP_ITERATION} means for a visit, but says nothing about how the runtime may go looking for a
     * component by client id, and dynamic-action replay is entirely an implementation concern -- no spec type is
     * involved. The invariant is nonetheless real and cross-implementation: a component which loads data per row
     * cannot defend itself against a walk it never asked for, and both implementations honoured this until
     * PR #5783 stopped honouring it here. A TCK test would have to assert a row-access count, which the spec
     * leaves free, so it belongs to the implementation which made the promise.
     */
    @Test
    void reapplyDynamicActionsIndexesTheViewWithoutIteratingRows() throws Exception {
        RecordingViewRoot viewRoot = new RecordingViewRoot();
        FacesContext context = mock(FacesContext.class);
        when(context.getViewRoot()).thenReturn(viewRoot);

        ComponentStruct action = new ComponentStruct(ComponentStruct.ADD, null, "form", "form:added", "added");

        StateContext stateContext = mock(StateContext.class);
        when(stateContext.getDynamicActions()).thenReturn(new ArrayList<>(List.of(action)));
        when(stateContext.getDynamicComponents()).thenReturn(new HashMap<>());

        FaceletViewHandlingStrategy strategy = mock(FaceletViewHandlingStrategy.class);
        Method reapplyDynamicActions = FaceletViewHandlingStrategy.class
                .getDeclaredMethod("reapplyDynamicActions", FacesContext.class);
        reapplyDynamicActions.setAccessible(true);

        try (MockedStatic<StateContext> mocked = mockStatic(StateContext.class);
                MockedStatic<VisitContext> visitContexts = mockStatic(VisitContext.class)) {
            mocked.when(() -> StateContext.getStateContext(context)).thenReturn(stateContext);
            mocked.when(() -> StateContext.pruneDynamicActions(any())).thenCallRealMethod();
            // Hand back the hints the caller asked for: the real factory would need a CDI-backed FactoryFinder,
            // and the assertion is about what the visit is asked for, not about what the factory makes of it.
            visitContexts.when(() -> VisitContext.createVisitContext(any(), any(), any())).thenAnswer(invocation -> {
                VisitContext visitContext = mock(VisitContext.class);
                when(visitContext.getHints()).thenReturn(invocation.getArgument(2));
                return visitContext;
            });
            // The hintless overload, so that indexing via it fails this test on its assertion rather than on a NPE.
            visitContexts.when(() -> VisitContext.createVisitContext(any())).thenAnswer(invocation -> {
                VisitContext visitContext = mock(VisitContext.class);
                when(visitContext.getHints()).thenReturn(Set.of());
                return visitContext;
            });

            reapplyDynamicActions.invoke(strategy, context);
        }

        assertTrue(viewRoot.visited, "reapplyDynamicActions did not index the view despite having a dynamic action");
        assertTrue(viewRoot.hints.contains(VisitHint.SKIP_ITERATION),
                "reapplyDynamicActions indexed the view with a visit which iterates rows");
    }
}
