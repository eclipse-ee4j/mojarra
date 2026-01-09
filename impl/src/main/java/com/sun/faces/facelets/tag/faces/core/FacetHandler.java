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
import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIPanel;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;

import com.sun.faces.facelets.tag.TagHandlerImpl;
import com.sun.faces.facelets.tag.faces.ComponentSupport;

/**
 * Register a named facet on the UIComponent associated with the closest parent UIComponent custom action.
 * 
 * See <a target="_new" href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/facet.html">tag
 * documentation</a>.
 *
 * @author Jacob Hookom
 * @version $Id$
 */
public final class FacetHandler extends TagHandlerImpl implements jakarta.faces.view.facelets.FacetHandler {

    public static final String KEY = "facelets.FACET_NAME";
    public static final String FACET_HAS_PASSTHROUGH_ATTRIBUTES = "facelets.FACET_HAS_PASSTHROUGH_ATTRIBUTES";

    protected final TagAttribute name;

    public FacetHandler(TagConfig config) {
        super(config);
        name = getRequiredAttribute("name");
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.facelets.FaceletHandler#apply(com.sun.facelets.FaceletContext, jakarta.faces.component.UIComponent)
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        if (parent == null) {
            throw new TagException(tag, "Parent UIComponent was null");
        }

        String facetName = getFacetName(ctx);
        parent.getAttributes().put(KEY, facetName);

        try {
            nextHandler.apply(ctx, parent);
            UIComponent child = parent.getFacets().get(facetName);

            if (child == null && ComponentSupport.hasPassthroughAttributes(tag)) {
                child = ctx.getFacesContext().getApplication().createComponent(UIPanel.COMPONENT_TYPE);
                parent.getFacets().put(facetName, child);
            }

            if (child != null) {
                ComponentSupport.copyPassthroughAttributes(ctx, child, tag);
                Map<String, Object> passThroughAttributes = child.getPassThroughAttributes(false);

                if (passThroughAttributes != null && !passThroughAttributes.isEmpty()) {
                    child.getTransientStateHelper().putTransient(FACET_HAS_PASSTHROUGH_ATTRIBUTES, true);
                }
            }
        } finally {
            parent.getAttributes().remove(KEY);
        }
    }

    // jakarta.faces.view.facelets.tag.FacetHandler.getFacetName()
    // implementation
    @Override
    public String getFacetName(FaceletContext ctxt) {
        return name.getValue(ctxt);
    }

    public static boolean hasFacetPassThroughAttributes(UIComponent possibleFacet) {
        return possibleFacet.getTransientStateHelper().getTransient(FACET_HAS_PASSTHROUGH_ATTRIBUTES) != null;
    }
}
