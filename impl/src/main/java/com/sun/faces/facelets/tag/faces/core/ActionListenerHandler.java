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

import java.io.Serializable;

import com.sun.faces.facelets.util.ReflectionUtil;

import jakarta.el.ValueExpression;
import jakarta.faces.component.ActionSource;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;
import jakarta.faces.view.ActionSourceAttachedObjectHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;
import jakarta.faces.view.facelets.TagConfig;

/**
 * Register an ActionListener instance on the UIComponent associated with the closest parent UIComponent custom action.
 *
 * See <a target="_new" href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/actionListener.html">tag
 * documentation</a>.
 *
 * @author Jacob Hookom
 * @see jakarta.faces.event.ActionListener
 * @see jakarta.faces.component.ActionSource
 */
public final class ActionListenerHandler extends ActionListenerHandlerBase implements ActionSourceAttachedObjectHandler {

    private final static class LazyActionListener implements ActionListener, Serializable {

        private static final long serialVersionUID = -9202120013153262119L;

        private final String type;
        private final ValueExpression binding;

        public LazyActionListener(String type, ValueExpression binding) {
            this.type = type;
            this.binding = binding;
        }

        @Override
        public void processAction(ActionEvent event) throws AbortProcessingException {
            ActionListener instance = null;
            FacesContext faces = FacesContext.getCurrentInstance();
            if (faces == null) {
                return;
            }
            if (binding != null) {
                instance = (ActionListener) binding.getValue(faces.getELContext());
            }
            if (instance == null && type != null) {
                try {
                    instance = (ActionListener) ReflectionUtil.newInstance(type);
                } catch (Exception e) {
                    throw new AbortProcessingException("Could not instantiate ActionListener of type " + type, e);
                }
                if (binding != null) {
                    binding.setValue(faces.getELContext(), instance);
                }
            }
            if (instance != null) {
                instance.processAction(event);
            }
        }
    }

    private final TagAttribute binding;

    private String listenerType;

    private final TagAttribute typeAttribute;

    /**
     * @param config
     */
    public ActionListenerHandler(TagConfig config) {
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
    public void applyAttachedObject(FacesContext context, UIComponent parent) {
        FaceletContext ctx = (FaceletContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
        ActionSource as = (ActionSource) parent;
        ValueExpression b = null;
        if (binding != null) {
            b = binding.getValueExpression(ctx, ActionListener.class);
        }
        ActionListener listener = new LazyActionListener(listenerType, b);
        as.addActionListener(listener);
    }

    private void checkType(String type) {
        try {
            ReflectionUtil.forName(type);
        } catch (ClassNotFoundException e) {
            throw new TagAttributeException(typeAttribute, "Couldn't qualify ActionListener", e);
        }
    }
}
