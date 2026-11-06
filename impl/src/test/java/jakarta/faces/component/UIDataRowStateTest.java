/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

import static com.sun.faces.junit.Serialization.serializeAndDeserialize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.faces.junit.JUnitFacesTestCaseBase;
import com.sun.faces.mock.MockRenderKit;
import com.sun.faces.mock.MockRenderKitFactory;

import jakarta.faces.FactoryFinder;
import jakarta.faces.application.StateManager;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.render.Renderer;

/**
 * The per-row state of the inputs of a {@link UIData} without {@code rowStatePreserved} survives serializing the state
 * of the table, and is discarded when the table is rendered without errors. With {@code rowStatePreserved}, the delta
 * states of the rows survive serializing the state of the table, and each restore gets its own copy of them.
 */
class UIDataRowStateTest extends JUnitFacesTestCaseBase {

    private static final List<String> ROWS = List.of("first", "second");

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        MockRenderKitFactory renderKitFactory = (MockRenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        MockRenderKit renderKit = new MockRenderKit();
        renderKit.addRenderer(UIData.COMPONENT_FAMILY, "jakarta.faces.Table", new Renderer() {
        });
        renderKitFactory.addRenderKit(RenderKitFactory.HTML_BASIC_RENDER_KIT, renderKit);
    }

    @Test
    void perRowStateRoundTrips() throws Exception {
        UIData table = createTable();
        table.setRowIndex(0);
        input(table).setValue("typed");
        table.setRowIndex(1);
        input(table).setSubmittedValue("submitted");
        input(table).setValid(false);
        table.setRowIndex(-1);

        Object state = serializeAndDeserialize(table.saveState(facesContext));
        UIData restored = createTable();
        restored.restoreState(facesContext, state);

        restored.setRowIndex(0);
        assertEquals("typed", input(restored).getLocalValue());
        assertTrue(input(restored).isLocalValueSet());
        assertNull(input(restored).getSubmittedValue());
        assertTrue(input(restored).isValid());
        restored.setRowIndex(1);
        assertNull(input(restored).getLocalValue());
        assertFalse(input(restored).isLocalValueSet());
        assertEquals("submitted", input(restored).getSubmittedValue());
        assertFalse(input(restored).isValid());
    }

    @Test
    void preservedRowStatesRoundTrip() throws Exception {
        UIData table = createRowStatePreservedTableWithTypedFirstRow();

        Object state = serializeAndDeserialize(table.saveState(facesContext));
        UIData restored = createRowStatePreservedTable();
        restored.restoreState(facesContext, state);

        restored.setRowIndex(0);
        assertEquals("typed", input(restored).getLocalValue());
        restored.setRowIndex(1);
        assertNull(input(restored).getLocalValue());
    }

    /**
     * A row without saved state of its own gets the initial state of its children, not the state of the row before.
     */
    @Test
    void preservedRowWithoutOwnStateDoesNotShowTheStateOfThePreviousRow() {
        UIData table = createRowStatePreservedTable();
        table.setRowIndex(0);
        input(table).setValue("typed");
        table.setRowIndex(1);

        assertNull(input(table).getLocalValue());
    }

    /**
     * The delta states of the rows are saved as a map of the saved states of the components of each row, which are
     * serializable as they are.
     */
    @Test
    void preservedRowStatesAreSavedAsMap() {
        UIData table = createRowStatePreservedTableWithTypedFirstRow();

        assertInstanceOf(Map.class, ((Object[]) table.saveState(facesContext))[1]);
    }

    /**
     * A state which is kept without serializing it, as server side state saving does by default, must not change when a
     * table restored from it changes the state of a row.
     */
    @Test
    void restoredTableDoesNotChangeTheSavedPreservedRowStates() {
        UIData table = createRowStatePreservedTableWithTypedFirstRow();
        Object state = table.saveState(facesContext);

        UIData restored = createRowStatePreservedTable();
        restored.restoreState(facesContext, state);
        restored.setRowIndex(1);
        input(restored).setValue("changed");
        restored.setRowIndex(-1);
        UIData restoredAgain = createRowStatePreservedTable();
        restoredAgain.restoreState(facesContext, state);

        restoredAgain.setRowIndex(1);
        assertNull(input(restoredAgain).getLocalValue());
    }

    /**
     * A table with {@code rowStatePreserved} nested in another one is one instance whose state is saved and restored once
     * per enclosing row, so the saved state of one enclosing row must not change when another enclosing row is restored.
     */
    @Test
    void nestedRowStatePreservedTableKeepsPreservedRowStatesPerEnclosingRow() {
        UIData outer = createNestedTable(true);
        UIData inner = inner(outer);

        outer.setRowIndex(0);
        inner.setRowIndex(0);
        input(inner).setValue("typed");
        inner.setRowIndex(-1);
        outer.setRowIndex(1);
        outer.setRowIndex(-1);

        outer.setRowIndex(0);
        Object[] innerState = assertInstanceOf(Object[].class, inner.saveState(facesContext));
        Map<?, ?> rowDeltaStates = assertInstanceOf(Map.class, innerState[1]);
        assertFalse(rowDeltaStates.isEmpty());
    }

    /**
     * A table nested in a table with {@code rowStatePreserved} is one instance whose per-row state is saved once per
     * enclosing row by the enclosing table. Discarding its per-row state on rendering without errors must reach every
     * enclosing row, so the enclosing table must not keep a copy of it.
     */
    @Test
    void nestedTableInRowStatePreservedTableDiscardsPerRowStateWithoutErrors() throws Exception {
        UIData outer = createNestedTable(false);
        UIData inner = inner(outer);
        UIInput input = input(inner);

        outer.setRowIndex(0);
        inner.setRowIndex(0);
        input.setSubmittedValue("submitted");
        inner.setRowIndex(-1);
        outer.setRowIndex(1);
        outer.setRowIndex(-1);

        outer.setRowIndex(0);
        inner.encodeBegin(facesContext);
        inner.setRowIndex(0);
        inner.setRowIndex(-1);
        outer.setRowIndex(1);
        outer.setRowIndex(-1);

        outer.setRowIndex(0);
        inner.setRowIndex(0);
        assertNull(input.getSubmittedValue());
    }

    private void markInitialState(UIComponent component) {
        facesContext.getAttributes().put(StateManager.IS_BUILDING_INITIAL_STATE, true);
        try {
            markInitialStateOfTree(component);
        } finally {
            facesContext.getAttributes().remove(StateManager.IS_BUILDING_INITIAL_STATE);
        }
    }

    private static void markInitialStateOfTree(UIComponent component) {
        component.markInitialState();
        for (UIComponent child : component.getChildren()) {
            markInitialStateOfTree(child);
        }
    }

    private UIData createRowStatePreservedTableWithTypedFirstRow() {
        UIData table = createRowStatePreservedTable();
        table.setRowIndex(0);
        input(table).setValue("typed");
        table.setRowIndex(-1);
        return table;
    }

    private UIData createRowStatePreservedTable() {
        UIData table = createTable();
        table.setRowStatePreserved(true);
        markInitialState(facesContext.getViewRoot());
        return table;
    }

    private UIData createNestedTable(boolean innerRowStatePreserved) {
        UIData outer = createTable();
        outer.setRowStatePreserved(true);
        UIData inner = new UIData();
        inner.setId("inner");
        inner.setValue(ROWS);
        inner.setRowStatePreserved(innerRowStatePreserved);
        UIColumn column = new UIColumn();
        column.setId("innerColumn");
        inner.getChildren().add(column);
        UIInput input = new UIInput();
        input.setId("innerInput");
        column.getChildren().add(input);
        outer.getChildren().get(0).getChildren().add(inner);
        markInitialState(facesContext.getViewRoot());
        return outer;
    }

    private static UIData inner(UIData outer) {
        return (UIData) outer.getChildren().get(0).getChildren().get(1);
    }

    private UIData createTable() {
        UIViewRoot view = new UIViewRoot();
        view.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.setViewRoot(view);
        UIForm form = new UIForm();
        form.setId("form");
        view.getChildren().add(form);
        UIData table = new UIData();
        table.setId("table");
        table.setValue(ROWS);
        form.getChildren().add(table);
        UIColumn column = new UIColumn();
        column.setId("column");
        table.getChildren().add(column);
        UIInput input = new UIInput();
        input.setId("input");
        column.getChildren().add(input);
        return table;
    }

    private static UIInput input(UIData table) {
        return (UIInput) table.getChildren().get(0).getChildren().get(0);
    }
}
