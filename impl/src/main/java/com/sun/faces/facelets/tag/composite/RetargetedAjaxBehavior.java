/*
 * Copyright (c) 1997, 2023 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.facelets.tag.composite;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.AjaxBehavior;
import jakarta.faces.component.behavior.ClientBehaviorContext;
import jakarta.faces.component.behavior.ClientBehaviorHint;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.AjaxBehaviorListener;
import jakarta.faces.event.BehaviorEvent;
import jakarta.faces.render.ClientBehaviorRenderer;

/**
 * Basically represents {@code <f:ajax>} which is retargeted by {@code <cc:clientBehavior>} in {@code AjaxHandler} and checked in {@code AjaxBehaviorRenderer}.
 *
 * We should probably introduce {@code AjaxBehaviorWrapper} in Faces.next to reduce boilerplate like this.
 *
 * - https://github.com/jakartaee/faces/issues/1567
 * - https://github.com/eclipse-ee4j/mojarra/issues/5032
 */
public class RetargetedAjaxBehavior extends AjaxBehavior {

    private AjaxBehavior retargeted;
    private List<String> targetClientIds;

    public RetargetedAjaxBehavior(AjaxBehavior retargeted, List<String> targetClientIds) {
        this.retargeted = retargeted;
        this.targetClientIds = targetClientIds;
    }

    public List<String> getTargetClientIds() {
        return targetClientIds;
    }

    @Override
    public String getScript(ClientBehaviorContext behaviorContext) {
        ClientBehaviorRenderer renderer = getRenderer(behaviorContext.getFacesContext());
        return renderer != null ? renderer.getScript(behaviorContext, this) : null;
    }

    @Override
    public void broadcast(BehaviorEvent event) throws AbortProcessingException {
        retargeted.broadcast(event);
    }

    @Override
    public int hashCode() {
        return retargeted.hashCode();
    }

    @Override
    public String getRendererType() {
        return retargeted.getRendererType();
    }

    @Override
    public void decode(FacesContext context, UIComponent component) {
        retargeted.decode(context, component);
    }

    @Override
    public boolean isTransient() {
        return retargeted.isTransient();
    }

    @Override
    public Set<ClientBehaviorHint> getHints() {
        return retargeted.getHints();
    }

    @Override
    public void setTransient(boolean transientFlag) {
        retargeted.setTransient(transientFlag);
    }

    @Override
    public String getOnerror() {
        return retargeted.getOnerror();
    }

    @Override
    public void setOnerror(String onerror) {
        retargeted.setOnerror(onerror);
    }

    @Override
    public boolean equals(Object obj) {
        return retargeted.equals(obj);
    }

    @Override
    public String getOnevent() {
        return retargeted.getOnevent();
    }

    @Override
    public void setOnevent(String onevent) {
        retargeted.setOnevent(onevent);
    }

    @Override
    public void markInitialState() {
        retargeted.markInitialState();
    }

    @Override
    public boolean initialStateMarked() {
        return retargeted.initialStateMarked();
    }

    @Override
    public Collection<String> getExecute() {
        return retargeted.getExecute();
    }

    @Override
    public void clearInitialState() {
        retargeted.clearInitialState();
    }

    @Override
    public void setExecute(Collection<String> execute) {
        retargeted.setExecute(execute);
    }

    @Override
    public String getDelay() {
        return retargeted.getDelay();
    }

    @Override
    public void setDelay(String delay) {
        retargeted.setDelay(delay);
    }

    @Override
    public Collection<String> getRender() {
        return retargeted.getRender();
    }

    @Override
    public void setRender(Collection<String> render) {
        retargeted.setRender(render);
    }

    @Override
    public boolean isResetValues() {
        return retargeted.isResetValues();
    }

    @Override
    public void setResetValues(boolean resetValues) {
        retargeted.setResetValues(resetValues);
    }

    @Override
    public boolean isDisabled() {
        return retargeted.isDisabled();
    }

    @Override
    public void setDisabled(boolean disabled) {
        retargeted.setDisabled(disabled);
    }

    @Override
    public boolean isImmediate() {
        return retargeted.isImmediate();
    }

    @Override
    public void setImmediate(boolean immediate) {
        retargeted.setImmediate(immediate);
    }

    @Override
    public boolean isImmediateSet() {
        return retargeted.isImmediateSet();
    }

    @Override
    public String toString() {
        return retargeted.toString();
    }

    @Override
    public boolean isResetValuesSet() {
        return retargeted.isResetValuesSet();
    }

    @Override
    public ValueExpression getValueExpression(String name) {
        return retargeted.getValueExpression(name);
    }

    @Override
    public void setValueExpression(String name, ValueExpression binding) {
        retargeted.setValueExpression(name, binding);
    }

    @Override
    public void addAjaxBehaviorListener(AjaxBehaviorListener listener) {
        retargeted.addAjaxBehaviorListener(listener);
    }

    @Override
    public void removeAjaxBehaviorListener(AjaxBehaviorListener listener) {
        retargeted.removeAjaxBehaviorListener(listener);
    }

    @Override
    public Object saveState(FacesContext context) {
        return retargeted.saveState(context);
    }

    @Override
    public void restoreState(FacesContext context, Object state) {
        retargeted.restoreState(context, state);
    }
}
