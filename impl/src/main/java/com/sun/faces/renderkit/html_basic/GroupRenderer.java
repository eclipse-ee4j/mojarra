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

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

/**
 * Arbitrary grouping "renderer" that simply renders its children recursively in the <code>encodeEnd()</code> method.
 *
 */
public class GroupRenderer extends HtmlBasicRenderer {

    private static final Attribute[] ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.PANELGROUP);
    // ---------------------------------------------------------- Public Methods

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (!shouldEncode(component)) {
            return;
        }
        // Render a span around this group if necessary
        String style = (String) component.getAttributes().get("style");
        String styleClass = (String) component.getAttributes().get("styleClass");
        ResponseWriter writer = context.getResponseWriter();

        if (divOrSpan(component)) {
            if ("block".equals(component.getAttributes().get("layout"))) {
                writer.startElement("div", component);
            } else {
                writer.startElement("span", component);
            }
            writeIdAttributeIfNecessary(context, writer, component);
            if (styleClass != null) {
                writer.writeAttribute("class", styleClass, "styleClass");
            }
            // JAVASERVERFACES-3270: do not manually render "style" as it is handled
            // in renderPassThruAttributes().
        }

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

        // Close our span element if necessary
        ResponseWriter writer = context.getResponseWriter();
        if (divOrSpan(component)) {
            if ("block".equals(component.getAttributes().get("layout"))) {
                writer.endElement("div");
            } else {
                writer.endElement("span");
            }
        }

    }

    @Override
    public boolean getRendersChildren() {

        return true;

    }

    // --------------------------------------------------------- Private Methods

    /**
     * @param component <code>UIComponent</code> for this group
     *
     * @return <code>true</code> if we need to render a div or span element around this group.
     */
    private boolean divOrSpan(UIComponent component) {

        return shouldWriteIdAttribute(component) || component.getAttributes().get("style") != null || component.getAttributes().get("styleClass") != null;

    }

}
