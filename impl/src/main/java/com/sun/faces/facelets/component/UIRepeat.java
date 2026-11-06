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

package com.sun.faces.facelets.component;

import static com.sun.faces.cdi.CdiUtils.createDataModel;
import static com.sun.faces.facelets.tag.faces.ComponentSupport.restoreFullDescendantComponentDeltaStates;
import static com.sun.faces.facelets.tag.faces.ComponentSupport.restoreFullDescendantComponentStates;
import static com.sun.faces.facelets.tag.faces.ComponentSupport.restoreTransientDescendantComponentStates;
import static com.sun.faces.facelets.tag.faces.ComponentSupport.saveDescendantComponentStates;
import static com.sun.faces.facelets.tag.faces.ComponentSupport.saveDescendantInitialComponentStates;
import static com.sun.faces.util.Util.isNestedInIterator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamField;
import java.io.Serializable;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sun.faces.facelets.tag.IterationStatus;

import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.application.StateManager;
import jakarta.faces.component.ContextCallback;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.NamingContainer;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIForm;
import jakarta.faces.component.UINamingContainer;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.component.visit.VisitHint;
import jakarta.faces.component.visit.VisitResult;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.FacesListener;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PostValidateEvent;
import jakarta.faces.event.PreValidateEvent;
import jakarta.faces.model.ArrayDataModel;
import jakarta.faces.model.DataModel;
import jakarta.faces.model.IterableDataModel;
import jakarta.faces.model.ListDataModel;
import jakarta.faces.model.ResultSetDataModel;
import jakarta.faces.model.ScalarDataModel;
import jakarta.faces.render.Renderer;

public class UIRepeat extends UINamingContainer {

    public static final String COMPONENT_TYPE = "facelets.ui.Repeat";

    public static final String COMPONENT_FAMILY = "facelets";

    private final static DataModel EMPTY_MODEL = new ListDataModel<>(Collections.emptyList());

    /**
     * Properties that are tracked by state saving.
     */
    enum PropertyKeys {
        value, var, varStatus, begin, end, step, size, rowStatePreserved, originalBegin, originalEnd
    }

    private transient DataModel model;

    private int index = -1;

    private Object initialDescendantFullComponentState = null;
    private Map<String, Object> preservedRowStates = new HashMap<>();
    private Map<String, Object> transientRowStates = new HashMap<>();

    public UIRepeat() {
        setRendererType("facelets.ui.Repeat");
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public void setEnd(Integer end) {
        getStateHelper().put(PropertyKeys.end, end);
    }

    public Integer getEnd() {
        return (Integer) getStateHelper().eval(PropertyKeys.end);
    }

    public void setSize(Integer size) {
        getStateHelper().put(PropertyKeys.size, size);
    }

    public Integer getSize() {
        return (Integer) getStateHelper().eval(PropertyKeys.size);
    }

    public void setOffset(Integer offset) {
        setBegin(offset);
    }

    public Integer getOffset() {
        Integer begin = (Integer) getStateHelper().get(PropertyKeys.begin);
        if (begin != null) {
            return begin;
        }
        ValueExpression ve = getValueExpression("offset");
        if (ve != null) {
            return (Integer) ve.getValue(getFacesContext().getELContext());
        }
        return null;

    }

    public void setBegin(Integer begin) {
        getStateHelper().put(PropertyKeys.begin, begin);
    }

    public Integer getBegin() {
        return (Integer) getStateHelper().eval(PropertyKeys.begin);
    }

    public void setStep(Integer step) {
        getStateHelper().put(PropertyKeys.step, step);
    }

    public Integer getStep() {
        return (Integer) getStateHelper().eval(PropertyKeys.step);
    }

    /**
     * <p class="changed_added_4_1">
     * Boolean flag directing how the per-row component state of {@link EditableValueHolder} children should be handled across requests on the same view.
     * If set to {@code true}, then state for {@link EditableValueHolder} components in each row will not be discarded before a new row is rendered.
     * If not specified, the default value is {@code false}.
     * </p>
     * <p>
     * This attribute should be set only when the current repeat component contains {@link UIForm} children which in turn contains {@link EditableValueHolder} children.
     * This will only work reliably when the data model of the current repeat component does not change across requests on the same view by e.g. sorting, adding or removing rows.
     * The alternative is to use <code>c:forEach</code> instead.
     * </p>
     *
     * @param rowStatePreserved Whether to preserve row state while rendering
     * @since 4.1
     */
    public void setRowStatePreserved(boolean rowStatePreserved) {
        getStateHelper().put(PropertyKeys.rowStatePreserved, rowStatePreserved);
    }

    /**
     * <p class="changed_added_4_1">
     * Returns whether row state is preserved as per {@link #setRowStatePreserved(boolean)}.
     * </p>
     *
     * @return Whether row state is preserved.
     * @since 4.1
     */
    public boolean isRowStatePreserved() {
        Boolean rowStatePreserved = (Boolean) getStateHelper().eval(PropertyKeys.rowStatePreserved);
        return rowStatePreserved != null && rowStatePreserved;
    }

    /**
     * A value expression cannot name the variable this component exports, since the body of the tag resolves that name
     * as it is written. One is therefore rejected rather than accepted and then ignored.
     *
     * @throws IllegalArgumentException when the given name is {@code var} or {@code varStatus}
     */
    @Override
    public void setValueExpression(String name, ValueExpression binding) {
        if ("var".equals(name) || "varStatus".equals(name)) {
            throw new IllegalArgumentException(name);
        }

        super.setValueExpression(name, binding);
    }

    public String getVar() {
        return (String) getStateHelper().get(PropertyKeys.var);
    }

    public void setVar(String var) {
        getStateHelper().put(PropertyKeys.var, var);
    }

    public String getVarStatus() {
        return (String) getStateHelper().get(PropertyKeys.varStatus);
    }

    public void setVarStatus(String varStatus) {
        getStateHelper().put(PropertyKeys.varStatus, varStatus);
    }

    private void resetDataModel(FacesContext context) {
        if (isNestedInIterator(context, this)) {
            this.setDataModel(null);
        }
    }

    private void setDataModel(DataModel model) {
        // noinspection unchecked
        this.model = model;
    }

    /**
     * The bound which the first data model of this repeat was built with. The data model of a repeat without value
     * changes the begin and end properties to its own range.
     */
    private Integer getOriginalBound(PropertyKeys originalKey, PropertyKeys key) {
        Integer bound = (Integer) getStateHelper().get(originalKey);
        if (bound == null) {
            bound = (Integer) getStateHelper().eval(key);
            if (bound != null) {
                getStateHelper().put(originalKey, bound);
            }
        }
        return bound;
    }

    private DataModel getDataModel() {
        if (model == null) {
            Object val = getValue();
            if (val == null) {
                Integer begin = getOriginalBound(PropertyKeys.originalBegin, PropertyKeys.begin);
                Integer end = getOriginalBound(PropertyKeys.originalEnd, PropertyKeys.end);

                if (end == null) {
                    if (begin == null) {
                        model = EMPTY_MODEL;
                    } else {
                        throw new IllegalArgumentException("end");
                    }
                } else {
                    int b = begin == null ? 0 : begin;
                    int e = end;
                    int d = b < e ? 1 : b > e ? -1 : 0;
                    int s = Math.abs(e - b) + 1;
                    Integer[] array = new Integer[s];

                    for (int i = 0; i < s; i++) {
                        array[i] = b + i * d;
                    }

                    model = new ArrayDataModel<>(array);
                    setBegin(0);
                    setEnd(s);
                }
            } else if (val instanceof DataModel) {
                // noinspection unchecked
                model = (DataModel<Object>) val;
            } else if (val instanceof List) {
                // noinspection unchecked
                model = new ListDataModel<>((List<Object>) val);
            } else if (Object[].class.isAssignableFrom(val.getClass())) {
                model = new ArrayDataModel<>((Object[]) val);
            } else if (val instanceof ResultSet) {
                model = new ResultSetDataModel((ResultSet) val);
            } else if (val instanceof Iterable) {
                model = new IterableDataModel<>((Iterable<?>) val);
            } else if (val instanceof Map) {
                model = new IterableDataModel<>(((Map<?, ?>) val).entrySet());
            } else {
                DataModel<?> dataModel = createDataModel(val.getClass());
                if (dataModel != null) {
                    dataModel.setWrappedData(val);
                    model = dataModel;
                } else {
                    model = new ScalarDataModel<>(val);
                }

            }
        }
        return model;
    }

    public Object getValue() {
        return getStateHelper().eval(PropertyKeys.value);
    }

    public void setValue(Object value) {
        getStateHelper().put(PropertyKeys.value, value);
    }

    private transient StringBuilder buffer;

    private StringBuilder getBuffer() {
        if (buffer == null) {
            buffer = new StringBuilder();
        }
        buffer.setLength(0);
        return buffer;
    }

    @Override
    public String getClientId(FacesContext faces) {
        String id = super.getClientId(faces);
        if (index >= 0) {
            id = getBuffer().append(id).append(getSeparatorChar(faces)).append(index).toString();
        }
        return id;
    }

    private transient Object origValueOfVar;
    private transient Object origValueOfVarStatus;

    private void captureOrigValue(FacesContext ctx) {
        String var = getVar();
        String varStatus = getVarStatus();
        if (var != null || varStatus != null) {
            Map<String, Object> attrs = ctx.getExternalContext().getRequestMap();
            if (var != null) {
                origValueOfVar = attrs.get(var);
            }
            if (varStatus != null) {
                origValueOfVarStatus = attrs.get(varStatus);
            }
        }
    }

    private void restoreOrigValue(FacesContext ctx) {
        String var = getVar();
        String varStatus = getVarStatus();
        if (var != null || varStatus != null) {
            Map<String, Object> attrs = ctx.getExternalContext().getRequestMap();
            if (var != null) {
                if (origValueOfVar != null) {
                    attrs.put(var, origValueOfVar);
                } else {
                    attrs.remove(var);
                }
            }
            if (varStatus != null) {
                if (origValueOfVarStatus != null) {
                    attrs.put(varStatus, origValueOfVarStatus);
                } else {
                    attrs.remove(varStatus);
                }
            }
        }
    }

    // Per-row transient state of the stateful descendants, keyed by this repeat's own row-scoped clientId. The value
    // array is positionally aligned to iterationStatefulList (the EditableValueHolder descendants in deterministic
    // tree order), so save and restore index by position instead of recomputing a clientId and hashing it per child
    // per row. The array length is guarded on restore so a tree-shape change between requests degrades to a reset per
    // slot. The key must be the clientId rather than the bare index: a nested repeat is one component instance reused
    // across the enclosing iteration's rows, so an index alone identifies the same key under every enclosing row and
    // the rows would overwrite each other's state. This repeat's own clientId already carries the enclosing rows'
    // indices, and is computed once per index change rather than once per child per row.
    private Map<String, SavedState[]> childState;

    /**
     * Per-iteration cache of the descendants the per-row save/restore walks act on, collected by
     * {@link #ensureIterationLists()} on first need and cleared when iteration ends
     * ({@link #setIndex} to {@code -1}) so the next iteration re-evaluates the tree shape.
     * {@code iterationResetList} is every descendant -- all need their cached clientId reset each
     * row; {@code iterationStatefulList} is the {@link EditableValueHolder} subset carrying per-row
     * state. {@code null} means not yet collected. Iterating these flat lists replaces the previous
     * per-row recursive tree walk; a read-only repeat has an empty stateful list, so save is a no-op.
     */
    private transient List<UIComponent> iterationResetList;
    private transient List<UIComponent> iterationStatefulList;

    private Map<String, SavedState[]> getChildState() {
        if (childState == null) {
            childState = new HashMap<>();
        }
        return childState;
    }

    /**
     * The key this repeat's per-row child state is stored under: its own clientId, which carries the row indices of
     * any enclosing iterating components, plus its current index. Nesting therefore yields distinct keys where the
     * bare index would collide.
     */
    private String getRowStateKey(FacesContext ctx) {
        // Deliberately super.getClientId(): this repeat's own getClientId() appends the index and
        // rebuilds the id through a buffer on every call. The inherited one is cached in the clientId
        // field and invalidated by the iterationResetList walk whenever an enclosing row changes, so
        // it yields the enclosing rows' indices without rebuilding them per row.
        return super.getClientId(ctx) + NamingContainer.SEPARATOR_CHAR + index;
    }

    private void clearChildState() {
        childState = null;
    }

    /**
     * Collects, once per iteration, the descendants the per-row save/restore walks act on, so those
     * walks become flat-list iterations instead of recursive tree traversals re-done every row.
     * {@code iterationResetList} is every descendant (each needs its cached clientId reset per row);
     * {@code iterationStatefulList} is the {@link EditableValueHolder} subset. Cleared on
     * {@code setIndex(-1)} so the next iteration re-evaluates the tree shape.
     */
    private void ensureIterationLists() {
        if (iterationResetList != null) {
            return;
        }
        List<UIComponent> reset = new ArrayList<>();
        List<UIComponent> stateful = new ArrayList<>();
        if (getChildCount() > 0) {
            for (UIComponent kid : getChildren()) {
                collectIterationState(kid, reset, stateful);
            }
        }
        iterationResetList = reset;
        iterationStatefulList = stateful;
    }

    private void collectIterationState(UIComponent component, List<UIComponent> reset, List<UIComponent> stateful) {
        reset.add(component);
        if (component instanceof EditableValueHolder || component instanceof UIForm) {
            // UIForm carries per-row state too: with one form per row only the submitted one may
            // validate and update its model, so its submitted flag must be restored per row rather
            // than leaking whichever row was decoded last. Mirrors UIData#collectIterationState.
            stateful.add(component);
        }
        Iterator<UIComponent> itr = component.getFacetsAndChildren();
        while (itr.hasNext()) {
            collectIterationState(itr.next(), reset, stateful);
        }
    }

    private void saveChildState(FacesContext ctx) {
        ensureIterationLists();
        int count = iterationStatefulList.size();
        if (count == 0) {
            return;
        }
        // Only slots with delta state hold a SavedState, and only a row with such a slot is kept, so the saved state
        // holds no rows which restore the same as a reset.
        String rowStateKey = getRowStateKey(ctx);
        SavedState[] rowState = null;
        for (int i = 0; i < count; i++) {
            UIComponent c = iterationStatefulList.get(i);
            if (!c.isTransient()) {
                SavedState ss = new SavedState();
                if (c instanceof EditableValueHolder) {
                    ss.populate((EditableValueHolder) c);
                } else {
                    ss.populate((UIForm) c);
                }
                if (ss.hasDeltaState()) {
                    if (rowState == null) {
                        rowState = new SavedState[count];
                    }
                    rowState[i] = ss;
                }
            }
        }
        if (rowState != null) {
            getChildState().put(rowStateKey, rowState);
        } else {
            removeChildState(ctx);
        }
    }

    private void removeChildState(FacesContext ctx) {
        if (childState != null) {
            childState.remove(getRowStateKey(ctx));
            if (childState.isEmpty()) {
                childState = null;
            }
        }
    }

    private void restoreChildState(FacesContext ctx) {
        // The clientId reset must run for every descendant (not just stateful ones): each row needs a
        // row-specific clientId (e.g. repeat:N:foo), else rows render duplicate id="...". The descendant
        // set is constant within an iteration, so it is collected once and walked here as a flat list.
        ensureIterationLists();
        for (UIComponent component : iterationResetList) {
            component.setId(component.getId()); // forces the cached clientId to be recomputed
        }
        int count = iterationStatefulList.size();
        if (count == 0) {
            return;
        }
        // Positional restore: index into this row's saved-state array rather than rebuilding/hashing a clientId per
        // child. A null array (row never saved) or short array (tree shape changed) falls back to a reset per slot.
        SavedState[] rowState = childState == null ? null : childState.get(getRowStateKey(ctx));
        for (int i = 0; i < count; i++) {
            SavedState state = rowState != null && i < rowState.length ? rowState[i] : null;
            restoreComponentState(iterationStatefulList.get(i), state);
        }
    }

    /**
     * Restore the per-row transient state of a single stateful descendant. A {@code null} state resets the component to
     * its un-submitted defaults.
     */
    private void restoreComponentState(UIComponent component, SavedState state) {
        if (component instanceof EditableValueHolder) {
            EditableValueHolder input = (EditableValueHolder) component;
            if (state == null) {
                input.resetValue();
            } else {
                state.apply(input);
            }
        } else if (component instanceof UIForm) {
            ((UIForm) component).setSubmitted(state != null && state.isSubmitted());
        }
    }

    private boolean keepSaved(FacesContext context) {

        return hasErrorMessages(context) || isNestedInIterator(context, this);

    }

    private boolean hasErrorMessages(FacesContext context) {

        FacesMessage.Severity sev = context.getMaximumSeverity();
        return sev != null && FacesMessage.SEVERITY_ERROR.compareTo(sev) <= 0;

    }

    private void setIndex(FacesContext ctx, int index) {
        if (isRowStatePreserved() && initialDescendantFullComponentState != null) {
            setRowIndexWithRowStatePreserved(ctx, index);
        } else {
            setRowIndexWithoutRowStatePreserved(ctx, index);
        }
    }

    private void setRowIndexWithRowStatePreserved(FacesContext ctx, int index) {
        if (index < -1) {
            throw new IllegalArgumentException("index is less than -1");
        }

        if (this.index == index) {
            return;
        }

        if (initialDescendantFullComponentState != null) {
            // Just save the row
            String rowKey = getContainerClientId(ctx);
            Map<String, Object> sm = saveDescendantComponentStates(ctx, null, getChildren().iterator(), UIComponent::saveState, false);
            if (sm != null && !sm.isEmpty()) {
                preservedRowStates.put(rowKey, sm);
            } else {
                preservedRowStates.remove(rowKey);
            }
            if (this.index != -1) {
                transientRowStates.put(rowKey, saveDescendantComponentStates(ctx, null, getChildren().iterator(), UIComponent::saveTransientState, false));
            }
        }

        // Update to the new row index
        this.index = index;
        DataModel localModel = getDataModel();
        localModel.setRowIndex(index);

        // if rowIndex is -1, clear the cache
        if (this.index == -1) {
            setDataModel(null);
        }

        // Clear or expose the current row data as a request scope attribute
        String var = getVar();
        if (this.index != -1 && var != null && localModel.isRowAvailable()) {
            Map<String, Object> attrs = ctx.getExternalContext().getRequestMap();
            attrs.put(var, localModel.getRowData());
        }

        if (initialDescendantFullComponentState != null) {
            Object rowState = preservedRowStates.get(getContainerClientId(ctx));
            if (rowState == null) {
                // Restore as original
                restoreFullDescendantComponentStates(ctx, getChildren().iterator(), initialDescendantFullComponentState, false);
            } else {
                // Restore first original and then delta
                restoreFullDescendantComponentDeltaStates(ctx, getChildren().iterator(), rowState, initialDescendantFullComponentState, false);
            }
            if (this.index == -1) {
                restoreTransientDescendantComponentStates(ctx, getChildren().iterator(), null, false);
            } else {
                rowState = transientRowStates.get(getContainerClientId(ctx));
                if (rowState == null) {
                    restoreTransientDescendantComponentStates(ctx, getChildren().iterator(), null, false);
                } else {
                    restoreTransientDescendantComponentStates(ctx, getChildren().iterator(), (Map<String, Object>) rowState, false);
                }
            }
        }
    }

    private void setRowIndexWithoutRowStatePreserved(FacesContext ctx, int index) {

        // Force the DataModel to build before any short-circuit. For a value-less
        // ui:repeat with begin/end attributes, the first getDataModel() call has the
        // side effect of rewriting begin/end into the model's [0, size) form (see
        // getDataModel() ArrayDataModel branch); the caller of setIndex(faces, -1) at
        // the start of process() relies on that side effect to fire before process()
        // reads getBegin()/getEnd() into its local iteration bounds.
        DataModel localModel = getDataModel();

        // No-op short-circuit when the index is already current. Common when callers
        // restore the iterator to -1 at end-of-iteration on a repeat that was already at -1.
        if (this.index == index) {
            return;
        }

        // save child state
        if (this.index != -1 && localModel.isRowAvailable()) {
            this.saveChildState(ctx);
        } else if (this.index >= 0 && childState != null) {
            this.removeChildState(ctx);
        }

        this.index = index;
        localModel.setRowIndex(index);

        String var = getVar();
        if (this.index != -1 && var != null && localModel.isRowAvailable()) {
            Map<String, Object> attrs = ctx.getExternalContext().getRequestMap();
            attrs.put(var, localModel.getRowData());
        }

        // restore child state
        if (this.index != -1 && localModel.isRowAvailable()) {
            this.restoreChildState(ctx);
        }

        // Iteration ended: clear the per-iteration stateful-descendants cache so the next
        // iteration re-evaluates the tree shape (children can change between iterations).
        // Cleared after restoreChildState() so the in-iteration walks still hit the cache.
        if (this.index == -1) {
            iterationResetList = null;
            iterationStatefulList = null;
        }
    }

    private void updateIterationStatus(FacesContext ctx, IterationStatus status) {
        String varStatus = getVarStatus();
        if (varStatus != null) {
            Map<String, Object> attrs = ctx.getExternalContext().getRequestMap();
            attrs.put(varStatus, status);
        }
    }

    private boolean isIndexAvailable() {
        return getDataModel().isRowAvailable();
    }

    public void process(FacesContext faces, PhaseId phase) {

        // stop if not rendered
        if (!isRendered()) {
            return;
        }

        // clear datamodel
        resetDataModel(faces);

        // We must clear the child state if we just entered the Render Phase, and there are no error messages
        if (PhaseId.RENDER_RESPONSE.equals(phase) && !hasErrorMessages(faces)) {
            clearChildState();
        }

        // reset index
        captureOrigValue(faces);
        setIndex(faces, -1);

        try {
            // has children
            if (getChildCount() > 0) {
                Iterator itr;
                UIComponent c;

                Integer begin = getBegin();
                Integer step = getStep();
                Integer end = getEnd();
                Integer offset = getOffset();

                if (null != offset && offset > 0) {
                    begin = offset;
                }

                Integer size = getSize();
                if (null != size) {
                    end = size;
                }

                // grab renderer
                String rendererType = getRendererType();
                Renderer renderer = null;
                if (rendererType != null) {
                    renderer = getRenderer(faces);
                }

                int rowCount = getDataModel().getRowCount();
                int i = begin != null ? begin : 0;
                int e = end != null ? end : rowCount;
                int s = step != null ? step : 1;
                validateIterationControlValues(rowCount, i, e);
                if (null != size && size > 0) {
                    e = size - 1;
                }

                setIndex(faces, i);
                updateIterationStatus(faces, new IterationStatus(true, i + s > e || rowCount == 1, i, begin, end, step));
                while (i <= e && isIndexAvailable()) {

                    if (PhaseId.RENDER_RESPONSE.equals(phase) && renderer != null) {
                        renderer.encodeChildren(faces, this);
                    } else {
                        itr = getChildren().iterator();
                        while (itr.hasNext()) {
                            c = (UIComponent) itr.next();
                            if (PhaseId.APPLY_REQUEST_VALUES.equals(phase)) {
                                c.processDecodes(faces);
                            } else if (PhaseId.PROCESS_VALIDATIONS.equals(phase)) {
                                c.processValidators(faces);
                            } else if (PhaseId.UPDATE_MODEL_VALUES.equals(phase)) {
                                c.processUpdates(faces);
                            } else if (PhaseId.RENDER_RESPONSE.equals(phase)) {
                                c.encodeAll(faces);
                            }
                        }
                    }
                    i += s;
                    setIndex(faces, i);
                    updateIterationStatus(faces, new IterationStatus(false, i + s >= e, i, begin, end, step));
                }
            }
        } catch (IOException e) {
            throw new FacesException(e);
        } finally {
            setIndex(faces, -1);
            restoreOrigValue(faces);
        }

        /*
         * Once rendering is done we need to make sure the child components are not still having client ids that use an index.
         */
        if (PhaseId.RENDER_RESPONSE.equals(phase)) {
            resetClientIds(this);
        }
    }

    private void resetClientIds(UIComponent component) {
        Iterator<UIComponent> iterator = component.getFacetsAndChildren();
        while (iterator.hasNext()) {
            UIComponent child = iterator.next();
            resetClientIds(child);
            child.setId(child.getId());
        }
    }

    @Override
    public boolean invokeOnComponent(FacesContext faces, String clientId, ContextCallback callback) throws FacesException {
        String id = super.getClientId(faces);
        if (clientId.equals(id)) {
            pushComponentToEL(faces, this);
            try {
                callback.invokeContextCallback(faces, this);
            } finally {
                popComponentFromEL(faces);
            }
            return true;
        } else if (clientId.startsWith(id)) {
            int prevIndex = index;
            int idxStart = clientId.indexOf(getSeparatorChar(faces), id.length());
            if (idxStart != -1 && Character.isDigit(clientId.charAt(idxStart + 1))) {
                int idxEnd = clientId.indexOf(getSeparatorChar(faces), idxStart + 1);
                if (idxEnd != -1) {
                    int newIndex = Integer.parseInt(clientId.substring(idxStart + 1, idxEnd));
                    boolean found = false;
                    try {
                        captureOrigValue(faces);
                        setIndex(faces, newIndex);
                        if (isIndexAvailable()) {
                            found = super.invokeOnComponent(faces, clientId, callback);
                        }
                    } finally {
                        setIndex(faces, prevIndex);
                        restoreOrigValue(faces);
                    }
                    return found;
                }
            } else {
                return super.invokeOnComponent(faces, clientId, callback);
            }
        }
        return false;
    }

    @Override
    public boolean visitTree(VisitContext context, VisitCallback callback) {
        // First check to see whether we are visitable. If not
        // short-circuit out of this subtree, though allow the
        // visit to proceed through to other subtrees.
        if (!isVisitable(context)) {
            return false;
        }

        FacesContext facesContext = context.getFacesContext();
        boolean visitRows = requiresRowIteration(context);

        int oldIndex = -1;
        if (visitRows) {
            oldIndex = index;
            captureOrigValue(facesContext);
            setIndex(facesContext, -1);
        }

        setDataModel(null);

        // Push ourselves to EL
        pushComponentToEL(facesContext, null);

        try {

            // Visit ourselves. Note that we delegate to the
            // VisitContext to actually perform the visit.
            VisitResult result = context.invokeVisitCallback(this, callback);

            // If the visit is complete, short-circuit out and end the visit
            if (result == VisitResult.COMPLETE) {
                return true;
            }

            // Visit children, short-circuiting as necessary
            if (result == VisitResult.ACCEPT && doVisitChildren(context)) {

                // And finally, visit rows
                if (!visitRows) {
                    // visit rows without model access
                    for (UIComponent kid : getChildren()) {
                        if (kid.visitTree(context, callback)) {
                            return true;
                        }
                    }
                } else {
                    if (visitChildren(context, callback)) {
                        return true;
                    }
                }
            }
        } finally {
            // Clean up - pop EL and restore old row index
            popComponentFromEL(facesContext);
            if (visitRows) {
                setIndex(facesContext, oldIndex);
                restoreOrigValue(facesContext);
            }
        }

        // Return false to allow the visit to continue
        return false;
    }

    // SKIP_ITERATION is honored unconditionally, as UIData does. A visit that asks for it wants the
    // row-less clientIds: full state saving keys the saved component state by them, so iterating anyway
    // makes the restore look up repeat:N:foo for state filed under repeat:foo and restore nothing.
    private boolean requiresRowIteration(VisitContext ctx) {
        return !ctx.getHints().contains(VisitHint.SKIP_ITERATION);
    }

    // Tests whether we need to visit our children as part of
    // a tree visit
    private boolean doVisitChildren(VisitContext context) {

        // Just need to check whether there are any ids under this
        // subtree. Make sure row index is cleared out since
        // getSubtreeIdsToVisit() needs our row-less client id.
        //
        // We only need to position if row iteration is actually needed.
        //
        if (requiresRowIteration(context)) {
            setIndex(context.getFacesContext(), -1);
        }
        Collection<String> idsToVisit = context.getSubtreeIdsToVisit(this);
        assert idsToVisit != null;

        // All ids or non-empty collection means we need to visit our children.
        return !idsToVisit.isEmpty();

    }

    private void validateIterationControlValues(int rowCount, int begin, int end) {

        if (rowCount == 0) {
            return;
        }
        // PENDING i18n
        if (begin > rowCount) {
            throw new FacesException("Iteration start index is greater than the number of available rows.");
        }
        if (begin > end) {
            throw new FacesException("Iteration start index is greater than the end index.");
        }
        if (end > rowCount) {
            throw new FacesException("Iteration end index is greater than the number of available rows.");
        }
    }

    private boolean visitChildren(VisitContext context, VisitCallback callback) {

        Integer begin = getBegin();
        Integer end = getEnd();
        Integer step = getStep();

        int rowCount = getDataModel().getRowCount();
        int i = begin != null ? begin : 0;
        int e = end != null ? end : rowCount;
        int s = step != null ? step : 1;
        validateIterationControlValues(rowCount, i, e);
        FacesContext faces = context.getFacesContext();
        setIndex(faces, i);
        updateIterationStatus(faces, new IterationStatus(true, i + s > e || rowCount == 1, i, begin, end, step));
        while (i < e && isIndexAvailable()) {

            setIndex(faces, i);
            updateIterationStatus(faces, new IterationStatus(false, i + s >= e, i, begin, end, step));
            for (UIComponent kid : getChildren()) {
                if (kid.visitTree(context, callback)) {
                    return true;
                }
            }
            i += s;
        }

        return false;
    }

    @Override
    public void processDecodes(FacesContext faces) {
        if (!isRendered()) {
            return;
        }
        setDataModel(null);
        if (!keepSaved(faces)) {
            childState = null;
        }
        process(faces, PhaseId.APPLY_REQUEST_VALUES);
        decode(faces);
    }

    @Override
    public void processUpdates(FacesContext faces) {
        if (!isRendered()) {
            return;
        }
        resetDataModel(faces);
        process(faces, PhaseId.UPDATE_MODEL_VALUES);
    }

    @Override
    public void processValidators(FacesContext faces) {
        if (!isRendered()) {
            return;
        }
        resetDataModel(faces);
        Application app = faces.getApplication();
        app.publishEvent(faces, PreValidateEvent.class, this);
        process(faces, PhaseId.PROCESS_VALIDATIONS);
        app.publishEvent(faces, PostValidateEvent.class, this);
    }

    /**
     * The per-row state of a stateful descendant. It serializes only the fields which are not at their default.
     */
    private final static class SavedState implements Serializable {

        private static final long serialVersionUID = 2L;
        private static final ObjectStreamField[] serialPersistentFields = {};
        private static final int SUBMITTED_VALUE = 1;
        private static final int VALUE = 2;
        private static final int LOCAL_VALUE_SET = 4;
        private static final int INVALID = 8;
        private static final int SUBMITTED = 16;

        private Object submittedValue;
        private boolean valid = true;
        private Object value;
        private boolean localValueSet;
        private boolean submitted;

        @Override
        public String toString() {
            return "submittedValue: " + submittedValue + " value: " + value + " localValueSet: " + localValueSet;
        }

        public void populate(EditableValueHolder evh) {
            value = evh.getLocalValue();
            valid = evh.isValid();
            submittedValue = evh.getSubmittedValue();
            localValueSet = evh.isLocalValueSet();
        }

        public void populate(UIForm form) {
            submitted = form.isSubmitted();
        }

        boolean isSubmitted() {
            return submitted;
        }

        public void apply(EditableValueHolder evh) {
            evh.setValue(value);
            evh.setValid(valid);
            evh.setSubmittedValue(submittedValue);
            evh.setLocalValueSet(localValueSet);
        }

        boolean hasDeltaState() {
            return submittedValue != null || value != null || localValueSet || !valid || submitted;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            int fields = (submittedValue != null ? SUBMITTED_VALUE : 0) | (value != null ? VALUE : 0) | (localValueSet ? LOCAL_VALUE_SET : 0)
                    | (valid ? 0 : INVALID) | (submitted ? SUBMITTED : 0);
            out.writeByte(fields);
            if (submittedValue != null) {
                out.writeObject(submittedValue);
            }
            if (value != null) {
                out.writeObject(value);
            }
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            int fields = in.readByte();
            submittedValue = (fields & SUBMITTED_VALUE) != 0 ? in.readObject() : null;
            value = (fields & VALUE) != 0 ? in.readObject() : null;
            localValueSet = (fields & LOCAL_VALUE_SET) != 0;
            valid = (fields & INVALID) == 0;
            submitted = (fields & SUBMITTED) != 0;
        }

    }

    private static final class IndexedEvent extends FacesEvent {

        private static final long serialVersionUID = 1L;

        private final FacesEvent target;

        private final int index;

        public IndexedEvent(UIRepeat owner, FacesEvent target, int index) {
            super(owner);
            this.target = target;
            this.index = index;
        }

        @Override
        public PhaseId getPhaseId() {
            return target.getPhaseId();
        }

        @Override
        public void setPhaseId(PhaseId phaseId) {
            target.setPhaseId(phaseId);
        }

        @Override
        public boolean isAppropriateListener(FacesListener listener) {
            return target.isAppropriateListener(listener);
        }

        @Override
        public void processListener(FacesListener listener) {
            UIRepeat owner = (UIRepeat) getComponent();
            int prevIndex = owner.index;
            FacesContext ctx = FacesContext.getCurrentInstance();
            try {
                owner.setIndex(ctx, index);
                if (owner.isIndexAvailable()) {
                    target.processListener(listener);
                }
            } finally {
                owner.setIndex(ctx, prevIndex);
            }
        }

        public int getIndex() {
            return index;
        }

        public FacesEvent getTarget() {
            return target;
        }

    }

    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException {
        if (event instanceof IndexedEvent) {
            IndexedEvent idxEvent = (IndexedEvent) event;
            FacesEvent target = idxEvent.getTarget();
            FacesContext ctx = target.getFacesContext();
            resetDataModel(ctx);
            int idx = idxEvent.getIndex();
            int prevIndex = this.index;
            boolean needsToSetIndex = idx != -1 || prevIndex != -1; // #5213
            UIComponent source = target.getComponent();
            UIComponent compositeParent = null;
            try {
                int rowCount = getDataModel().getRowCount();
                if (needsToSetIndex) {
                    setIndex(ctx, idx);
                }
                Integer begin = getBegin();
                Integer end = getEnd();
                Integer step = getStep();
                int b = begin != null ? begin : 0;
                int e = end != null ? end : rowCount;
                int s = step != null ? step : 1;
                updateIterationStatus(ctx, new IterationStatus(idx == b, idx + s >= e || rowCount == 1, idx, begin, end, step));
                if (isIndexAvailable()) {
                    if (!UIComponent.isCompositeComponent(source)) {
                        compositeParent = UIComponent.getCompositeComponentParent(source);
                    }
                    if (compositeParent != null) {
                        compositeParent.pushComponentToEL(ctx, null);
                    }
                    source.pushComponentToEL(ctx, null);
                    source.broadcast(target);

                }
            } finally {
                source.popComponentFromEL(ctx);
                if (compositeParent != null) {
                    compositeParent.popComponentFromEL(ctx);
                }
                updateIterationStatus(ctx, null);
                if (needsToSetIndex) {
                    setIndex(ctx, prevIndex);
                }
            }
        } else {
            super.broadcast(event);
        }
    }

    @Override
    public void queueEvent(FacesEvent event) {
        super.queueEvent(new IndexedEvent(this, event, index));
    }

    /**
     * <p class="changed_added_4_1">
     * Override the base class method to take special action if the method is being invoked when
     * {@link StateManager#IS_BUILDING_INITIAL_STATE} is true <strong>and</strong> the <code>rowStatePreserved</code>
     * property for this instance is <code>true</code>.
     * </p>
     * <p>
     * The additional action taken is to traverse the descendents and save their state without regard to any particular row
     * value.
     * </p>
     *
     * @since 4.1
     */

    @Override
    public void markInitialState() {
        if (isRowStatePreserved()) {
            if (getFacesContext().getAttributes().containsKey(StateManager.IS_BUILDING_INITIAL_STATE)) {
                initialDescendantFullComponentState = saveDescendantInitialComponentStates(getFacesContext(), getChildren().iterator(), false);
            }
        }
        super.markInitialState();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void restoreState(FacesContext faces, Object object) {
        if (faces == null) {
            throw new NullPointerException();
        }
        if (object == null) {
            return;
        }
        Object[] values = (Object[]) object;
        super.restoreState(faces, values[0]);
        Map<String, Object> restoredRowStates = (Map<String, Object>) values[1];
        if (restoredRowStates == null) {
            if (!preservedRowStates.isEmpty()) {
                preservedRowStates.clear();
            }
        } else {
            preservedRowStates = new HashMap<>(restoredRowStates);
        }
        Map<String, SavedState[]> restoredChildState = (Map<String, SavedState[]>) values[2];
        childState = restoredChildState == null ? null : new HashMap<>(restoredChildState);
    }

    @Override
    public Object saveState(FacesContext faces) {
        resetClientIds(this);

        if (faces == null) {
            throw new NullPointerException();
        }
        Object superState = super.saveState(faces);
        if (initialStateMarked() && superState == null && preservedRowStates.isEmpty() && childState == null) {
            return null;
        }
        Object rowStates = preservedRowStates.isEmpty() ? null : new HashMap<>(preservedRowStates);
        return new Object[] { superState, rowStates, childState };
    }

    @Override
    public void encodeChildren(FacesContext faces) throws IOException {
        if (!isRendered()) {
            return;
        }
        setDataModel(null);
        if (!keepSaved(faces)) {
            childState = null;
        }
        process(faces, PhaseId.RENDER_RESPONSE);
    }

    @Override
    public boolean getRendersChildren() {
        if (getRendererType() != null) {
            Renderer renderer = getRenderer(getFacesContext());
            if (renderer != null) {
                return renderer.getRendersChildren();
            }
        }
        return true;
    }
}
