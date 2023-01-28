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

package com.sun.faces.facelets.tag.faces.core;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import com.sun.faces.facelets.tag.TagHandlerImpl;
import com.sun.faces.facelets.tag.faces.ComponentSupport;
import com.sun.faces.facelets.util.ReflectionUtil;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.PhaseEvent;
import jakarta.faces.event.PhaseId;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;

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
            if (binding != null) {
                instance = (PhaseListener) binding.getValue(faces.getELContext());
            }
            if (instance == null && type != null) {
                try {
                    instance = (PhaseListener) ReflectionUtil.forName(type).getDeclaredConstructor().newInstance();
                } catch (IllegalArgumentException | ReflectiveOperationException | SecurityException e) {
                    throw new AbortProcessingException("Couldn't Lazily instantiate PhaseListener", e);
                }
                if (binding != null) {
                    binding.setValue(faces.getELContext(), instance);
                }
            }
            return instance;
        }

        @Override
        public void afterPhase(PhaseEvent event) {
            PhaseListener pl = getInstance();
            if (pl != null) {
                pl.afterPhase(event);
            }
        }

        @Override
        public void beforePhase(PhaseEvent event) {
            PhaseListener pl = getInstance();
            if (pl != null) {
                pl.beforePhase(event);
            }
        }

        @Override
        public PhaseId getPhaseId() {
            PhaseListener pl = getInstance();
            return pl != null ? pl.getPhaseId() : PhaseId.ANY_PHASE;
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
        binding = getAttribute("binding");
        typeAttribute = getAttribute("type");
        if (null != typeAttribute) {
            String stringType = null;
            if (!typeAttribute.isLiteral()) {
                FacesContext context = FacesContext.getCurrentInstance();
                FaceletContext ctx = (FaceletContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
                stringType = (String) typeAttribute.getValueExpression(ctx, String.class).getValue(ctx);
            } else {
                stringType = typeAttribute.getValue();
            }
            checkType(stringType);
            listenerType = stringType;
        } else {
            listenerType = null;
        }
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        if (ComponentHandler.isNew(parent)) {
            UIViewRoot root = ComponentSupport.getViewRoot(ctx, parent);
            if (root == null) {
                throw new TagException(tag, "UIViewRoot not available");
            }
            ValueExpression b = null;
            if (binding != null) {
                b = binding.getValueExpression(ctx, PhaseListener.class);
            }

            PhaseListener pl = new LazyPhaseListener(listenerType, b);

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
