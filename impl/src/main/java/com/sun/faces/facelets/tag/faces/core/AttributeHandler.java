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

import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;

/**
 * Sets the specified name and attribute on the parent UIComponent. If the "value" specified is not a literal, it will
 * instead set the ValueExpression on the UIComponent.
 * <p />
 * See <a target="_new" href="http://java.sun.com/j2ee/javaserverfaces/1.1_01/docs/tlddocs/f/attribute.html">tag
 * documentation</a>.
 *
 * @see jakarta.faces.component.UIComponent#getAttributes()
 * @see jakarta.faces.component.UIComponent#setValueExpression(java.lang.String, jakarta.el.ValueExpression)
 * @author Jacob Hookom
 */
public final class AttributeHandler extends TagHandlerImpl implements jakarta.faces.view.facelets.AttributeHandler {

    private final TagAttribute name;

    private final TagAttribute value;

    /**
     * @param config
     */
    public AttributeHandler(TagConfig config) {
        super(config);
        name = getRequiredAttribute("name");
        value = getRequiredAttribute("value");
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

        // only process if the parent is new to the tree
        if (parent.getParent() == null) {
            String n = getAttributeName(ctx);
            if (!parent.getAttributes().containsKey(n)) {
                if (value.isLiteral()) {
                    parent.getAttributes().put(n, value.getValue());
                } else {
                    parent.setValueExpression(n, value.getValueExpression(ctx, Object.class));
                }
            }
        }
    }

    // jakarta.faces.view.facelets.tag.AttributeHandler.getAttributeName()
    // implementation.
    @Override
    public String getAttributeName(FaceletContext ctxt) {
        return name.getValue(ctxt);
    }
}
