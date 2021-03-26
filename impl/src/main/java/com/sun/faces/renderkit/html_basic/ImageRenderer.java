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

// ImageRenderer.java

package com.sun.faces.renderkit.html_basic;

import java.io.IOException;
import java.util.logging.Level;

import com.sun.faces.RIConstants;
import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

/**
 * <B>ImageRenderer</B> is a class that handles the rendering of the graphic ImageTag
 *
 */

public class ImageRenderer extends HtmlBasicRenderer {

    private static final Attribute[] ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.GRAPHICIMAGE);

    // ---------------------------------------------------------- Public Methods

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (!shouldEncode(component)) {
            return;
        }

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        writer.startElement("img", component);
        writeIdAttributeIfNecessary(context, writer, component);
        writer.writeURIAttribute("src", RenderKitUtils.getImageSource(context, component, "value"), "value");
        // if we're writing XHTML and we have a null alt attribute
        if (writer.getContentType().equals(RIConstants.XHTML_CONTENT_TYPE) && null == component.getAttributes().get("alt")) {
            // write out an empty alt
            writer.writeAttribute("alt", "", "alt");
        }

        RenderKitUtils.renderPassThruAttributes(context, writer, component, ATTRIBUTES);
        RenderKitUtils.renderXHTMLStyleBooleanAttributes(writer, component);
        String styleClass;
        if (null != (styleClass = (String) component.getAttributes().get("styleClass"))) {
            writer.writeAttribute("class", styleClass, "styleClass");
        }
        writer.endElement("img");
        if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, "End encoding component " + component.getId());
        }

    }

    // --------------------------------------------------------- Private Methods

    // The testcase for this class is TestRenderers_2.java

} // end of class ImageRenderer
