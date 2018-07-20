/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.facelets.compiler;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.el.ValueExpression;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.search.UntargetableComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.FacesEvent;
import javax.faces.event.FacesListener;
import javax.faces.render.Renderer;

public class UILeaf extends UIComponentBase implements UntargetableComponent {

    private final static Map<String,UIComponent> facets = new HashMap<String,UIComponent>(0, 1.0f){

        private static final long serialVersionUID = 6132215325480325558L;

        @Override
        public void putAll(Map map) {
            // do nothing
        }

        @Override
        public UIComponent put(String name, UIComponent value) {
            return null;
        }
    };

    private UIComponent parent;
    private boolean returnLocalTransient = true;


    @Override
    public ValueExpression getValueExpression(String name) {
        return null;
    }

    @Override
    public void setValueExpression(String name, ValueExpression arg1) {
        // do nothing
    }

    @Override
    public String getFamily() {
        return "facelets.LiteralText";
    }

    @Override
    public UIComponent getParent() {
        return this.parent;
    }

    @Override
    public void setParent(UIComponent parent) {
        this.parent = parent;
                }

    @Override
    public String getRendererType() {
        return null;
    }

    @Override
    public void setRendererType(String rendererType) {
        // do nothing
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

    @Override
    public List<UIComponent> getChildren() {
        return Collections.emptyList();
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public UIComponent findComponent(String id) {
        return null;
    }

    @Override
    public Map<String,UIComponent> getFacets() {
        return facets;
    }

    @Override
    public int getFacetCount() {
        return 0;
    }

    @Override
    public UIComponent getFacet(String name) {
        return null;
    }

    @Override
    public Iterator<UIComponent> getFacetsAndChildren() {
        return Collections.<UIComponent>emptyList().iterator();
    }

    @Override
    public void broadcast(FacesEvent event) throws AbortProcessingException {
        // do nothing
    }

    @Override
    public void decode(FacesContext faces) {
        // do nothing
    }

    @Override
    public void encodeBegin(FacesContext faces) throws IOException {
        // do nothing
    }

    @Override
    public void encodeChildren(FacesContext faces) throws IOException {
        // do nothing
    }

    @Override
    public void encodeEnd(FacesContext faces) throws IOException {
        // do nothing
    }

    @Override
    public void encodeAll(FacesContext faces) throws IOException {
        this.encodeBegin(faces);
    }

    @Override
    protected void addFacesListener(FacesListener faces) {
        // do nothing
    }

    @Override
    protected FacesListener[] getFacesListeners(Class faces) {
        return null;
    }

    @Override
    protected void removeFacesListener(FacesListener faces) {
        // do nothing
    }

    @Override
    public void queueEvent(FacesEvent event) {
        // do nothing
    }

    @Override
    public void processDecodes(FacesContext faces) {
        // do nothing
    }

    @Override
    public void processValidators(FacesContext faces) {
        // do nothing
    }

    @Override
    public void processUpdates(FacesContext faces) {
        // do nothing
    }

    @Override
    protected FacesContext getFacesContext() {
        return FacesContext.getCurrentInstance();
    }

    @Override
    protected Renderer getRenderer(FacesContext faces) {
        return null;
    }


    @Override
    public boolean isTransient() {
        return ((returnLocalTransient) || super.isTransient());
    }

    @Override
    public void setTransient(boolean tranzient) {
        returnLocalTransient = false;
        super.setTransient(tranzient);
    }

}
