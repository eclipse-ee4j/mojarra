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

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.io.IOException;
import java.util.List;

import com.sun.faces.facelets.tag.MetaRulesetImpl;
import com.sun.faces.util.Util;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.behavior.Behavior;
import jakarta.faces.component.behavior.ClientBehavior;
import jakarta.faces.component.behavior.ClientBehaviorHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.AttachedObjectHandler;
import jakarta.faces.view.AttachedObjectTarget;
import jakarta.faces.view.BehaviorHolderAttachedObjectTarget;
import jakarta.faces.view.facelets.BehaviorHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.MetaRuleset;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagException;
import jakarta.faces.view.facelets.TagHandlerDelegate;

/**
 *
 * @author edburns
 */
class BehaviorTagHandlerDelegateImpl extends TagHandlerDelegate implements AttachedObjectHandler {

    private BehaviorHandler owner;

    public BehaviorTagHandlerDelegateImpl(BehaviorHandler owner) {
        this.owner = owner;
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        // only process if it's been created
        if (parent == null || !(parent.getParent() == null)) {
            return;
        }
        ComponentSupport.copyPassthroughAttributes(ctx, parent, owner.getTag());

        if (UIComponent.isCompositeComponent(parent)) {
            // Check composite component event name:
            BeanInfo componentBeanInfo = (BeanInfo) parent.getAttributes().get(UIComponent.BEANINFO_KEY);
            if (null == componentBeanInfo) {
                throw new TagException(owner.getTag(), "Error: enclosing composite component does not have BeanInfo attribute");
            }
            BeanDescriptor componentDescriptor = componentBeanInfo.getBeanDescriptor();
            if (null == componentDescriptor) {
                throw new TagException(owner.getTag(), "Error: enclosing composite component BeanInfo does not have BeanDescriptor");
            }
            List<AttachedObjectTarget> targetList = (List<AttachedObjectTarget>) componentDescriptor.getValue(AttachedObjectTarget.ATTACHED_OBJECT_TARGETS_KEY);
            if (null == targetList) {
                throw new TagException(owner.getTag(), "Error: enclosing composite component does not support behavior events");
            }
            String eventName = owner.getEventName();
            boolean supportedEvent = false;
            for (AttachedObjectTarget target : targetList) {
                if (target instanceof BehaviorHolderAttachedObjectTarget) {
                    BehaviorHolderAttachedObjectTarget behaviorTarget = (BehaviorHolderAttachedObjectTarget) target;
                    if (null != eventName && eventName.equals(behaviorTarget.getName()) || null == eventName && behaviorTarget.isDefaultEvent()) {
                        supportedEvent = true;
                        break;
                    }
                }
            }
            if (supportedEvent) {
                CompositeComponentTagHandler.getAttachedObjectHandlers(parent).add(owner);
            } else {
                throw new TagException(owner.getTag(), "Error: enclosing composite component does not support event " + eventName);
            }

        } else if (parent instanceof ClientBehaviorHolder) {
            owner.applyAttachedObject(ctx.getFacesContext(), parent);
        } else {
            throw new TagException(owner.getTag(), "Parent not an instance of ClientBehaviorHolder: " + parent);
        }

    }

    @Override
    public void applyAttachedObject(FacesContext context, UIComponent parent) {
        FaceletContext ctx = (FaceletContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
        // cast to the ClientBehaviorHolder.
        ClientBehaviorHolder behaviorHolder = (ClientBehaviorHolder) parent;
        ValueExpression bindingExpr = null;
        Behavior behavior = null;
        if (null != owner.getBinding()) {
            bindingExpr = owner.getBinding().getValueExpression(ctx, Behavior.class);
            behavior = (Behavior) bindingExpr.getValue(ctx);
        }
        if (null == behavior) {
            if (null != owner.getBehaviorId()) {
                behavior = ctx.getFacesContext().getApplication().createBehavior(owner.getBehaviorId());
                if (null == behavior) {
                    throw new TagException(owner.getTag(), "No Faces behavior defined for Id " + owner.getBehaviorId());
                }
                if (null != bindingExpr) {
                    bindingExpr.setValue(ctx, behavior);
                }
            } else {
                throw new TagException(owner.getTag(), "No behaviorId defined");
            }
        }
        owner.setAttributes(ctx, behavior);

        if (behavior instanceof ClientBehavior) {
            behaviorHolder.addClientBehavior(getEventName(behaviorHolder), (ClientBehavior) behavior);
        }
    }

    @Override
    public MetaRuleset createMetaRuleset(Class type) {
        Util.notNull("type", type);
        MetaRuleset m = new MetaRulesetImpl(owner.getTag(), type);
        m = m.ignore("event");
        return m.ignore("binding").ignore("for");
    }

    @Override
    public String getFor() {
        String result = null;
        TagAttribute attr = owner.getTagAttribute("for");

        if (null != attr) {
            result = attr.getValue();
        }
        return result;

    }

    private String getEventName(ClientBehaviorHolder holder) {
        String eventName;
        if (null != owner.getEvent()) {
            eventName = owner.getEvent().getValue();
        } else {
            eventName = holder.getDefaultEventName();
        }
        if (null == eventName) {
            throw new TagException(owner.getTag(), "The event name is not defined");
        }
        return eventName;
    }

}
