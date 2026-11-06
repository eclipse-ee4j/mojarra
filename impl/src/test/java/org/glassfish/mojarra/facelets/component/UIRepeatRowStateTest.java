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

package org.glassfish.mojarra.facelets.component;

import static org.glassfish.mojarra.junit.Serialization.serializeAndDeserialize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import jakarta.faces.FactoryFinder;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.render.Renderer;
import jakarta.faces.validator.LengthValidator;
import jakarta.faces.validator.Validator;

import org.glassfish.mojarra.junit.JUnitFacesTestCaseBase;
import org.glassfish.mojarra.mock.MockRenderKit;
import org.glassfish.mojarra.mock.MockRenderKitFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A {@link UIRepeat} saves the per-row state of its inputs only for the rows where it is not at its default, and that state survives serializing the state of
 * the repeat. After the initial state is marked, the repeat saves only the properties changed since.
 */
class UIRepeatRowStateTest extends JUnitFacesTestCaseBase {

    private static final List<String> ROWS = List.of("first", "second");
    private static final String TYPED_ROW = ":1:";
    private static final String SUBMITTED_VALUE = "submitted";

    private final List<String> renderedStates = new ArrayList<>();
    private boolean submittingEveryRow;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        MockRenderKit renderKit = new MockRenderKit();
        renderKit.addRenderer(UIInput.COMPONENT_FAMILY, "jakarta.faces.Text", new Renderer<UIInput>() {

            @Override
            public void decode(FacesContext context, UIInput input) {
                if (submittingEveryRow) {
                    input.setSubmittedValue(SUBMITTED_VALUE);
                }
                else if (input.getClientId(context).contains(TYPED_ROW)) {
                    input.setSubmittedValue("typed");
                    input.setValue("local");
                    input.setValid(false);
                }
            }

            @Override
            public void encodeBegin(FacesContext context, UIInput input) {
                renderedStates.add(input.getSubmittedValue() + " " + input.getLocalValue() + " " + input.isLocalValueSet() + " " + input.isValid());
            }

        });
        renderKit.addRenderer(UIForm.COMPONENT_FAMILY, "jakarta.faces.Form", new Renderer<UIForm>() {

            @Override
            public void decode(FacesContext context, UIForm form) {
                form.setSubmitted(form.getClientId(context).contains(TYPED_ROW));
            }

            @Override
            public void encodeBegin(FacesContext context, UIForm form) {
                renderedStates.add("submitted " + form.isSubmitted());
            }

        });
        MockRenderKitFactory renderKitFactory = (MockRenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        renderKitFactory.addRenderKit(RenderKitFactory.HTML_BASIC_RENDER_KIT, renderKit);
    }

    @Test
    void untouchedRowsSaveNoPerRowState() throws Exception {
        UIRepeat repeat = createRepeat();
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        repeat.encodeChildren(facesContext);

        assertNull(((Object[]) repeat.saveState(facesContext))[2]);
    }

    /**
     * A row without saved state resets its inputs, so after a postback without any change neither the repeat nor its inputs hold a delta to save.
     */
    @Test
    void unchangedRepeatSavesNoDeltaState() throws Exception {
        UIRepeat repeat = createRepeat();
        markInitialState(repeat);
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        repeat.encodeChildren(facesContext);

        assertNull(repeat.saveState(facesContext));
        assertNull(repeat.getChildren().get(0).saveState(facesContext));
    }

    /**
     * A valid postback updates the model and leaves the inputs at their defaults, so it saves no delta state.
     */
    @Test
    void validPostbackSavesNoDeltaState() throws Exception {
        UIRepeat repeat = createRepeat();
        UIInput input = (UIInput) repeat.getChildren().get(0);
        input.setValueExpression(
            "value", application.getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{requestScope.entered}", Object.class)
        );
        markInitialState(repeat);
        submittingEveryRow = true;

        facesContext.setCurrentPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        repeat.processDecodes(facesContext);
        facesContext.setCurrentPhaseId(PhaseId.PROCESS_VALIDATIONS);
        repeat.processValidators(facesContext);
        facesContext.setCurrentPhaseId(PhaseId.UPDATE_MODEL_VALUES);
        repeat.processUpdates(facesContext);
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        repeat.encodeChildren(facesContext);

        assertEquals(List.of(), facesContext.getMessageList());
        assertNull(repeat.saveState(facesContext));
        assertNull(input.saveState(facesContext));
    }

    @Test
    void fullStateRoundTripsEveryProperty() throws Exception {
        UIRepeat repeat = new UIRepeat();
        List<String> value = List.of("a", "a", "b");
        repeat.setValue(value);
        repeat.setVar("item");
        repeat.setVarStatus("status");
        repeat.setBegin(1);
        repeat.setEnd(2);
        repeat.setStep(1);
        repeat.setSize(3);
        repeat.setRowStatePreserved(true);
        Object state = serializeAndDeserialize(repeat.saveState(facesContext));

        UIRepeat restored = new UIRepeat();
        restored.restoreState(facesContext, state);

        assertEquals(value, restored.getValue());
        assertEquals("item", restored.getVar());
        assertEquals("status", restored.getVarStatus());
        assertEquals(1, restored.getBegin());
        assertEquals(2, restored.getEnd());
        assertEquals(1, restored.getStep());
        assertEquals(3, restored.getSize());
        assertEquals(true, restored.isRowStatePreserved());
    }

    /**
     * A repeat without value iterates from begin to end. Its data model changes the begin and end properties to its own range, so the delta state must keep the
     * original bounds, else every postback iterates another range.
     */
    @Test
    void repeatWithoutValueIteratesTheSameRowsOnEveryPostback() throws Exception {
        assertEquals(List.of(2, 3, 4, 5, 2, 3, 4, 5, 2, 3, 4, 5), renderedIndexesOfThreeRequests(2, 5));
        assertEquals(List.of(0, 1, 2, 0, 1, 2, 0, 1, 2), renderedIndexesOfThreeRequests(null, 2));
    }

    @Test
    void valueWithNullRowsKeepsItsRowsInFullState() throws Exception {
        List<String> value = Arrays.asList("a", null, "b");
        UIRepeat repeat = new UIRepeat();
        repeat.setValue(value);

        UIRepeat restored = new UIRepeat();
        restored.restoreState(facesContext, serializeAndDeserialize(repeat.saveState(facesContext)));

        assertEquals(value, restored.getValue());
    }

    @Test
    void propertyChangedAfterInitialStateIsSavedInDeltaState() throws Exception {
        UIRepeat repeat = createRepeat();
        markInitialState(repeat);
        repeat.setVar("row");
        Object state = serializeAndDeserialize(repeat.saveState(facesContext));

        UIRepeat restored = createRepeat();
        markInitialState(restored);
        restored.restoreState(facesContext, state);

        assertEquals("row", restored.getVar());
    }

    @Test
    void perRowStateRoundTrips() throws Exception {
        UIRepeat repeat = createRepeat();
        facesContext.setCurrentPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        repeat.processDecodes(facesContext);
        Object state = serializeAndDeserialize(repeat.saveState(facesContext));

        UIRepeat restored = createRepeat();
        restored.restoreState(facesContext, state);
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "error", null));
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        restored.encodeChildren(facesContext);

        assertEquals(List.of("null null false true", "submitted false", "typed local true false", "submitted true"), renderedStates);
    }

    /**
     * A state which is kept without serializing it, as server side state saving does by default, must not change when a repeat restored from it saves the state
     * of its rows again, as it does in a request with error messages.
     */
    @Test
    void restoredRepeatDoesNotChangeTheSavedPerRowState() throws Exception {
        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "error", null));
        facesContext.setCurrentPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        UIRepeat repeat = createRepeat();
        repeat.processDecodes(facesContext);
        Object state = repeat.saveState(facesContext);

        submittingEveryRow = true;
        UIRepeat restored = createRepeat();
        restored.restoreState(facesContext, state);
        restored.processDecodes(facesContext);
        submittingEveryRow = false;
        UIRepeat restoredAgain = createRepeat();
        restoredAgain.restoreState(facesContext, state);
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        restoredAgain.encodeChildren(facesContext);

        assertEquals(List.of("null null false true", "submitted false", "typed local true false", "submitted true"), renderedStates);
    }

    private List<Object> renderedIndexesOfThreeRequests(Integer begin, Integer end) throws Exception {
        List<Object> rendered = new ArrayList<>();
        Object state = null;
        for (int request = 0; request < 3; request++) {
            UIRepeat repeat = createRepeatWithoutValue(begin, end, rendered);
            markInitialState(repeat);
            if (state != null) {
                repeat.restoreState(facesContext, state);
            }
            facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
            repeat.encodeChildren(facesContext);
            state = serializeAndDeserialize(repeat.saveState(facesContext));
        }
        return rendered;
    }

    private UIRepeat createRepeatWithoutValue(Integer begin, Integer end, List<Object> rendered) {
        UIViewRoot view = new UIViewRoot();
        view.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.setViewRoot(view);
        UIRepeat repeat = new UIRepeat();
        repeat.setId("repeat");
        repeat.setBegin(begin);
        repeat.setEnd(end);
        repeat.setVar("index");
        view.getChildren().add(repeat);
        UIOutput output = new UIOutput() {

            @Override
            public void encodeBegin(FacesContext context) {
                rendered.add(context.getExternalContext().getRequestMap().get("index"));
            }

        };
        output.setId("output");
        repeat.getChildren().add(output);
        return repeat;
    }

    @Test
    void preservedRowWithoutOwnStateDoesNotShowTheLocalValueOfThePreviousRow() {
        UIRepeat repeat = createRowStatePreservedRepeat();
        inRow(repeat, 0, input -> input.setValue("typed"));

        assertNull(valueInRow(repeat, 1, UIInput::getLocalValue));
    }

    @Test
    void preservedRowWithoutOwnStateDoesNotShowTheValidatorOfThePreviousRow() {
        UIRepeat repeat = createRowStatePreservedRepeat();
        inRow(repeat, 0, input -> input.addValidator(new LengthValidator(3)));

        assertEquals(0, validatorsInRow(repeat, 1));
        assertEquals(1, validatorsInRow(repeat, 0));
    }

    @Test
    void preservedRowKeepsTheRemovalOfAValidator() {
        UIRepeat repeat = createRowStatePreservedRepeat(new LengthValidator(3));
        inRow(repeat, 0, input -> input.removeValidator(input.getValidators()[0]));

        assertEquals(1, validatorsInRow(repeat, 1));
        assertEquals(0, validatorsInRow(repeat, 0));
        assertEquals(0, validatorsInRow(repeat, 0));
    }

    /**
     * A row whose children are back at their initial state restores that initial state, not the state which the row had before.
     */
    @Test
    void preservedRowRevertedToItsInitialStateRestoresTheInitialState() {
        UIRepeat repeat = createRowStatePreservedRepeat();
        inRow(repeat, 0, input -> input.getAttributes().put("foo", "bar"));
        inRow(repeat, 0, input -> input.getAttributes().remove("foo"));

        assertNull(valueInRow(repeat, 0, input -> input.getAttributes().get("foo")));
    }

    /**
     * A state which is kept without serializing it, as server side state saving does by default, must not change when a repeat restored from it changes the
     * state of a row.
     */
    @Test
    void restoredRepeatDoesNotChangeTheSavedPreservedRowStates() {
        UIRepeat repeat = createRowStatePreservedRepeat();
        inRow(repeat, 0, input -> input.setValue("typed"));
        Object state = repeat.saveState(facesContext);

        UIRepeat restored = createRowStatePreservedRepeat();
        restored.restoreState(facesContext, state);
        inRow(restored, 1, input -> input.setValue("changed"));
        UIRepeat restoredAgain = createRowStatePreservedRepeat();
        restoredAgain.restoreState(facesContext, state);

        assertNull(valueInRow(restoredAgain, 1, UIInput::getLocalValue));
        assertEquals("typed", valueInRow(restoredAgain, 0, UIInput::getLocalValue));
    }

    private void inRow(UIRepeat repeat, int row, Consumer<UIInput> action) {
        valueInRow(repeat, row, input -> {
            action.accept(input);
            return null;
        });
    }

    private int validatorsInRow(UIRepeat repeat, int row) {
        return valueInRow(repeat, row, input -> input.getValidators().length);
    }

    private <T> T valueInRow(UIRepeat repeat, int row, Function<UIInput, T> read) {
        List<T> result = new ArrayList<>(1);
        repeat.invokeOnComponent(facesContext, "form:repeat:" + row + ":input", (context, component) -> result.add(read.apply((UIInput) component)));
        return result.get(0);
    }

    private UIRepeat createRowStatePreservedRepeat(Validator<?>... validators) {
        UIRepeat repeat = createRepeat();
        repeat.setRowStatePreserved(true);
        UIInput input = (UIInput) repeat.getChildren().get(0);
        for (Validator<?> validator : validators) {
            input.addValidator(validator);
        }
        facesContext.getAttributes().put(StateManager.IS_BUILDING_INITIAL_STATE, true);
        try {
            markInitialStateOfTree(facesContext.getViewRoot());
        }
        finally {
            facesContext.getAttributes().remove(StateManager.IS_BUILDING_INITIAL_STATE);
        }
        return repeat;
    }

    private static void markInitialStateOfTree(UIComponent component) {
        component.markInitialState();
        for (UIComponent child : component.getChildren()) {
            markInitialStateOfTree(child);
        }
    }

    private static void markInitialState(UIRepeat repeat) {
        repeat.markInitialState();
        repeat.getChildren().forEach(UIComponent::markInitialState);
    }

    private UIRepeat createRepeat() {
        UIViewRoot view = new UIViewRoot();
        view.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.setViewRoot(view);
        UIForm form = new UIForm();
        form.setId("form");
        view.getChildren().add(form);
        UIRepeat repeat = new UIRepeat();
        repeat.setId("repeat");
        repeat.setValue(ROWS);
        form.getChildren().add(repeat);
        UIInput input = new UIInput();
        input.setId("input");
        repeat.getChildren().add(input);
        UIForm rowForm = new UIForm();
        rowForm.setId("rowForm");
        repeat.getChildren().add(rowForm);
        return repeat;
    }

}
