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

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.io.IOException;
import java.util.List;

import com.sun.faces.application.view.FaceletViewHandlingStrategy;
import com.sun.faces.facelets.tag.TagHandlerImpl;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.AttachedObjectTarget;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;

public abstract class AttachedObjectTargetHandler extends TagHandlerImpl {

    private TagAttribute name = null;
    private TagAttribute targets = null;

    public AttachedObjectTargetHandler(TagConfig config) {
        super(config);
        name = getRequiredAttribute("name");
        targets = getAttribute("targets");
    }

    abstract AttachedObjectTargetImpl newAttachedObjectTargetImpl();

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {

        assert ctx.getFacesContext().getAttributes().containsKey(FaceletViewHandlingStrategy.IS_BUILDING_METADATA);

        // only process if it's been created
        if (null == parent || null == (parent = parent.getParent()) || !ComponentHandler.isNew(parent)) {
            return;
        }

        BeanInfo componentBeanInfo = (BeanInfo) parent.getAttributes().get(UIComponent.BEANINFO_KEY);
        if (null == componentBeanInfo) {
            throw new TagException(tag, "Error: I have an EditableValueHolder tag, but no enclosing composite component");
        }
        BeanDescriptor componentDescriptor = componentBeanInfo.getBeanDescriptor();
        if (null == componentDescriptor) {
            throw new TagException(tag, "Error: I have an EditableValueHolder tag, but no enclosing composite component");
        }

        List<AttachedObjectTarget> targetList = (List<AttachedObjectTarget>) componentDescriptor.getValue(AttachedObjectTarget.ATTACHED_OBJECT_TARGETS_KEY);
        AttachedObjectTargetImpl target = newAttachedObjectTargetImpl();
        targetList.add(target);

        ValueExpression ve = name.getValueExpression(ctx, String.class);
        String strValue = (String) ve.getValue(ctx);
        if (null != strValue) {
            target.setName(strValue);
        }

        if (null != targets) {
            ve = targets.getValueExpression(ctx, String.class);
            target.setTargetsList(ve);
        }

        nextHandler.apply(ctx, parent);

    }

}
