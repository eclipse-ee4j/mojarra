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

package com.sun.faces.facelets.component;

import static com.sun.faces.junit.Serialization.deserialize;
import static com.sun.faces.junit.Serialization.serialize;
import static com.sun.faces.junit.Serialization.serializeAndDeserialize;
import static com.sun.faces.junit.Serialization.withSerialVersionUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InvalidClassException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.faces.junit.JUnitFacesTestCaseBase;
import com.sun.faces.mock.MockRenderKit;
import com.sun.faces.mock.MockRenderKitFactory;

import jakarta.faces.FactoryFinder;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UIInput;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;
import jakarta.faces.render.RenderKitFactory;
import jakarta.faces.render.Renderer;

/**
 * A {@link UIRepeat} saves the per-row state of its inputs only for the rows where it is not at its default, and that
 * state survives serializing the state of the repeat. After the initial state is marked, the repeat saves only the
 * properties changed since.
 */
class UIRepeatRowStateTest extends JUnitFacesTestCaseBase {

    private static final List<String> ROWS = List.of("first", "second");
    private static final String TYPED_ROW = ":1:";
    private static final String SUBMITTED_VALUE = "submitted";
    private static final String SAVED_STATE_CLASS_NAME = UIRepeat.class.getName() + "$SavedState";
    private static final long OLDER_SAVED_STATE_SERIAL_VERSION_UID = 2920252657338389849L;

    private final List<String> renderedStates = new ArrayList<>();
    private boolean submittingEveryRow;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        MockRenderKit renderKit = new MockRenderKit();
        renderKit.addRenderer(UIInput.COMPONENT_FAMILY, "jakarta.faces.Text", new Renderer() {

            @Override
            public void decode(FacesContext context, UIComponent component) {
                if (submittingEveryRow) {
                    ((UIInput) component).setSubmittedValue(SUBMITTED_VALUE);
                } else if (component.getClientId(context).contains(TYPED_ROW)) {
                    UIInput input = (UIInput) component;
                    input.setSubmittedValue("typed");
                    input.setValue("local");
                    input.setValid(false);
                }
            }

            @Override
            public void encodeBegin(FacesContext context, UIComponent component) {
                UIInput input = (UIInput) component;
                renderedStates.add(input.getSubmittedValue() + " " + input.getLocalValue() + " " + input.isLocalValueSet() + " " + input.isValid());
            }
        });
        renderKit.addRenderer(UIForm.COMPONENT_FAMILY, "jakarta.faces.Form", new Renderer() {

            @Override
            public void decode(FacesContext context, UIComponent component) {
                ((UIForm) component).setSubmitted(component.getClientId(context).contains(TYPED_ROW));
            }

            @Override
            public void encodeBegin(FacesContext context, UIComponent component) {
                renderedStates.add("submitted " + ((UIForm) component).isSubmitted());
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

        assertNull(((Object[]) repeat.saveState(facesContext))[1]);
    }

    /**
     * A row without saved state resets its inputs, so after a postback without any change neither the repeat nor its
     * inputs hold a delta to save.
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
        input.setValueExpression("value", application.getExpressionFactory().createValueExpression(facesContext.getELContext(), "#{requestScope.entered}", Object.class));
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
    }

    /**
     * A repeat without value iterates from begin to end. Its data model changes the begin and end properties to its own
     * range, so the delta state must keep the original bounds, else every postback iterates another range.
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
     * A state which is kept without serializing it, as server side state saving does by default, must not change when a
     * repeat restored from it saves the state of its rows again, as it does in a request with error messages.
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

    /**
     * A per-row state in the serialized form of an older version, which serializes every field, is not read, so the state
     * helpers expire the view.
     */
    @Test
    void perRowStateInTheSerializedFormOfAnOlderVersionIsNotRead() throws Exception {
        UIRepeat repeat = createRepeat();
        facesContext.setCurrentPhaseId(PhaseId.APPLY_REQUEST_VALUES);
        repeat.processDecodes(facesContext);
        byte[] serialized = withSerialVersionUID(serialize(repeat.saveState(facesContext)), SAVED_STATE_CLASS_NAME, OLDER_SAVED_STATE_SERIAL_VERSION_UID);

        assertThrows(InvalidClassException.class, () -> deserialize(serialized));
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
