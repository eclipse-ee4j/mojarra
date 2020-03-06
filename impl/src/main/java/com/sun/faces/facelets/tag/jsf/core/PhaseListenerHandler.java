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

package com.sun.faces.facelets.tag.jsf.core;

import com.sun.faces.facelets.tag.TagHandlerImpl;
import com.sun.faces.facelets.tag.jsf.ComponentSupport;
import com.sun.faces.facelets.util.ReflectionUtil;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.view.facelets.*;

import jakarta.el.ValueExpression;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class PhaseListenerHandler extends TagHandlerImpl {

    private final static class LazyPhaseListener implements PhaseListener, Serializable {

        private static final long serialVersionUID = -6496143057319213401L;

        private final String type;

        private final ValueExpression binding;

        public LazyPhaseListener(String type, ValueExpression binding) {
            this.type = type;
            this.binding = binding;
        }

        private PhaseListener getInstance() {
            PhaseListener instance = null;
            FacesContext faces = FacesContext.getCurrentInstance();
            if (faces == null) {
                return null;
            }
            if (this.binding != null) {
                instance = (PhaseListener) binding.getValue(faces.getELContext());
            }
            if (instance == null && type != null) {
                try {
                    instance = (PhaseListener) ReflectionUtil.forName(this.type).newInstance();
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    throw new AbortProcessingException("Couldn't Lazily instantiate PhaseListener", e);
                }
                if (this.binding != null) {
                    binding.setValue(faces.getELContext(), instance);
                }
            }
            return instance;
        }

        @Override
        public void afterPhase(PhaseEvent event) {
            PhaseListener pl = this.getInstance();
            if (pl != null) {
                pl.afterPhase(event);
            }
        }

        @Override
        public void beforePhase(PhaseEvent event) {
            PhaseListener pl = this.getInstance();
            if (pl != null) {
                pl.beforePhase(event);
            }
        }

        @Override
        public PhaseId getPhaseId() {
            PhaseListener pl = this.getInstance();
            return (pl != null) ? pl.getPhaseId() : PhaseId.ANY_PHASE;
        }

        @Override
        public boolean equals(Object o) {

            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            LazyPhaseListener that = (LazyPhaseListener) o;

            if (binding != null ? !binding.equals(that.binding) : that.binding != null) {
                return false;
            }
            if (type != null ? !type.equals(that.type) : that.type != null) {
                return false;
            }

            return true;

        }

        @Override
        public int hashCode() {

            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + (binding != null ? binding.hashCode() : 0);
            return result;

        }

    }

    private final TagAttribute binding;

    private final String listenerType;

    private final TagAttribute typeAttribute;

    public PhaseListenerHandler(TagConfig config) {
        super(config);
        this.binding = this.getAttribute("binding");
        this.typeAttribute = this.getAttribute("type");
        if (null != this.typeAttribute) {
            String stringType = null;
            if (!this.typeAttribute.isLiteral()) {
                FacesContext context = FacesContext.getCurrentInstance();
                FaceletContext ctx = (FaceletContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
                stringType = (String) this.typeAttribute.getValueExpression(ctx, String.class).getValue(ctx);
            } else {
                stringType = this.typeAttribute.getValue();
            }
            checkType(stringType);
            this.listenerType = stringType;
        } else {
            this.listenerType = null;
        }
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        if (ComponentHandler.isNew(parent)) {
            UIViewRoot root = ComponentSupport.getViewRoot(ctx, parent);
            if (root == null) {
                throw new TagException(this.tag, "UIViewRoot not available");
            }
            ValueExpression b = null;
            if (this.binding != null) {
                b = this.binding.getValueExpression(ctx, PhaseListener.class);
            }

            PhaseListener pl = new LazyPhaseListener(this.listenerType, b);

            List<PhaseListener> listeners = root.getPhaseListeners();
            if (!listeners.contains(pl)) {
                root.addPhaseListener(pl);
            }
        }
    }

    private void checkType(String type) {
        try {
            ReflectionUtil.forName(type);
        } catch (ClassNotFoundException e) {
            throw new TagAttributeException(typeAttribute, "Couldn't qualify ActionListener", e);
        }
    }

}
