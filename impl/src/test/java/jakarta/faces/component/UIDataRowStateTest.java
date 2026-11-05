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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

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
 * of the table, and is discarded when the table is rendered without errors.
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

    /**
     * A table nested in a table with {@code rowStatePreserved} is one instance whose per-row state is saved once per
     * enclosing row by the enclosing table. Discarding its per-row state on rendering without errors must reach every
     * enclosing row, so the enclosing table must not keep a copy of it.
     */
    @Test
    void nestedTableInRowStatePreservedTableDiscardsPerRowStateWithoutErrors() throws Exception {
        UIData outer = createTable();
        outer.setRowStatePreserved(true);
        UIData inner = new UIData();
        inner.setId("inner");
        inner.setValue(ROWS);
        UIColumn column = new UIColumn();
        column.setId("innerColumn");
        inner.getChildren().add(column);
        UIInput input = new UIInput();
        input.setId("innerInput");
        column.getChildren().add(input);
        outer.getChildren().get(0).getChildren().add(inner);
        markInitialState(facesContext.getViewRoot());

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
        for (UIComponent child : component.getChildren()) {
            markInitialStateOfTree(child);
        }
        component.markInitialState();
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
