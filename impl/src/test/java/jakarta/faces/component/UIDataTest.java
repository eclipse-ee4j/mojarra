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

import static com.sun.faces.junit.Serialization.deserialize;
import static com.sun.faces.junit.Serialization.serialize;
import static com.sun.faces.junit.Serialization.serializeAndDeserialize;
import static com.sun.faces.junit.Serialization.withSerialVersionUID;
import static jakarta.faces.component.NamingContainer.SEPARATOR_CHAR;
import static jakarta.faces.component.UINamingContainer.SEPARATOR_CHAR_PARAM_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InvalidClassException;
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

    private static final long OLDER_SAVED_STATE_SERIAL_VERSION_UID = 2920252657338389849L;

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
     * Without {@code rowStatePreserved}, there are no per-row delta states, and their slot in the saved state holds
     * {@code null}, which restores as no per-row delta states.
     */
    @Test
    public void emptyRowDeltaStatesAreSavedAsNull() {
        FacesContext context = mock(FacesContext.class);
        UIData data = new UIData();
        data.markInitialState();
        data.setFirst(4);

        Object[] state = (Object[]) data.saveState(context);
        assertNull(state[1]);

        UIData restored = new UIData();
        restored.markInitialState();
        restored.restoreState(context, state);
        assertEquals(4, restored.getFirst());
        assertNull(((Object[]) restored.saveState(context))[1]);
    }

    /**
     * A per-row state with every field at its default is restored at its defaults.
     */
    @Test
    public void savedStateAtItsDefaultsRoundTrips() throws Exception {
        SavedState restored = serializeAndDeserialize(new SavedState());

        assertNull(restored.getSubmittedValue());
        assertNull(restored.getValue());
        assertFalse(restored.isLocalValueSet());
        assertTrue(restored.isValid());
        assertFalse(restored.getSubmitted());
    }

    /**
     * A per-row state in the serialized form of an older version, which serializes every field, is not read, so the state
     * helpers expire the view.
     */
    @Test
    public void savedStateInTheSerializedFormOfAnOlderVersionIsNotRead() throws Exception {
        byte[] serialized = withSerialVersionUID(serialize(new SavedState()), SavedState.class.getName(), OLDER_SAVED_STATE_SERIAL_VERSION_UID);

        assertThrows(InvalidClassException.class, () -> deserialize(serialized));
    }

    /**
     * Every field of a per-row state which is not at its default is restored as saved.
     */
    @Test
    public void savedStateRoundTripsEveryField() throws Exception {
        SavedState state = new SavedState();
        state.setSubmittedValue("submitted");
        state.setValue("value");
        state.setLocalValueSet(true);
        state.setValid(false);
        state.setSubmitted(true);

        SavedState restored = serializeAndDeserialize(state);

        assertEquals("submitted", restored.getSubmittedValue());
        assertEquals("value", restored.getValue());
        assertTrue(restored.isLocalValueSet());
        assertFalse(restored.isValid());
        assertTrue(restored.getSubmitted());
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
