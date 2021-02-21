/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.facelets.tag.faces.html;

import static com.sun.faces.facelets.tag.faces.ComponentSupport.MARK_CREATED;

import java.util.List;

import com.sun.faces.facelets.tag.faces.ComponentSupport;
import com.sun.faces.facelets.tag.faces.ComponentTagHandlerDelegateImpl;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributes;

/**
 * This class overrides key methods from <code>ComponentTagHandlerDelegateImpl</code> in order to properly find existing
 * component resources as well as properly handling the case when this concrete implementations of this class are
 * applied more than once for a particular request.
 */
public abstract class ComponentResourceDelegate extends ComponentTagHandlerDelegateImpl {

    private TagAttributes attributes;

    // ------------------------------------------------------------ Constructors

    public ComponentResourceDelegate(ComponentHandler owner) {

        super(owner);
        attributes = owner.getTag().getAttributes();

    }

    // ----------------------------------------- Methods from TagHandlerDelegate

    @Override
    protected UIComponent findChild(FaceletContext ctx, UIComponent parent, String tagId) {

        // If we have a target for this particular component, we need to
        // query the UIViewRoot's component resources, otherwise defer
        // to our super class.
        String target = getLocationTarget(ctx);
        if (target != null) {
            final UIViewRoot root = ctx.getFacesContext().getViewRoot();
            List<UIComponent> resources = root.getComponentResources(ctx.getFacesContext(), target);
            for (UIComponent c : resources) {
                String cid = (String) c.getAttributes().get(MARK_CREATED);
                if (tagId.equals(cid)) {
                    return c;
                }
            }
            return null;
        } else {
            return super.findChild(ctx, parent, tagId);
        }

    }

    @Override
    protected void addComponentToView(FaceletContext ctx, UIComponent parent, UIComponent c, boolean componentFound) {

        if (!componentFound) {
            // default to the existing logic which will add the component
            // in-place. An event will be fired to move the component
            // as a UIViewRoot component resource
            super.addComponentToView(ctx, parent, c, componentFound);
        } else {
            // when re-applying we supress events for existing components,
            // so if we simply relied on the default logic, the resources
            // wouldn't be be moved. We'll do it manually instead.
            String target = getLocationTarget(ctx);
            if (target != null) {
                final UIViewRoot root = ctx.getFacesContext().getViewRoot();
                root.addComponentResource(ctx.getFacesContext(), c, target);
            } else {
                super.addComponentToView(ctx, parent, c, componentFound);
            }
        }

    }

    @Override
    protected void doOrphanedChildCleanup(FaceletContext ctx, UIComponent parent, UIComponent c) {

        FacesContext context = ctx.getFacesContext();
        boolean suppressEvents = ComponentSupport.suppressViewModificationEvents(context);
        if (suppressEvents) {
            // if the component has already been found, it will be removed
            // and added back to the view. We don't want to publish events
            // for this case.
            context.setProcessingEvents(false);
        }

        ComponentSupport.finalizeForDeletion(c);
        UIViewRoot root = context.getViewRoot();
        root.removeComponentResource(context, c, getLocationTarget(ctx));

        if (suppressEvents) {
            // restore the original state
            context.setProcessingEvents(true);
        }

    }

    // ------------------------------------------------------- Protected Methods

    protected abstract String getLocationTarget(FaceletContext ctx);

    protected TagAttribute getAttribute(String name) {

        return attributes.get(name);

    }

}
