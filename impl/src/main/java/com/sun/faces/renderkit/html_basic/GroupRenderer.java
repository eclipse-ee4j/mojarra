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

import com.sun.faces.renderkit.Attributes;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.html.HtmlPanelGroup;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

/**
 * Arbitrary grouping "renderer" that simply renders its children recursively in the <code>encodeEnd()</code> method.
 *
 */
public class GroupRenderer extends HtmlBasicRenderer {

    private static final Attributes ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.PANELGROUP);
    // ---------------------------------------------------------- Public Methods

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (!shouldEncode(component)) {
            return;
        }
        // Render a span around this group if necessary
        String styleClass = getStyleClass(component);
        ResponseWriter writer = context.getResponseWriter();

        if (divOrSpan(component, styleClass)) {
            writer.startElement(getElementName(component), component);
            writeIdAttributeIfNecessary(context, writer, component);
            if (styleClass != null) {
                writer.writeAttribute("class", styleClass, "styleClass");
            }
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

        RenderKitUtils.flushPendingBehaviorEventListeners(context, component, null);

        // Close our span element if necessary
        ResponseWriter writer = context.getResponseWriter();
        String styleClass = getStyleClass(component);
        if (divOrSpan(component, styleClass)) {
            writer.endElement(getElementName(component));
        }

    }

    @Override
    public boolean getRendersChildren() {

        return true;

    }

    // --------------------------------------------------------- Private Methods

    /**
     * @param component <code>UIComponent</code> for this group
     * @param styleClass the already-resolved {@code styleClass} value
     *
     * @return <code>true</code> if we need to render a div or span element around this group.
     */
    private boolean divOrSpan(UIComponent component, String styleClass) {

        return shouldWriteIdAttribute(component) || styleClass != null || getStyle(component) != null;

    }

    /**
     * @return the element name to wrap this group in, which is a {@code div} when its layout is {@code block} and a
     * {@code span} otherwise.
     */
    private static String getElementName(UIComponent component) {

        String layout = component instanceof HtmlPanelGroup group ? group.getLayout() : (String) component.getAttributes().get("layout");
        return "block".equals(layout) ? "div" : "span";

    }

    private static String getStyleClass(UIComponent component) {

        return component instanceof HtmlPanelGroup group ? group.getStyleClass() : (String) RenderKitUtils.getAttributeIfSet(component, "styleClass");

    }

    private static String getStyle(UIComponent component) {

        return component instanceof HtmlPanelGroup group ? group.getStyle() : (String) RenderKitUtils.getAttributeIfSet(component, "style");

    }

}
