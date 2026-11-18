/*
 * Copyright (c) 2021, 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.mojarra.facelets.tag.faces.core;

import java.io.IOException;
import java.util.Map;

import jakarta.faces.component.TransientStateHelper;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIPanel;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;

import org.glassfish.mojarra.facelets.tag.TagHandlerImpl;
import org.glassfish.mojarra.facelets.tag.faces.ComponentSupport;

/**
 * Register a named facet on the UIComponent associated with the closest parent UIComponent custom action.
 *
 * See <a target="_new" href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/facet.html">tag documentation</a>.
 *
 * @author Jacob Hookom
 */
public final class FacetHandler extends TagHandlerImpl implements jakarta.faces.view.facelets.FacetHandler {

    public static final String KEY = "facelets.FACET_NAME";
    public static final String FACET_PASSTHROUGH_ATTRIBUTES = "facelets.FACET_PASSTHROUGH_ATTRIBUTES";

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

        recordBuildTimeDecision(ctx, name);

        String facetName = getFacetName(ctx);
        parent.getAttributes().put(KEY, facetName);

        try {
            nextHandler.apply(ctx, parent);
            UIComponent child = parent.getFacets().get(facetName);

            Map<String, Object> facetPassThroughAttributes = ComponentSupport.getPassthroughAttributes(ctx, tag);

            if (!facetPassThroughAttributes.isEmpty()) {
                if (child == null) {
                    child = ctx.getFacesContext().getApplication().createComponent(UIPanel.COMPONENT_TYPE);
                    parent.getFacets().put(facetName, child);
                }

                child.getTransientStateHelper().putTransient(FACET_PASSTHROUGH_ATTRIBUTES, facetPassThroughAttributes);
            }
        }
        finally {
            parent.getAttributes().remove(KEY);
        }
    }

    // jakarta.faces.view.facelets.tag.FacetHandler.getFacetName()
    // implementation
    @Override
    public String getFacetName(FaceletContext ctxt) {
        return name.getValue(ctxt);
    }

    /**
     * Returns the pass-through attributes of the <code>f:facet</code> tag which produced the given facet, or <code>null</code> when it has none. They belong on
     * the element which the renderer starts around the facet, not on the markup of the facet itself.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getFacetPassThroughAttributes(UIComponent possibleFacet) {
        TransientStateHelper helper = possibleFacet.getTransientStateHelper(false);
        return helper != null ? (Map<String, Object>) helper.getTransient(FACET_PASSTHROUGH_ATTRIBUTES) : null;
    }

}
