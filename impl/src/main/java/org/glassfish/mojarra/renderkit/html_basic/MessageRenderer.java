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

// MessageRenderer.java

package org.glassfish.mojarra.renderkit.html_basic;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIMessage;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.html.HtmlMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

import org.glassfish.mojarra.renderkit.RenderKitUtils;

/**
 * <p>
 * <B>MessageRenderer</B> handles rendering for the Message
 * <p>
 * .
 *
 */

public class MessageRenderer extends HtmlBasicRenderer {

    private OutputMessageRenderer omRenderer = null;

    // ------------------------------------------------------------ Constructors

    public MessageRenderer() {

        omRenderer = new OutputMessageRenderer();

    }

    // ---------------------------------------------------------- Public Methods

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (component instanceof UIOutput) {
            omRenderer.encodeBegin(context, component);
        }

    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (component instanceof UIOutput) {
            omRenderer.encodeChildren(context, component);
        }

    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (component instanceof UIOutput) {
            omRenderer.encodeEnd(context, component);
            return;
        }

        if (!shouldEncode(component)) {
            return;
        }

        // If id is user specified, we must render
        boolean mustRender = shouldWriteIdAttribute(component);

        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        UIMessage message = (UIMessage) component;

        String clientId = message.getFor();

        // "for" attribute required for Message.
        if (clientId == null) {
            logger.warning("'for' attribute cannot be null");
            return;
        }

        clientId = augmentIdReference(clientId, component);
        Iterator<FacesMessage> messageIter = getMessageIter(context, clientId, component);

        assert messageIter != null;
        if (!messageIter.hasNext()) {
            if (mustRender) {
                // no message to render, but must render anyway
                writer.startElement("span", component);
                writeIdAttributeIfNecessary(context, writer, component);
                writer.endElement("span");
            } // otherwise, return without rendering
            return;
        }
        FacesMessage curMessage = messageIter.next();
        if (curMessage.isRendered() && !message.isRedisplay()) {
            return;
        }
        curMessage.rendered();

        HtmlMessage htmlMessage = component instanceof HtmlMessage ? (HtmlMessage) component : null;
        String severityStyle = null;
        String severityStyleClass = null;
        boolean showSummary = message.isShowSummary();
        boolean showDetail = message.isShowDetail();

        // make sure we have a non-null value for summary and
        // detail.
        String summary = null != (summary = curMessage.getSummary()) ? summary : "";
        // Default to summary if we have no detail
        String detail = null != (detail = curMessage.getDetail()) ? detail : summary;

        if (curMessage.getSeverity() != null) {
            String severityPrefix = curMessage.getSeverity().name().toLowerCase();
            severityStyle = (String) component.getAttributes().get(severityPrefix + "Style");
            severityStyleClass = (String) component.getAttributes().get(severityPrefix + "Class");
        }

        List<String> setAttributes = RenderKitUtils.getAttributesThatAreSet(component);
        String style = (String) RenderKitUtils.getAttributeIfSet(component, setAttributes, "style");
        String styleClass = (String) RenderKitUtils.getAttributeIfSet(component, "styleClass");
        String dir = (String) RenderKitUtils.getAttributeIfSet(component, setAttributes, "dir");
        String lang = (String) RenderKitUtils.getAttributeIfSet(component, setAttributes, "lang");
        String title = (String) RenderKitUtils.getAttributeIfSet(component, setAttributes, "title");

        // if we have style and severityStyle
        if (style != null && severityStyle != null) {
            // severityStyle wins
            style = severityStyle;
        }
        // if we have no style, but do have severityStyle
        else if (style == null && severityStyle != null) {
            // severityStyle wins
            style = severityStyle;
        }

        // if we have styleClass and severityStyleClass
        if (styleClass != null && severityStyleClass != null) {
            // severityStyleClass wins
            styleClass = severityStyleClass;
        }
        // if we have no styleClass, but do have severityStyleClass
        else if (styleClass == null && severityStyleClass != null) {
            // severityStyleClass wins
            styleClass = severityStyleClass;
        }

        // Done intializing local variables. Move on to rendering.

        boolean wroteSpan = false;
        if (styleClass != null || style != null || dir != null || lang != null || title != null || mustRender) {
            writer.startElement("span", component);
            writeIdAttributeIfNecessary(context, writer, component);

            wroteSpan = true;
            if (style != null) {
                writer.writeAttribute("style", style, "style");
            }
            if (styleClass != null) {
                writer.writeAttribute("class", styleClass, "styleClass");
            }
            if (dir != null) {
                writer.writeAttribute("dir", dir, "dir");
            }
            if (lang != null) {
                writer.writeAttribute(RenderKitUtils.prefixAttribute("lang", writer), lang, "lang");
            }
            if (title != null) {
                writer.writeAttribute("title", title, "title");
            }

        }

        boolean isTooltip = htmlMessage != null ? htmlMessage.isTooltip()
                : RenderKitUtils.attributeIsTrue(component, "tooltip", false);

        boolean wroteTooltip = false;
        if ((showSummary || showDetail) && isTooltip) {

            if (!wroteSpan) {
                writer.startElement("span", component);
            }
            if (title == null || title.length() == 0) {
                writer.writeAttribute("title", detail, "title");
            }
            writer.flush();
            writer.writeText("\t", component, null);
            wroteTooltip = true;
        } else if (wroteSpan) {
            writer.flush();
        }

        if (showSummary) {
            writer.writeText("\t", component, null);
            writer.writeText(summary, component, null);
            writer.writeText(" ", component, null);
        }
        if (showDetail) {
            writer.writeText(detail, component, null);
        }

        if (wroteSpan || wroteTooltip) {
            writer.endElement("span");
        }

    }

} // end of class MessageRenderer
