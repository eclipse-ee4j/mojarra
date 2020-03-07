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
import java.util.Iterator;
import java.util.Map;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;
import jakarta.faces.render.Renderer;

public class PassthroughRenderer extends HtmlBasicRenderer {

// We are purposely piggy backing off the PANELGROUP attributes since they are
// identical for this renderer.
    private static final Attribute[] ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.PANELGROUP);

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (!shouldEncode(component)) {
            return;
        }

        Map<String, Object> attrs = component.getPassThroughAttributes();
        String localName = (String) attrs.get(Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY);
        if (null == localName) {
            String clientId = component.getClientId(context);
            throw new FacesException("Unable to determine localName for component with clientId " + clientId);
        }

        ResponseWriter writer = context.getResponseWriter();
        writer.startElement(localName, component);

        writeIdAttributeIfNecessary(context, writer, component);

        RenderKitUtils.renderPassThruAttributes(context, writer, component, ATTRIBUTES);

    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        rendererParamsNotNull(context, component);

        if (!shouldEncodeChildren(component)) {
            return;
        }

        // Render our children recursively
        Iterator<UIComponent> kids = getChildren(component);
        while (kids.hasNext()) {
            encodeRecursive(context, kids.next());
        }

    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (!shouldEncode(component)) {
            return;
        }

        Map<String, Object> attrs = component.getPassThroughAttributes();
        String localName = (String) attrs.get(Renderer.PASSTHROUGH_RENDERER_LOCALNAME_KEY);
        context.getResponseWriter().endElement(localName);
    }

    @Override
    public boolean getRendersChildren() {
        return true;
    }

}
