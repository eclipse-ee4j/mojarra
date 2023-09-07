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
import static com.sun.faces.renderkit.RenderKitUtils.PredefinedPostbackParameter.BEHAVIOR_SOURCE_PARAM;
import static com.sun.faces.util.Util.isNestedInIterator;

import java.io.IOException;
import java.io.Serializable;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.sun.faces.facelets.tag.IterationStatus;

import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.application.Application;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.ContextCallback;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
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

    // our data
    private Object value;

    private transient DataModel model;

    // variables
    private String var;

    private String varStatus;

    private int index = -1;

    private Integer originalBegin;
    private Integer originalEnd;

    private Integer begin;
    private Integer end;
    private Integer step;
    private Integer size;

    public UIRepeat() {
        setRendererType("facelets.ui.Repeat");
    }

    @Override
    public String getFamily() {
        return COMPONENT_FAMILY;
    }

    public void setEnd(Integer end) {
        this.end = end;
    }

    public Integer getEnd() {

        if (end != null) {
            return end;
        }
        ValueExpression ve = getValueExpression("end");
        if (ve != null) {
            return (Integer) ve.getValue(getFacesContext().getELContext());
        }
        return null;

    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Integer getSize() {

        if (size != null) {
            return size;
        }
        ValueExpression ve = getValueExpression("size");
        if (ve != null) {
            return (Integer) ve.getValue(getFacesContext().getELContext());
        }
        return null;

    }

    public void setOffset(Integer offset) {
        begin = offset;
    }

    public Integer getOffset() {

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
        this.begin = begin;
    }

    public Integer getBegin() {

        if (begin != null) {
            return begin;
        }
        ValueExpression ve = getValueExpression("begin");
        if (ve != null) {
            return (Integer) ve.getValue(getFacesContext().getELContext());
        }
        return null;

    }

    public void setStep(Integer step) {
        this.step = step;
    }

    public Integer getStep() {

        if (step != null) {
            return step;
        }
        ValueExpression ve = getValueExpression("step");
        if (ve != null) {
            return (Integer) ve.getValue(getFacesContext().getELContext());
        }
        return null;

    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public String getVarStatus() {
        return varStatus;
    }

    public void setVarStatus(String varStatus) {
        this.varStatus = varStatus;
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

    private DataModel getDataModel() {
        if (model == null) {
            Object val = getValue();
            if (val == null) {
                if (originalBegin == null) {
                    originalBegin = getBegin();
                }
                if (originalEnd == null) {
                    originalEnd = getEnd();
                }

                Integer begin = originalBegin;
                Integer end = originalEnd;

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
        if (value == null) {
            ValueExpression ve = getValueExpression("value");
            if (ve != null) {
                return ve.getValue(getFacesContext().getELContext());
            }
        }
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    private transient StringBuffer buffer;

    private StringBuffer getBuffer() {
        if (buffer == null) {
            buffer = new StringBuffer();
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

    private Map<String, SavedState> childState;

    private Map<String, SavedState> getChildState() {
        if (childState == null) {
            childState = new HashMap<>();
        }
        return childState;
    }

    private void clearChildState() {
        childState = null;
    }

    private void saveChildState(FacesContext ctx) {
        if (getChildCount() > 0) {

            for (UIComponent uiComponent : getChildren()) {
                this.saveChildState(ctx, uiComponent);
            }
        }
    }

    private void removeChildState(FacesContext ctx) {
        if (getChildCount() > 0) {

            for (UIComponent uiComponent : getChildren()) {
                this.removeChildState(ctx, uiComponent);
            }

            if (childState != null) {
                childState.remove(this.getClientId(ctx));
            }
        }
    }

    private void removeChildState(FacesContext faces, UIComponent c) {
        String id = c.getId();
        c.setId(id);

        Iterator itr = c.getFacetsAndChildren();
        while (itr.hasNext()) {
            removeChildState(faces, (UIComponent) itr.next());
        }
        if (childState != null) {
            childState.remove(c.getClientId(faces));
        }
    }

    private void saveChildState(FacesContext faces, UIComponent c) {

        if (c instanceof EditableValueHolder && !c.isTransient()) {
            String clientId = c.getClientId(faces);
            SavedState ss = getChildState().get(clientId);
            if (ss == null) {
                ss = new SavedState();
                getChildState().put(clientId, ss);
            }
            ss.populate((EditableValueHolder) c);
        }

        // continue hack
        Iterator itr = c.getFacetsAndChildren();
        while (itr.hasNext()) {
            saveChildState(faces, (UIComponent) itr.next());
        }
    }

    private void restoreChildState(FacesContext ctx) {
        if (getChildCount() > 0) {

            for (UIComponent uiComponent : getChildren()) {
                this.restoreChildState(ctx, uiComponent);
            }
        }
    }

    private void restoreChildState(FacesContext faces, UIComponent c) {
        // reset id
        String id = c.getId();
        c.setId(id);

        // hack
        if (c instanceof EditableValueHolder) {
            EditableValueHolder evh = (EditableValueHolder) c;
            String clientId = c.getClientId(faces);
            SavedState ss = getChildState().get(clientId);
            if (ss != null) {
                ss.apply(evh);
            } else {
                NULL_STATE.apply(evh);
            }
        }

        // continue hack
        Iterator itr = c.getFacetsAndChildren();
        while (itr.hasNext()) {
            restoreChildState(faces, (UIComponent) itr.next());
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

        DataModel localModel = getDataModel();

        // save child state
        if (this.index != -1 && localModel.isRowAvailable()) {
            this.saveChildState(ctx);
        } else if (this.index >= 0 && childState != null) {
            this.removeChildState(ctx);
        }

        this.index = index;
        localModel.setRowIndex(index);

        if (this.index != -1 && var != null && localModel.isRowAvailable()) {
            Map<String, Object> attrs = ctx.getExternalContext().getRequestMap();
            attrs.put(var, localModel.getRowData());
        }

        // restore child state
        if (this.index != -1 && localModel.isRowAvailable()) {
            this.restoreChildState(ctx);
        }
    }

    private void updateIterationStatus(FacesContext ctx, IterationStatus status) {
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

    private boolean requiresRowIteration(VisitContext ctx) {
        boolean shouldIterate = !ctx.getHints().contains(VisitHint.SKIP_ITERATION);
        if (!shouldIterate) {
            FacesContext faces = ctx.getFacesContext();
            String sourceId = BEHAVIOR_SOURCE_PARAM.getValue(faces);
            boolean containsSource = sourceId != null ? sourceId.startsWith(super.getClientId(faces) + getSeparatorChar(faces)) : false;
            return containsSource;
        } else {
            return shouldIterate;
        }
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

    private final static SavedState NULL_STATE = new SavedState();

    // from RI
    private final static class SavedState implements Serializable {

        private Object submittedValue;

        private static final long serialVersionUID = 2920252657338389849L;

        Object getSubmittedValue() {
            return submittedValue;
        }

        void setSubmittedValue(Object submittedValue) {
            this.submittedValue = submittedValue;
        }

        private boolean valid = true;

        boolean isValid() {
            return valid;
        }

        void setValid(boolean valid) {
            this.valid = valid;
        }

        private Object value;

        Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }

        private boolean localValueSet;

        boolean isLocalValueSet() {
            return localValueSet;
        }

        public void setLocalValueSet(boolean localValueSet) {
            this.localValueSet = localValueSet;
        }

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

        public void apply(EditableValueHolder evh) {
            evh.setValue(value);
            evh.setValid(valid);
            evh.setSubmittedValue(submittedValue);
            evh.setLocalValueSet(localValueSet);
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

    @Override
    public void restoreState(FacesContext faces, Object object) {
        if (faces == null) {
            throw new NullPointerException();
        }
        if (object == null) {
            return;
        }
        Object[] state = (Object[]) object;
        super.restoreState(faces, state[0]);
        // noinspection unchecked
        childState = (Map<String, SavedState>) state[1];
        begin = (Integer) state[2];
        end = (Integer) state[3];
        step = (Integer) state[4];
        var = (String) state[5];
        varStatus = (String) state[6];
        value = state[7];
        originalBegin = (Integer) state[8];
        originalEnd = (Integer) state[9];
    }

    @Override
    public Object saveState(FacesContext faces) {
        resetClientIds(this);

        if (faces == null) {
            throw new NullPointerException();
        }
        Object[] state = new Object[10];
        state[0] = super.saveState(faces);
        state[1] = childState;
        state[2] = begin;
        state[3] = end;
        state[4] = step;
        state[5] = var;
        state[6] = varStatus;
        state[7] = value;
        state[8] = originalBegin;
        state[9] = originalEnd;
        return state;
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
