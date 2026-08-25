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

package org.glassfish.mojarra.renderkit.html_basic;

import java.io.IOException;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.glassfish.mojarra.renderkit.RenderKitUtils;

/**
 * <p>
 * This <code>Renderer</code> handles the rendering of <code>stylesheet</code> references.
 * </p>
 */
public class StylesheetRenderer extends ScriptStyleBaseRenderer {

    public static final String RENDERER_TYPE = "jakarta.faces.resource.Stylesheet";

    public static final String FILE_EXTENSION = ".css";

    public static final String DEFAULT_CONTENT_TYPE = "text/css";

    @Override
    protected void startInlineElement(FacesContext context, ResponseWriter writer, UIComponent component) throws IOException {
        writer.startElement("style", component);
        writeTypeAttributeIfNecessary(context, writer);
    }

    private void writeTypeAttributeIfNecessary(FacesContext context, ResponseWriter writer) throws IOException {
        if (!RenderKitUtils.isOutputHtml5Doctype(context)) {
            writer.writeAttribute("type", DEFAULT_CONTENT_TYPE, "type");
        }
    }

    @Override
    protected void endInlineElement(ResponseWriter writer, UIComponent component) throws IOException {
        writer.endElement("style");
    }

    @Override
    protected void startExternalElement(FacesContext context, ResponseWriter writer, UIComponent component) throws IOException {
        writer.startElement("link", component);
        writer.writeAttribute("rel", "stylesheet", "rel");
        writeTypeAttributeIfNecessary(context, writer);
    }

    @Override
    protected void endExternalElement(ResponseWriter writer, UIComponent component, String resourceUrl) throws IOException {
        writer.writeURIAttribute("href", resourceUrl, "href");

        String media = (String) component.getAttributes().get("media");
        if (media != null) {
            writer.writeAttribute("media", media, "media");
        }

        writer.endElement("link");
    }

    @Override
    protected String verifyTarget(String toVerify) {
        return "head";
    }

}
