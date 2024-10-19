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

import static jakarta.faces.view.facelets.FaceletContext.FACELET_CONTEXT_KEY;

import java.io.IOException;
import java.io.Serializable;

import com.sun.faces.facelets.tag.TagHandlerImpl;
import com.sun.faces.facelets.tag.faces.CompositeComponentTagHandler;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.faces.component.ActionSource;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ActionEvent;
import jakarta.faces.event.ActionListener;
import jakarta.faces.view.ActionSourceAttachedObjectHandler;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;

public class SetPropertyActionListenerHandler extends TagHandlerImpl implements ActionSourceAttachedObjectHandler {

    private final TagAttribute value;
    private final TagAttribute target;

    public SetPropertyActionListenerHandler(TagConfig config) {
        super(config);
        value = getRequiredAttribute("value");
        target = getRequiredAttribute("target");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        if (parent == null || !ComponentHandler.isNew(parent)) {
            return;
        }

        if (parent instanceof ActionSource) {
            applyAttachedObject(ctx.getFacesContext(), parent);
        } else if (UIComponent.isCompositeComponent(parent)) {
            if (getFor() == null) {
                throw new TagException(tag, "actionListener tags nested within composite components must have a non-null \"for\" attribute");
            }
            // Allow the composite component to know about the target
            // component.
            CompositeComponentTagHandler.getAttachedObjectHandlers(parent).add(this);
        } else {
            throw new TagException(tag, "Parent is not of type ActionSource, type is: " + parent);
        }
    }

    @Override
    public void applyAttachedObject(FacesContext context, UIComponent parent) {
        FaceletContext faceletContext = (FaceletContext) context.getAttributes().get(FACELET_CONTEXT_KEY);

        ActionSource src = (ActionSource) parent;
        ValueExpression valueExpr = value.getValueExpression(faceletContext, Object.class);
        ValueExpression targetExpr = target.getValueExpression(faceletContext, Object.class);

        src.addActionListener(new SetPropertyListener(valueExpr, targetExpr));
    }

    @Override
    public String getFor() {
        String result = null;
        TagAttribute attr = getAttribute("for");

        if (attr != null) {
            if (attr.isLiteral()) {
                result = attr.getValue();
            } else {
                FaceletContext ctx = (FaceletContext) FacesContext.getCurrentInstance().getAttributes().get(FACELET_CONTEXT_KEY);
                result = (String) attr.getValueExpression(ctx, String.class).getValue(ctx);
            }
        }

        return result;
    }

    private static class SetPropertyListener implements ActionListener, Serializable {

        private static final long serialVersionUID = -2760242070551459725L;

        private ValueExpression value;
        private ValueExpression target;

        public SetPropertyListener(ValueExpression value, ValueExpression target) {
            this.value = value;
            this.target = target;
        }

        @Override
        public void processAction(ActionEvent evt) throws AbortProcessingException {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ELContext elContext = facesContext.getELContext();

            Object valueObj = value.getValue(elContext);
            if (valueObj != null) {
                valueObj = facesContext.getApplication()
                                       .getExpressionFactory()
                                       .coerceToType(valueObj, target.getType(elContext));
            }

            target.setValue(elContext, valueObj);
        }

    }

}
