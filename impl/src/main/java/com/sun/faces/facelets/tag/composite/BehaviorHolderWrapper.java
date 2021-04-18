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

package com.sun.faces.facelets.tag.composite;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.el.ValueExpression;
import jakarta.faces.FacesException;
import jakarta.faces.component.ContextCallback;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.component.visit.VisitCallback;
import jakarta.faces.component.visit.VisitContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.FacesEvent;
import jakarta.faces.event.FacesListener;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.render.Renderer;

/**
 * <p class="changed_added_2_0">
 * </p>
 */
public class BehaviorHolderWrapper extends UIComponent implements ClientBehaviorHolder {

    private final UIComponent parent;
    private final String virtualEvent;
    private final String event;
    private final ValueExpression targets;

    public BehaviorHolderWrapper(UIComponent parent, String virtualEvent, String event, ValueExpression targets) {
        this.parent = parent;
        this.virtualEvent = virtualEvent;
        this.event = event;
        this.targets = targets;
    }

    /**
     * @see jakarta.faces.component.UIComponent#broadcast(jakarta.faces.event.FacesEvent)
     */
    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException {
        parent.broadcast(event);
    }

    /**
     * @see jakarta.faces.component.UIComponent#decode(jakarta.faces.context.FacesContext)
     */
    @Override
    public void decode(FacesContext context) {
        parent.decode(context);
    }

    /**
     * @see jakarta.faces.component.UIComponent#encodeBegin(jakarta.faces.context.FacesContext)
     */
    @Override
    public void encodeBegin(FacesContext context) throws IOException {
        parent.encodeBegin(context);
    }

    /**
     * @see jakarta.faces.component.UIComponent#encodeChildren(jakarta.faces.context.FacesContext)
     */
    @Override
    public void encodeChildren(FacesContext context) throws IOException {
        parent.encodeChildren(context);
    }

    /**
     * @see jakarta.faces.component.UIComponent#encodeEnd(jakarta.faces.context.FacesContext)
     */
    @Override
    public void encodeEnd(FacesContext context) throws IOException {
        parent.encodeEnd(context);
    }

    /**
     * @see jakarta.faces.component.UIComponent#findComponent(java.lang.String)
     */
    @Override
    public UIComponent findComponent(String expr) {
        return parent.findComponent(expr);
    }

    /**
     * @see jakarta.faces.component.UIComponent#getAttributes()
     */
    @Override
    public Map<String, Object> getAttributes() {
        return parent.getAttributes();
    }

    @Override
    public Map<String, Object> getPassThroughAttributes(boolean create) {
        return parent.getPassThroughAttributes(create);
    }

    /**
     * @see jakarta.faces.component.UIComponent#getChildCount()
     */
    @Override
    public int getChildCount() {
        return parent.getChildCount();
    }

    /**
     * @see jakarta.faces.component.UIComponent#getChildren()
     */
    @Override
    public List<UIComponent> getChildren() {
        return parent.getChildren();
    }

    /**
     * @see jakarta.faces.component.UIComponent#getClientId(jakarta.faces.context.FacesContext)
     */
    @Override
    public String getClientId(FacesContext context) {
        return parent.getClientId(context);
    }

    /**
     * @see jakarta.faces.component.UIComponent#getFacet(java.lang.String)
     */
    @Override
    public UIComponent getFacet(String name) {
        return parent.getFacet(name);
    }

    /**
     * @see jakarta.faces.component.UIComponent#getFacets()
     */
    @Override
    public Map<String, UIComponent> getFacets() {
        return parent.getFacets();
    }

    /**
     * @see jakarta.faces.component.UIComponent#getFacetsAndChildren()
     */
    @Override
    public Iterator<UIComponent> getFacetsAndChildren() {
        return parent.getFacetsAndChildren();
    }

    /**
     * @see jakarta.faces.component.UIComponent#getFamily()
     */
    @Override
    public String getFamily() {
        return parent.getFamily();
    }

    /**
     * @see jakarta.faces.component.UIComponent#getId()
     */
    @Override
    public String getId() {
        return parent.getId();
    }

    /**
     * @see jakarta.faces.component.UIComponent#getParent()
     */
    @Override
    public UIComponent getParent() {
        return parent.getParent();
    }

    /**
     * @see jakarta.faces.component.UIComponent#getRendererType()
     */
    @Override
    public String getRendererType() {
        return parent.getRendererType();
    }

    /**
     * @see jakarta.faces.component.UIComponent#getRendersChildren()
     */
    @Override
    public boolean getRendersChildren() {
        return parent.getRendersChildren();
    }

    @Override
    public ValueExpression getValueExpression(String name) {
        return parent.getValueExpression(name);
    }

    /**
     * @see jakarta.faces.component.UIComponent#invokeOnComponent(jakarta.faces.context.FacesContext, java.lang.String,
     * jakarta.faces.component.ContextCallback)
     */
    @Override
    public boolean invokeOnComponent(FacesContext context, String clientId, ContextCallback callback) throws FacesException {
        return parent.invokeOnComponent(context, clientId, callback);
    }

    /**
     * @see jakarta.faces.component.UIComponent#isInView()
     */
    @Override
    public boolean isInView() {
        return parent.isInView();
    }

    /**
     * @see jakarta.faces.component.UIComponent#isRendered()
     */
    @Override
    public boolean isRendered() {
        return parent.isRendered();
    }

    /**
     * @see jakarta.faces.component.StateHolder#isTransient()
     */
    @Override
    public boolean isTransient() {
        return parent.isTransient();
    }

    /**
     * @see jakarta.faces.component.UIComponent#processDecodes(jakarta.faces.context.FacesContext)
     */
    @Override
    public void processDecodes(FacesContext context) {
        parent.processDecodes(context);
    }

    /**
     * @see jakarta.faces.component.UIComponent#processEvent(jakarta.faces.event.ComponentSystemEvent)
     */
    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        parent.processEvent(event);
    }

    /**
     * @see jakarta.faces.component.UIComponent#processRestoreState(jakarta.faces.context.FacesContext, java.lang.Object)
     */
    @Override
    public void processRestoreState(FacesContext context, Object state) {
        parent.processRestoreState(context, state);
    }

    /**
     * @see jakarta.faces.component.UIComponent#processSaveState(jakarta.faces.context.FacesContext)
     */
    @Override
    public Object processSaveState(FacesContext context) {
        return parent.processSaveState(context);
    }

    /**
     * @see jakarta.faces.component.UIComponent#processUpdates(jakarta.faces.context.FacesContext)
     */
    @Override
    public void processUpdates(FacesContext context) {
        parent.processUpdates(context);
    }

    /**
     * @see jakarta.faces.component.UIComponent#processValidators(jakarta.faces.context.FacesContext)
     */
    @Override
    public void processValidators(FacesContext context) {
        parent.processValidators(context);
    }

    /**
     * @see jakarta.faces.component.UIComponent#queueEvent(jakarta.faces.event.FacesEvent)
     */
    @Override
    public void queueEvent(FacesEvent event) {
        parent.queueEvent(event);
    }

    /**
     * @see jakarta.faces.component.StateHolder#restoreState(jakarta.faces.context.FacesContext, java.lang.Object)
     */
    @Override
    public void restoreState(FacesContext context, Object state) {
        if (context == null) {
            throw new NullPointerException();
        }
        parent.restoreState(context, state);
    }

    /**
     * @see jakarta.faces.component.StateHolder#saveState(jakarta.faces.context.FacesContext)
     */
    @Override
    public Object saveState(FacesContext context) {
        if (context == null) {
            throw new NullPointerException();
        }
        return parent.saveState(context);
    }

    /**
     * @see jakarta.faces.component.UIComponent#setId(java.lang.String)
     */
    @Override
    public void setId(String id) {
        parent.setId(id);
    }

    /**
     * @see jakarta.faces.component.UIComponent#setParent(jakarta.faces.component.UIComponent)
     */
    @Override
    public void setParent(UIComponent parent) {
        parent.setParent(parent);
    }

    /**
     * @see jakarta.faces.component.UIComponent#setRendered(boolean)
     */
    @Override
    public void setRendered(boolean rendered) {
        parent.setRendered(rendered);
    }

    /**
     * @see jakarta.faces.component.UIComponent#setRendererType(java.lang.String)
     */
    @Override
    public void setRendererType(String rendererType) {
        parent.setRendererType(rendererType);
    }

    /**
     * @see jakarta.faces.component.StateHolder#setTransient(boolean)
     */
    @Override
    public void setTransient(boolean newTransientValue) {
        parent.setTransient(newTransientValue);
    }

    /**
     * @see jakarta.faces.component.UIComponent#setValueExpression(java.lang.String, jakarta.el.ValueExpression)
     */
    @Override
    public void setValueExpression(String name, ValueExpression binding) {
        parent.setValueExpression(name, binding);
    }

    /**
     * @see jakarta.faces.component.UIComponent#subscribeToEvent(java.lang.Class,
     * jakarta.faces.event.ComponentSystemEventListener)
     */
    @Override
    public void subscribeToEvent(Class<? extends SystemEvent> eventClass, ComponentSystemEventListener componentListener) {
        parent.subscribeToEvent(eventClass, componentListener);
    }

    /**
     * @see jakarta.faces.component.UIComponent#unsubscribeFromEvent(java.lang.Class,
     * jakarta.faces.event.ComponentSystemEventListener)
     */
    @Override
    public void unsubscribeFromEvent(Class<? extends SystemEvent> eventClass, ComponentSystemEventListener componentListener) {
        parent.unsubscribeFromEvent(eventClass, componentListener);
    }

    /**
     * @see jakarta.faces.component.UIComponent#visitTree(jakarta.faces.component.visit.VisitContext,
     * jakarta.faces.component.visit.VisitCallback)
     */
    @Override
    public boolean visitTree(VisitContext context, VisitCallback callback) {
        return parent.visitTree(context, callback);
    }

    @Override
    protected void addFacesListener(FacesListener listener) {
        // no-op
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected FacesListener[] getFacesListeners(Class clazz) {
        return new FacesListener[0];
    }

    @Override
    protected Renderer getRenderer(FacesContext context) {
        return null;
    }

    @Override
    protected void removeFacesListener(FacesListener listener) {
        // no-op
    }

    @Override
    public void addClientBehavior(String eventName, ClientBehavior behavior) {
        if (parent instanceof ClientBehaviorHolder) {
            ClientBehaviorHolder parentHolder = (ClientBehaviorHolder) parent;
            if (virtualEvent.equals(eventName)) {
                parentHolder.addClientBehavior(event, behavior);
            }
        } else {
            throw new FacesException("Unable to attach behavior to non-ClientBehaviorHolder parent:" + parent);
        }

    }

    @Override
    public Map<String, List<ClientBehavior>> getClientBehaviors() {
        if (parent instanceof ClientBehaviorHolder) {
            ClientBehaviorHolder parentHolder = (ClientBehaviorHolder) parent;
            Map<String, List<ClientBehavior>> behaviors = new HashMap<>(1);
            behaviors.put(virtualEvent, parentHolder.getClientBehaviors().get(event));
            return Collections.unmodifiableMap(behaviors);
        } else {
            throw new FacesException("Unable to get behaviors from non-ClientBehaviorHolder parent:" + parent);
        }
    }

    @Override
    public String getDefaultEventName() {
        return virtualEvent;
    }

    @Override
    public Collection<String> getEventNames() {
        return Collections.singleton(virtualEvent);
    }
    
    public ValueExpression getTargets() {
        return targets;
    }

}
