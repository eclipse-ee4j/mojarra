/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.faces.component;

import static jakarta.faces.component.NamingContainer.SEPARATOR_CHAR;
import static jakarta.faces.component.UINamingContainer.SEPARATOR_CHAR_PARAM_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKit;

/**
 * @author Manfred Riem (manfred.riem@oracle.com)
 */
public class UIDataTest {

    private static final ContextCallback NO_OP_CALLBACK = (context, target) -> {
    };

    /**
     * Test partial state saving.
     */
    @Test
    public void testSaveState() {
        FacesContext context = mock(FacesContext.class);
        UIData data = new UIData();
        data.markInitialState();
        assertNull(data.saveState(context));
    }

    /**
     * Test full state saving.
     */
    @Test
    public void testSaveState2() {
        FacesContext context = mock(FacesContext.class);
        UIData data = new UIData();
        assertNotNull(data.saveState(context));
    }

    /**
     * Partial state saving returns null only when nothing changed, so a property that did change still has to come
     * back as state.
     */
    @Test
    public void testSaveState3() {
        FacesContext context = mock(FacesContext.class);
        UIData data = new UIData();
        data.markInitialState();
        data.setFirst(4);
        assertNotNull(data.saveState(context));
    }

    /**
     * The row index is not component state. Every phase leaves it at -1 before state is saved and no page can set it,
     * so holding it on a field rather than in the state helper is what lets each {@code getClientId} under this table
     * read it without a map lookup.
     */
    @Test
    public void theRowIndexIsNotSavedState() {
        FacesContext context = mock(FacesContext.class);
        UIData data = new UIData();
        data.markInitialState();
        data.setRowIndex(4);
        assertNull(data.saveState(context));
    }

    @Test
    public void testInvokeOnComponentMustNotCallSetRowIndexIfNotTouched() throws Exception {
        FacesContext context = mock(FacesContext.class);
        ExternalContext externalContext = mock(ExternalContext.class);
        when(context.getExternalContext()).thenReturn(externalContext);
        RenderKit renderKit = mock(RenderKit.class);
        when(context.getRenderKit()).thenReturn(renderKit);
        context.getAttributes().put(SEPARATOR_CHAR_PARAM_NAME, SEPARATOR_CHAR);

        UIData data = new UIData() {
            @Override
            public void setRowIndex(int rowIndex) {
                context.getAttributes().put("setRowIndexCalled", true);
            }
        };

        data.setId("data");

        data.invokeOnComponent(context, "differentId", (contextInLambda, target) -> {
        });

        assertNull(context.getAttributes().get("setRowIndexCalled"));
    }

    /**
     * A client ID which merely starts with the client ID of the table, without a separator in between, belongs to a
     * sibling component and must not be parsed for a row index.
     */
    @Test
    public void invokeOnComponentIgnoresSiblingWithSameIdPrefix() {
        for (String clientId : List.of("usersearchtext", "users2", "usersX:btn", "usersX:3:btn", "usersX:panel:btn")) {
            List<Integer> rowIndexes = new ArrayList<>();
            UIData data = createRowIndexRecordingData(rowIndexes);

            assertFalse(data.invokeOnComponent(mockFacesContext(), clientId, NO_OP_CALLBACK), clientId);
            assertTrue(rowIndexes.isEmpty(), clientId);
        }
    }

    /**
     * A client ID of a component inside the table still positions the table on the row index in it. The row is not
     * available in this stub, so the callback is never reached and the row index is restored afterwards.
     */
    @Test
    public void invokeOnComponentPositionsRowOfDescendant() {
        List<Integer> rowIndexes = new ArrayList<>();
        UIData data = createRowIndexRecordingData(rowIndexes);

        assertFalse(data.invokeOnComponent(mockFacesContext(), "users:3:input", NO_OP_CALLBACK));
        assertEquals(List.of(3, -1), rowIndexes);
    }

    /**
     * The client ID of a table nested in a row of another table includes the row index of the outer table. The same
     * separator rule applies after that prefix.
     */
    @Test
    public void invokeOnComponentOfNestedDataIgnoresSiblingWithSameIdPrefix() {
        List<Integer> rowIndexes = new ArrayList<>();
        UIData data = createRowIndexRecordingData(rowIndexes);
        UIData outer = new UIData() {
            @Override
            public String getContainerClientId(FacesContext context) {
                return "outer:3";
            }
        };
        outer.getChildren().add(data);
        FacesContext context = mockFacesContext();

        assertFalse(data.invokeOnComponent(context, "outer:3:usersX:1:btn", NO_OP_CALLBACK));
        assertTrue(rowIndexes.isEmpty());

        assertFalse(data.invokeOnComponent(context, "outer:3:users:1:btn", NO_OP_CALLBACK));
        assertEquals(List.of(1, -1), rowIndexes);
    }

    private static UIData createRowIndexRecordingData(List<Integer> rowIndexes) {
        UIData data = new UIData() {
            @Override
            public void setRowIndex(int rowIndex) {
                rowIndexes.add(rowIndex);
            }
        };

        data.setId("users");
        return data;
    }

    private static FacesContext mockFacesContext() {
        FacesContext context = mock(FacesContext.class);
        when(context.getExternalContext()).thenReturn(mock(ExternalContext.class));
        when(context.getRenderKit()).thenReturn(mock(RenderKit.class));
        return context;
    }
}
