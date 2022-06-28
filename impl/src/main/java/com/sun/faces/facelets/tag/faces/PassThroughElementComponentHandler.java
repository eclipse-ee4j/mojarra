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

package com.sun.faces.facelets.tag.faces;

import java.util.Map;

import com.sun.faces.util.Util;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.render.Renderer;
import jakarta.faces.view.facelets.ComponentConfig;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagException;

public class PassThroughElementComponentHandler extends ComponentHandler {

    private final TagAttribute elementName;

    protected final TagAttribute getRequiredPassthroughAttribute(String localName) throws TagException {
        for (String namespace : PassThroughAttributeLibrary.NAMESPACES) {
            TagAttribute attr = tag.getAttributes().get(namespace, localName);

            if (attr != null) {
                return attr;
            }
        }

        throw new TagException(tag, "Attribute '" + localName + "' is required");
    }

    public PassThroughElementComponentHandler(ComponentConfig config) {
        super(config);

        elementName = getRequiredPassthroughAttribute(Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY);
    }

    @Override
    public UIComponent createComponent(FaceletContext ctx) {
        UIComponent result = null;
        try {
            Class<?> clazz = Util.loadClass("com.sun.faces.component.PassthroughElement", this);
            result = (UIComponent) clazz.newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException cnfe) {
            throw new FacesException(cnfe);
        }

        return result;
    }

    @Override
    public void onComponentCreated(FaceletContext ctx, UIComponent c, UIComponent parent) {
        if (parent.getParent() == null) {
            Map<String, Object> passThroughAttrs = c.getPassThroughAttributes(true);
            Object attrValue;
            attrValue = elementName.isLiteral() ? elementName.getValue(ctx) : elementName.getValueExpression(ctx, Object.class);
            passThroughAttrs.put(Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY, attrValue);
        }

    }

}
