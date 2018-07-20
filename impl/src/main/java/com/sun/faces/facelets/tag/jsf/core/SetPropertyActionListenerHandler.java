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

import java.io.IOException;
import java.io.Serializable;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.faces.component.ActionSource;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.faces.view.ActionSource2AttachedObjectHandler;
import javax.faces.view.facelets.ComponentHandler;
import javax.faces.view.facelets.FaceletContext;
import javax.faces.view.facelets.TagAttribute;
import javax.faces.view.facelets.TagConfig;
import javax.faces.view.facelets.TagException;

import com.sun.faces.facelets.tag.TagHandlerImpl;
import com.sun.faces.facelets.tag.jsf.CompositeComponentTagHandler;

public class SetPropertyActionListenerHandler extends TagHandlerImpl implements ActionSource2AttachedObjectHandler {

    private final TagAttribute value;

    private final TagAttribute target;

    public SetPropertyActionListenerHandler(TagConfig config) {
        super(config);
        this.value = this.getRequiredAttribute("value");
        this.target = this.getRequiredAttribute("target");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent)
            throws IOException {

        if (null == parent || !(ComponentHandler.isNew(parent))) {
            return;
        }
        if (parent instanceof ActionSource) {
            applyAttachedObject(ctx.getFacesContext(), parent);
        } else if (UIComponent.isCompositeComponent(parent)) {
            if (null == getFor()) {
                // PENDING(): I18N
                throw new TagException(this.tag,
                                       "actionListener tags nested within composite components must have a non-null \"for\" attribute");
            }
            // Allow the composite component to know about the target
            // component.
            CompositeComponentTagHandler.getAttachedObjectHandlers(parent)
                  .add(this);

        } else {
            throw new TagException(this.tag,
                                   "Parent is not of type ActionSource, type is: "
                                   + parent);
        }

    }

    @Override
    public void applyAttachedObject(FacesContext context, UIComponent parent) {
        FaceletContext ctx = (FaceletContext) context.getAttributes()
              .get(FaceletContext.FACELET_CONTEXT_KEY);

        ActionSource src = (ActionSource) parent;
        ValueExpression valueExpr = this.value.getValueExpression(ctx,
                    Object.class);
        ValueExpression targetExpr = this.target.getValueExpression(
                ctx, Object.class);

        src.addActionListener(new SetPropertyListener(valueExpr, targetExpr));
    }


    @Override
    public String getFor() {
        String result = null;
        TagAttribute attr = this.getAttribute("for");

        if (null != attr) {
            if (attr.isLiteral()) {
                result = attr.getValue();
            } else {
                FacesContext context = FacesContext.getCurrentInstance();
                FaceletContext ctx = (FaceletContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
                result = (String)attr.getValueExpression(ctx, String.class).getValue(ctx);
            }
        }
        return result;
    }

    private static class SetPropertyListener implements ActionListener,
            Serializable {

        private static final long serialVersionUID = -2760242070551459725L;

        private ValueExpression value;

        private ValueExpression target;

        public SetPropertyListener() {
        };

        public SetPropertyListener(ValueExpression value, ValueExpression target) {
            this.value = value;
            this.target = target;
        }

        @Override
        public void processAction(ActionEvent evt)
                throws AbortProcessingException {
            FacesContext faces = FacesContext.getCurrentInstance();
            ELContext el = faces.getELContext();
            Object valueObj = this.value.getValue(el);
             if (valueObj != null) {
                ExpressionFactory factory =
                      faces.getApplication().getExpressionFactory();
                valueObj = factory.coerceToType(valueObj, target.getType(el));
            }
            this.target.setValue(el, valueObj);
        }

    }

}
