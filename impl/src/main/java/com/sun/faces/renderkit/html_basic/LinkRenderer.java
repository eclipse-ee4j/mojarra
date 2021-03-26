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

// LinkRenderer.java

package com.sun.faces.renderkit.html_basic;

import java.io.IOException;
import java.util.logging.Level;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

/**
 * <B>LinkRenderer</B> acts as superclass for CommandLinkRenderer and OutputLinkRenderer.
 */

public abstract class LinkRenderer extends HtmlBasicRenderer {

    private static final Attribute[] ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.COMMANDLINK);

    // ------------------------------------------------------- Protected Methods

    protected abstract void renderAsActive(FacesContext context, UIComponent component) throws IOException;

    protected void renderAsDisabled(FacesContext context, UIComponent component) throws IOException {

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        writer.startElement("span", component);
        String writtenId = writeIdAttributeIfNecessary(context, writer, component);
        if (null != writtenId) {
            writer.writeAttribute("name", writtenId, "name");
        }

        RenderKitUtils.renderPassThruAttributes(context, writer, component, ATTRIBUTES);

        writeCommonLinkAttributes(writer, component);
        writeValue(component, writer);
        writer.flush();

    }

    protected void writeCommonLinkAttributes(ResponseWriter writer, UIComponent component) throws IOException {

        // handle styleClass
        String styleClass = (String) component.getAttributes().get("styleClass");
        if (styleClass != null) {
            writer.writeAttribute("class", styleClass, "styleClass");
        }

    }

    protected void writeValue(UIComponent component, ResponseWriter writer) throws IOException {

        Object v = getValue(component);
        String label = null;
        if (v != null) {
            label = v.toString();
        }

        if (label != null && label.length() != 0) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Value to be rendered " + label);
            }
            writer.writeText(label, component, null);
        }

    }

} // end of class LinkRenderer
