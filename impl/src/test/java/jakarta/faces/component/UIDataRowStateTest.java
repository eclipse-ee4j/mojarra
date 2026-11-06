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

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.faces.junit.JUnitFacesTestCaseBase;
import com.sun.faces.mock.MockRenderKit;
import com.sun.faces.mock.MockRenderKitFactory;

import jakarta.faces.FactoryFinder;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.html.HtmlInputText;
import jakarta.faces.convert.IntegerConverter;
import jakarta.faces.convert.NumberConverter;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.PostValidateEvent;
import jakarta.faces.event.PreValidateEvent;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.SystemEventListener;
import jakarta.faces.event.ValueChangeEvent;
import jakarta.faces.event.ValueChangeListener;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.render.Renderer;
import jakarta.faces.validator.LengthValidator;

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

    /**
     * A state which is kept without serializing it, as server side state saving does by default, must not change when a
     * table restored from it changes or discards the state of a row.
     */
    @Test
    void restoredTableDoesNotChangeTheSavedPerRowState() {
        UIData table = createTable();
        table.setRowIndex(0);
        input(table).setSubmittedValue("first");
        table.setRowIndex(-1);
        Object state = table.saveState(facesContext);

        UIData restored = createTable();
        restored.restoreState(facesContext, state);
        restored.setRowIndex(0);
        input(restored).setSubmittedValue(null);
        restored.setRowIndex(1);
        input(restored).setSubmittedValue("changed");
        restored.setRowIndex(-1);
        UIData restoredAgain = createTable();
        restoredAgain.restoreState(facesContext, state);

        restoredAgain.setRowIndex(0);
        assertEquals("first", input(restoredAgain).getSubmittedValue());
        restoredAgain.setRowIndex(1);
        assertNull(input(restoredAgain).getSubmittedValue());
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
     * A row whose children are back at their initial state restores that initial state, not the state which the row had
     * before.
     */
    @Test
    void preservedRowRevertedToItsInitialStateRestoresTheInitialState() {
        UIData table = createRowStatePreservedTable();
        table.setRowIndex(0);
        input(table).getAttributes().put("foo", "bar");
        table.setRowIndex(-1);
        table.setRowIndex(0);
        input(table).getAttributes().remove("foo");
        table.setRowIndex(-1);

        table.setRowIndex(0);
        assertNull(input(table).getAttributes().get("foo"));
    }

    @Test
    void preservedRowWithoutOwnStateDoesNotShowTheValidatorOfThePreviousRow() {
        assertNotInNextRow(input -> input.addValidator(new LengthValidator(3)), input -> input.getValidators().length);
    }

    @Test
    void preservedRowWithoutOwnStateDoesNotShowTheValueChangeListenerOfThePreviousRow() {
        assertNotInNextRow(input -> input.addValueChangeListener(new Listener()), input -> input.getValueChangeListeners().length);
    }

    @Test
    void preservedRowWithoutOwnStateDoesNotShowTheSystemEventListenerOfThePreviousRow() {
        assertNotInNextRow(input -> input.subscribeToEvent(PostValidateEvent.class, new Listener()), input -> listenerCount(input, PostValidateEvent.class));
    }

    @Test
    void preservedRowWithoutOwnStateDoesNotShowTheConverterOfThePreviousRow() {
        assertNotInNextRow(input -> input.setConverter(new IntegerConverter()), input -> input.getConverter() == null ? 0 : 1);
    }

    @Test
    void preservedRowWithoutOwnStateDoesNotShowTheClientBehaviorOfThePreviousRow() {
        assertNotInNextRow(input -> input.addClientBehavior("change", new AjaxBehavior()), input -> input.getClientBehaviors().size());
    }

    @Test
    void preservedRowWithoutOwnStateDoesNotShowTheValueExpressionOfThePreviousRow() {
        assertNotInNextRow(input -> input.setValueExpression("title", application.getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{requestScope.title}", String.class)),
                input -> input.getValueExpression("title") == null ? 0 : 1);
    }

    @Test
    void preservedRowWithoutOwnStateDoesNotShowASystemEventListenerOfAnotherEventOfThePreviousRow() {
        HtmlInputText input = new HtmlInputText();
        input.subscribeToEvent(PostValidateEvent.class, new Listener());
        UIData table = createRowStatePreservedTable(input);
        table.setRowIndex(0);
        input.subscribeToEvent(PreValidateEvent.class, new Listener());

        table.setRowIndex(1);

        assertEquals(0, listenerCount(input, PreValidateEvent.class));
        assertEquals(1, listenerCount(input, PostValidateEvent.class));
    }

    /**
     * Entering a row restores a new converter instance, so the test changes the converter which the input has in the row,
     * not the one it was given before the initial state was marked.
     */
    @Test
    void preservedRowWithOnlyAConverterDeltaKeepsTheValidatorsOfTheInitialState() {
        HtmlInputText input = new HtmlInputText();
        input.addValidator(new LengthValidator(3));
        input.setConverter(new NumberConverter());
        UIData table = createRowStatePreservedTable(input);
        table.setRowIndex(0);
        ((NumberConverter) input.getConverter()).setPattern("#");
        table.setRowIndex(1);

        table.setRowIndex(0);

        assertEquals(1, input.getValidators().length);
    }

    @Test
    void preservedRowKeepsTheRemovalOfAValidator() throws Exception {
        assertRemovalStaysInItsRow(input -> input.addValidator(new LengthValidator(3)), input -> input.removeValidator(input.getValidators()[0]),
                input -> input.getValidators().length);
    }

    @Test
    void preservedRowKeepsTheRemovalOfAValueChangeListener() throws Exception {
        assertRemovalStaysInItsRow(input -> input.addValueChangeListener(new Listener()),
                input -> input.removeValueChangeListener(input.getValueChangeListeners()[0]), input -> input.getValueChangeListeners().length);
    }

    /**
     * A validator or a listener which was removed in a row of a table with {@code rowStatePreserved} stays removed in that
     * row, on every visit of the row and across saving and restoring the table, and stays in the other rows.
     */
    private void assertRemovalStaysInItsRow(Consumer<HtmlInputText> attach, Consumer<HtmlInputText> remove, ToIntFunction<HtmlInputText> count)
            throws Exception {
        HtmlInputText input = new HtmlInputText();
        attach.accept(input);
        UIData table = createRowStatePreservedTable(input);
        table.setRowIndex(0);
        remove.accept(input);
        table.setRowIndex(1);
        assertEquals(1, count.applyAsInt(input));
        table.setRowIndex(0);
        assertEquals(0, count.applyAsInt(input));
        table.setRowIndex(1);
        table.setRowIndex(0);
        assertEquals(0, count.applyAsInt(input));
        table.setRowIndex(-1);
        Object state = serializeAndDeserialize(table.saveState(facesContext));

        HtmlInputText restoredInput = new HtmlInputText();
        attach.accept(restoredInput);
        UIData restored = createRowStatePreservedTable(restoredInput);
        restored.restoreState(facesContext, state);
        restored.setRowIndex(0);

        assertEquals(0, count.applyAsInt(restoredInput));
    }

    private static int listenerCount(UIComponent component, Class<? extends SystemEvent> eventClass) {
        List<SystemEventListener> listeners = component.getListenersForEventClass(eventClass);
        return listeners == null ? 0 : listeners.size();
    }

    /**
     * An object which was attached to a child in a row of a table with {@code rowStatePreserved} belongs to that row. A row
     * without saved state of its own gets the initial state of its children, without that object.
     */
    private void assertNotInNextRow(Consumer<HtmlInputText> attach, ToIntFunction<HtmlInputText> count) {
        HtmlInputText input = new HtmlInputText();
        UIData table = createRowStatePreservedTable(input);
        table.setRowIndex(0);
        attach.accept(input);
        assertEquals(1, count.applyAsInt(input));

        table.setRowIndex(1);

        assertEquals(0, count.applyAsInt(input));
    }

    public static class Listener implements ValueChangeListener, ComponentSystemEventListener, Serializable {

        private static final long serialVersionUID = 1L;

        @Override
        public void processValueChange(ValueChangeEvent event) {
        }

        @Override
        public void processEvent(ComponentSystemEvent event) {
        }
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
     * enclosing row by the enclosing table. Discarding its per-row state on rendering without errors must reach the
     * enclosing row: an enclosing row whose descendants have no state left restores the initial state of its
     * descendants.
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
        return createRowStatePreservedTable(new UIInput());
    }

    private UIData createRowStatePreservedTable(UIInput input) {
        UIData table = createTable(input);
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
        return createTable(new UIInput());
    }

    private UIData createTable(UIInput input) {
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
        input.setId("input");
        column.getChildren().add(input);
        return table;
    }

    private static UIInput input(UIData table) {
        return (UIInput) table.getChildren().get(0).getChildren().get(0);
    }
}
