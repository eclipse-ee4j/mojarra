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
import java.util.ListIterator;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

/**
 * <p>
 * This <code>Renderer</code> is responsible for rendering the standard HTML body element as well as rendering any
 * resources that should be output before the <code>body</code> tag is closed.
 * </p>
 */
public class BodyRenderer extends HtmlBasicRenderer {

    private static final Attribute[] BODY_ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.OUTPUTBODY);

    @Override
    public void decode(FacesContext context, UIComponent component) {
        // no-op
    }

    /**
     * Encode the beginning.
     *
     * @param context the Faces context.
     * @param component the UI component.
     * @throws IOException when an I/O error occurs.
     */
    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        writer.startElement("body", component);
        if (RenderKitUtils.isOutputHtml5Doctype(context)) {
            writeIdAttributeIfNecessary(context, writer, component);
        }
        String styleClass = (String) component.getAttributes().get("styleClass");
        if (styleClass != null && styleClass.length() != 0) {
            writer.writeAttribute("class", styleClass, "styleClass");
        }
        RenderKitUtils.renderPassThruAttributes(context, writer, component, BODY_ATTRIBUTES);
    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {
        // no-op
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
        ResponseWriter writer = context.getResponseWriter();
        UIViewRoot viewRoot = context.getViewRoot();
        ListIterator iter = viewRoot.getComponentResources(context, "body").listIterator();
        while (iter.hasNext()) {
            UIComponent resource = (UIComponent) iter.next();
            resource.encodeAll(context);
        }
        RenderKitUtils.renderUnhandledMessages(context);
        writer.endElement("body");
    }

    /**
     * Do we render our children.
     *
     * @return false.
     */
    @Override
    public boolean getRendersChildren() {
        return false;
    }
}
