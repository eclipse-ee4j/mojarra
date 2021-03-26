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

package com.sun.faces.facelets.tag.ui;

import java.io.IOException;

import com.sun.faces.facelets.tag.TagHandlerImpl;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;

/**
 * @author Jacob Hookom
 */
public final class DefineHandler extends TagHandlerImpl {

    private final String name;

    /**
     * @param config
     */
    public DefineHandler(TagConfig config) {
        super(config);
        TagAttribute attr = getRequiredAttribute("name");
        if (!attr.isLiteral()) {
            FacesContext context = FacesContext.getCurrentInstance();
            FaceletContext ctx = (FaceletContext) context.getAttributes().get(FaceletContext.FACELET_CONTEXT_KEY);
            name = (String) attr.getValueExpression(ctx, String.class).getValue(ctx);
        } else {
            name = attr.getValue();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sun.facelets.FaceletHandler#apply(com.sun.facelets.FaceletContext, jakarta.faces.component.UIComponent)
     */
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        // no-op
        // this.nextHandler.apply(ctx, parent);
    }

    public void applyDefinition(FaceletContext ctx, UIComponent parent) throws IOException {
        nextHandler.apply(ctx, parent);
    }

    public String getName() {
        return name;
    }
}
