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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.sun.faces.context.StateContext;
import com.sun.faces.util.ComponentStruct;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.context.FacesContext;

class FaceletViewHandlingStrategyTest {

    /** A view root that records whether its subtree was walked. */
    private static final class RecordingViewRoot extends UIViewRoot {
        boolean visited;

        @Override
        public boolean visitTree(VisitContext context, VisitCallback callback) {
            visited = true;
            return super.visitTree(context, callback);
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
}
