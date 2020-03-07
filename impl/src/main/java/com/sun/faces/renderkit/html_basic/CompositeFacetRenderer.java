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

package com.sun.faces.renderkit.html_basic;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;
import com.sun.faces.util.Util;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.Renderer;

/**
 * <p>
 * This <code>Renderer</code> is responsible for rendering the content of a facet defined within the <em>using page</em>
 * template in the desired location within the composite component implementation section.
 * </p>
 */
public class CompositeFacetRenderer extends Renderer {

    private static final Logger logger = FacesLogger.RENDERKIT.getLogger();

    // --------------------------------------------------- Methods from Renderer

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {

        Util.notNull("context", context);
        Util.notNull("component", component);

        String facetName = (String) component.getAttributes().get(UIComponent.FACETS_KEY);
        if (null == facetName) {
            return;
        }

        UIComponent currentCompositeComponent = UIComponent.getCurrentCompositeComponent(context);
        if (null != currentCompositeComponent) {
            UIComponent facet = currentCompositeComponent.getFacet(facetName);
            if (null != facet) {
                facet.encodeAll(context);
            } else {
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Could not find facet named {0}", facetName);
                }
            }
        }
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

}
