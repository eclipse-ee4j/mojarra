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

package com.sun.faces.facelets.tag.faces;

import java.io.IOException;

import com.sun.faces.facelets.tag.MetaRulesetImpl;
import com.sun.faces.util.Util;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.ValueHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.view.AttachedObjectHandler;
import jakarta.faces.view.facelets.ConverterHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagException;
import jakarta.faces.view.facelets.TagHandlerDelegate;

/**
 *
 * @author edburns
 */
public class ConverterTagHandlerDelegateImpl extends TagHandlerDelegate implements AttachedObjectHandler {

    private ConverterHandler owner;

    public ConverterTagHandlerDelegateImpl(ConverterHandler owner) {
        this.owner = owner;
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        // only process if it's been created
        if (parent == null || !(parent.getParent() == null)) {
            return;
        }
        ComponentSupport.copyPassthroughAttributes(ctx, parent, owner.getTag());
        if (parent instanceof ValueHolder) {
            owner.applyAttachedObject(ctx.getFacesContext(), parent);
        } else if (UIComponent.isCompositeComponent(parent)) {
            if (null == owner.getFor()) {
                // PENDING(): I18N
                throw new TagException(owner.getTag(), "converter tags nested within composite components must have a non-null \"for\" attribute");
            }
            // Allow the composite component to know about the target
            // component.
            CompositeComponentTagHandler.getAttachedObjectHandlers(parent).add(owner);
        } else {
            throw new TagException(owner.getTag(), "Parent not an instance of ValueHolder: " + parent);
        }
    }

    @Override
    public MetaRuleset createMetaRuleset(Class type) {
        Util.notNull("type", type);
        MetaRuleset m = new MetaRulesetImpl(owner.getTag(), type);

        return m.ignore("binding").ignore("for");
    }

    @Override
    public String getFor() {
        String result = null;
        TagAttribute attr = owner.getTagAttribute("for");

        if (null != attr) {
            if (attr.isLiteral()) {
                result = attr.getValue();
            } else {
                FacesContext context = FacesContext.getCurrentInstance();
                FaceletContext ctx = (FaceletContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
                result = (String) attr.getValueExpression(ctx, String.class).getValue(ctx);
            }
        }
        return result;

    }

    @Override
    public void applyAttachedObject(FacesContext context, UIComponent parent) {
        FaceletContext ctx = (FaceletContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
        // cast to a ValueHolder
        ValueHolder vh = (ValueHolder) parent;
        ValueExpression ve = null;
        Converter c = null;
        if (owner.getBinding() != null) {
            ve = owner.getBinding().getValueExpression(ctx, Converter.class);
            c = (Converter) ve.getValue(ctx);
        }
        if (c == null) {
            c = createConverter(ctx);
            if (ve != null) {
                ve.setValue(ctx, c);
            }
        }
        if (c == null) {
            throw new TagException(owner.getTag(), "No Converter was created");
        }
        owner.setAttributes(ctx, c);
        vh.setConverter(c);
        Object lv = vh.getLocalValue();
        FacesContext faces = ctx.getFacesContext();
        if (lv instanceof String) {
            vh.setValue(c.getAsObject(faces, parent, (String) lv));
        }
    }

    /**
     * Create a Converter instance
     *
     * @param ctx FaceletContext to use
     * @return Converter instance, cannot be null
     */
    private Converter createConverter(FaceletContext ctx) {
        if (owner.getConverterId(ctx) == null) {
            throw new TagException(owner.getTag(),
                    "Default behavior invoked of requiring a converter-id passed in the constructor, must override ConvertHandler(ConverterConfig)");
        }
        return ctx.getFacesContext().getApplication().createConverter(owner.getConverterId(ctx));
    }

}
