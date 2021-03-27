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
import java.util.HashSet;
import java.util.Set;

import com.sun.faces.cdi.CdiValidator;
import com.sun.faces.component.validator.ComponentValidators;
import com.sun.faces.facelets.tag.MetaRulesetImpl;
import com.sun.faces.util.RequestStateManager;
import com.sun.faces.util.Util;

import jakarta.el.ValueExpression;
import jakarta.faces.component.EditableValueHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.Validator;
import jakarta.faces.view.AttachedObjectHandler;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.CompositeFaceletHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagException;
import jakarta.faces.view.facelets.TagHandler;
import jakarta.faces.view.facelets.TagHandlerDelegate;
import jakarta.faces.view.facelets.ValidatorHandler;

public class ValidatorTagHandlerDelegateImpl extends TagHandlerDelegate implements AttachedObjectHandler {

    protected final ValidatorHandler owner;
    private final boolean wrapping;

    // ------------------------------------------------------------ Constructors

    public ValidatorTagHandlerDelegateImpl(ValidatorHandler owner) {

        this.owner = owner;
        wrapping = isWrapping();

    }

    // ----------------------------------------- Methods from TagHandlerDelegate

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {

        ComponentSupport.copyPassthroughAttributes(ctx, parent, owner.getTag());
        if (wrapping) {
            applyWrapping(ctx, parent);
        } else {
            applyNested(ctx, parent);
        }

    }

    @Override
    public MetaRuleset createMetaRuleset(Class type) {

        Util.notNull("type", type);
        MetaRuleset m = new MetaRulesetImpl(owner.getTag(), type);

        return m.ignore("binding").ignore("disabled").ignore("for");

    }

    // -------------------------------------- Methods from AttachedObjectHandler

    @SuppressWarnings({ "unchecked" })
    @Override
    public void applyAttachedObject(FacesContext context, UIComponent parent) {

        FaceletContext ctx = (FaceletContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
        EditableValueHolder evh = (EditableValueHolder) parent;
        if (owner.isDisabled(ctx)) {
            Set<String> disabledIds = (Set<String>) RequestStateManager.get(context, RequestStateManager.DISABLED_VALIDATORS);
            if (disabledIds == null) {
                disabledIds = new HashSet<>(3);
                RequestStateManager.set(context, RequestStateManager.DISABLED_VALIDATORS, disabledIds);
            }
            disabledIds.add(owner.getValidatorId(ctx));
            return;
        }

        ValueExpression ve = null;
        Validator v = null;
        if (owner.getBinding() != null) {
            ve = owner.getBinding().getValueExpression(ctx, Validator.class);
            v = (Validator) ve.getValue(ctx);
        }
        if (v == null) {
            v = createValidator(ctx);
            if (ve != null) {
                ve.setValue(ctx, v);
            }
        }
        if (v == null) {
            throw new TagException(owner.getTag(), "No Validator was created");
        }
        owner.setAttributes(ctx, v);

        Validator[] validators = evh.getValidators();
        boolean found = false;

        for (Validator validator : validators) {
            if (validator.getClass().equals(v.getClass()) && !(v instanceof CdiValidator)) {
                found = true;
                break;
            }
        }

        if (!found) {
            evh.addValidator(v);
        }
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

    // ------------------------------------------------------- Protected Methods

    protected ComponentValidators.ValidatorInfo createValidatorInfo(FaceletContext ctx) {

        return new ComponentValidators.ValidatorInfo(ctx, owner);

    }

    // --------------------------------------------------------- Private Methods

    // Tests whether the valiator tag is wrapping other tags.
    private boolean isWrapping() {

        // Would be nice if there was some easy way to determine whether
        // we are a leaf handler. However, even leaf handlers have a
        // non-null nextHandler - the CompilationUnit.LEAF instance.
        // We assume that if we've got a TagHandler or CompositeFaceletHandler
        // as our nextHandler, we are not a leaf.
        return owner.getValidatorConfig().getNextHandler() instanceof TagHandler
                || owner.getValidatorConfig().getNextHandler() instanceof CompositeFaceletHandler;

    }

    private void applyWrapping(FaceletContext ctx, UIComponent parent) throws IOException {

        ComponentValidators validators = ComponentValidators.getValidators(ctx.getFacesContext(), true);
        validators.pushValidatorInfo(createValidatorInfo(ctx));
        owner.getValidatorConfig().getNextHandler().apply(ctx, parent);
        validators.popValidatorInfo();

    }

    private void applyNested(FaceletContext ctx, UIComponent parent) {

        // only process if it's been created
        if (!ComponentHandler.isNew(parent)) {
            return;
        }

        if (parent instanceof EditableValueHolder) {
            applyAttachedObject(ctx.getFacesContext(), parent);
        } else if (UIComponent.isCompositeComponent(parent)) {
            if (null == owner.getFor()) {
                // PENDING(): I18N
                throw new TagException(owner.getTag(), "validator tags nested within composite components must have a non-null \"for\" attribute");
            }
            // Allow the composite component to know about the target
            // component.
            CompositeComponentTagHandler.getAttachedObjectHandlers(parent).add(owner);
        } else {
            throw new TagException(owner.getTag(), "Parent not an instance of EditableValueHolder: " + parent);
        }

    }

    /**
     * Template method for creating a Validator instance
     *
     * @param ctx FaceletContext to use
     * @return a new Validator instance
     */
    private Validator createValidator(FaceletContext ctx) {

        String id = owner.getValidatorId(ctx);
        if (id == null) {
            throw new TagException(owner.getTag(),
                    "A validator id was not specified. Typically the validator id is set in the constructor ValidateHandler(ValidatorConfig)");
        }
        return ctx.getFacesContext().getApplication().createValidator(id);

    }

}
