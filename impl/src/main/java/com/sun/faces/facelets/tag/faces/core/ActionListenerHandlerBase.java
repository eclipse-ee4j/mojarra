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

import com.sun.faces.facelets.tag.TagHandlerImpl;
import com.sun.faces.facelets.tag.faces.CompositeComponentTagHandler;

import jakarta.faces.application.Resource;
import jakarta.faces.component.ActionSource;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ActionSourceAttachedObjectHandler;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;

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
public abstract class ActionListenerHandlerBase extends TagHandlerImpl implements ActionSourceAttachedObjectHandler {

    /**
     * @param config
     */
    public ActionListenerHandlerBase(TagConfig config) {
        super(config);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.facelets.FaceletHandler#apply(com.sun.facelets.FaceletContext, jakarta.faces.component.UIComponent)
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        if (null == parent || !ComponentHandler.isNew(parent)) {
            return;
        }

        if (parent instanceof ActionSource) {
            applyAttachedObject(ctx.getFacesContext(), parent);
        } else if (parent.getAttributes().containsKey(Resource.COMPONENT_RESOURCE_KEY)) {
            if (null == getFor()) {
                // PENDING(): I18N
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
    public abstract void applyAttachedObject(FacesContext context, UIComponent parent);

    @Override
    public String getFor() {
        String result = null;
        TagAttribute attr = getAttribute("for");

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

}
