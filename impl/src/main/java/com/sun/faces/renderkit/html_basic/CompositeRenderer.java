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
import java.util.Map;

import com.sun.faces.util.Util;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.Renderer;

/**
 * <p>
 * This <code>Renderer</code> is responsible for rendering the children defined within the composite implementation
 * section of a composite component template.
 * </p>
 */
public class CompositeRenderer extends Renderer {

    // --------------------------------------------------- Methods from Renderer

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {

        Util.notNull("context", context);
        Util.notNull("component", component);

        Map<String, UIComponent> facets = component.getFacets();
        UIComponent compositeRoot = facets.get(UIComponent.COMPOSITE_FACET_NAME);
        if (null == compositeRoot) {
            throw new IOException("PENDING_I18N: Unable to find composite " + " component root for composite component with id " + component.getId()
                    + " and class " + component.getClass().getName());
        }
        compositeRoot.encodeAll(context);

    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

}
