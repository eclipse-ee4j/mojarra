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

import com.sun.faces.facelets.tag.TagHandlerImpl;

import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;

public final class PassThroughAttributesHandler extends TagHandlerImpl implements jakarta.faces.view.facelets.AttributeHandler {

    private final TagAttribute value;

    public PassThroughAttributesHandler(TagConfig config) {
        super(config);
        value = getRequiredAttribute("value");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        if (parent == null) {
            throw new TagException(tag, "Parent UIComponent was null");
        }

        // only process if the parent is new to the tree
        if (parent.getParent() == null) {
            Map<String, Object> componentPassThroughAttrs = parent.getPassThroughAttributes(true);
            Map<String, Object> tagPassThroughAttrs = (Map<String, Object>) value.getObject(ctx, Map.class);
            for (Map.Entry<String, Object> cur : tagPassThroughAttrs.entrySet()) {
                componentPassThroughAttrs.put(cur.getKey(), cur.getValue());
            }
        }
    }

    // jakarta.faces.view.facelets.tag.AttributeHandler.getAttributeName()
    // implementation.
    @Override
    public String getAttributeName(FaceletContext ctxt) {
        return "value";
    }
}
