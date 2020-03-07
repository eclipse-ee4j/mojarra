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

import java.util.Map;

import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagException;

public class RenderFacetHandler extends ComponentHandler {

    // Supported attribute names
    private static final String NAME_ATTRIBUTE = "name";
    private static final String REQUIRED_ATTRIBUTE = "required";

    // Attributes

    // This attribute is required.
    TagAttribute name;

    // This attribute is not required. If not defined, then assume the facet
    // isn't necessary.
    TagAttribute required;

    // ------------------------------------------------------------ Constructors

    public RenderFacetHandler(ComponentConfig config) {
        super(config);
        name = getRequiredAttribute(NAME_ATTRIBUTE);
        required = getAttribute(REQUIRED_ATTRIBUTE);
    }

    // ------------------------------------------------- Methods from TagHandler

    @Override
    public void onComponentPopulated(FaceletContext ctx, UIComponent c, UIComponent parent) {

        UIComponent compositeParent = UIComponent.getCurrentCompositeComponent(ctx.getFacesContext());
        if (compositeParent == null) {
            return;
        }
        boolean requiredValue = required != null && required.getBoolean(ctx);
        String nameValue = name.getValue(ctx);

        if (compositeParent.getFacetCount() == 0 && requiredValue) {
            throwRequiredException(ctx, nameValue, compositeParent);
        }

        Map<String, UIComponent> facetMap = compositeParent.getFacets();
        c.getAttributes().put(UIComponent.FACETS_KEY, nameValue);
        if (requiredValue && !facetMap.containsKey(nameValue)) {
            throwRequiredException(ctx, nameValue, compositeParent);
        }

    }

    // --------------------------------------------------------- Private Methods

    private void throwRequiredException(FaceletContext ctx, String name, UIComponent compositeParent) {

        throw new TagException(tag, "Unable to find facet named '" + name + "' in parent composite component with id '"
                + compositeParent.getClientId(ctx.getFacesContext()) + '\'');

    }

}
