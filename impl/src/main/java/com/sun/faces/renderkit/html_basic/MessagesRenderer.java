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

// MessagesRenderer.java

package com.sun.faces.renderkit.html_basic;

import java.io.IOException;
import java.util.Iterator;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.AttributeManager;
import com.sun.faces.renderkit.RenderKitUtils;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIMessages;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

/**
 * <p>
 * <B>MessagesRenderer</B> handles rendering for the Messages
 * <p>
 * .
 *
 */

public class MessagesRenderer extends HtmlBasicRenderer {

    private static final Attribute[] ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.MESSAGESMESSAGES);

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

        // If id is user specified, we must render
        boolean mustRender = shouldWriteIdAttribute(component);

        UIMessages messages = (UIMessages) component;
        ResponseWriter writer = context.getResponseWriter();
        assert writer != null;

        String clientId = ((UIMessages) component).getFor();
        // if no clientId was included
        if (clientId == null) {
            // and the author explicitly only wants global messages
            if (messages.isGlobalOnly()) {
                // make it so only global messages get displayed.
                clientId = "";
            }
        }

        // "for" attribute optional for Messages
        Iterator messageIter = getMessageIter(context, clientId, component);

        assert messageIter != null;

        if (!messageIter.hasNext()) {
            if (mustRender) {
                // no message to render, but must render anyway
                // but if we're writing the dev stage messages,
                // only write it if messages exist
                if ("jakarta_faces_developmentstage_messages".equals(component.getId())) {
                    return;
                }
                writer.startElement("div", component);
                writeIdAttributeIfNecessary(context, writer, component);
                writer.endElement("div");
            } // otherwise, return without rendering
            return;
        }

        String layout = (String) component.getAttributes().get("layout");
        boolean showSummary = messages.isShowSummary();
        boolean showDetail = messages.isShowDetail();
        String styleClass = (String) component.getAttributes().get("styleClass");

        boolean wroteTable = false;

        // For layout attribute of "table" render as HTML table.
        // If layout attribute is not present, or layout attribute
        // is "list", render as HTML list.
        if (layout != null && layout.equals("table")) {
            writer.startElement("table", component);
            wroteTable = true;
        } else {
            writer.startElement("ul", component);
        }

        // Render "table" or "ul" level attributes.
        writeIdAttributeIfNecessary(context, writer, component);
        if (null != styleClass) {
            writer.writeAttribute("class", styleClass, "styleClass");
        }
        // style is rendered as a passthru attribute
        RenderKitUtils.renderPassThruAttributes(context, writer, component, ATTRIBUTES);

        while (messageIter.hasNext()) {
            FacesMessage curMessage = (FacesMessage) messageIter.next();
            if (curMessage.isRendered() && !messages.isRedisplay()) {
                continue;
            }
            curMessage.rendered();

            String severityStyle = null;
            String severityStyleClass = null;

            // make sure we have a non-null value for summary and
            // detail.
            String summary = null != (summary = curMessage.getSummary()) ? summary : "";
            // Default to summary if we have no detail
            String detail = null != (detail = curMessage.getDetail()) ? detail : summary;

            if (curMessage.getSeverity() == FacesMessage.SEVERITY_INFO) {
                severityStyle = (String) component.getAttributes().get("infoStyle");
                severityStyleClass = (String) component.getAttributes().get("infoClass");
            } else if (curMessage.getSeverity() == FacesMessage.SEVERITY_WARN) {
                severityStyle = (String) component.getAttributes().get("warnStyle");
                severityStyleClass = (String) component.getAttributes().get("warnClass");
            } else if (curMessage.getSeverity() == FacesMessage.SEVERITY_ERROR) {
                severityStyle = (String) component.getAttributes().get("errorStyle");
                severityStyleClass = (String) component.getAttributes().get("errorClass");
            } else if (curMessage.getSeverity() == FacesMessage.SEVERITY_FATAL) {
                severityStyle = (String) component.getAttributes().get("fatalStyle");
                severityStyleClass = (String) component.getAttributes().get("fatalClass");
            }

            // Done intializing local variables. Move on to rendering.

            if (wroteTable) {
                writer.startElement("tr", component);
            } else {
                writer.startElement("li", component);
            }

            if (severityStyle != null) {
                writer.writeAttribute("style", severityStyle, "style");
            }
            if (severityStyleClass != null) {
                styleClass = severityStyleClass;
                writer.writeAttribute("class", styleClass, "styleClass");
            }

            if (wroteTable) {
                writer.startElement("td", component);
            }

            Object val = component.getAttributes().get("tooltip");
            boolean isTooltip = val != null && Boolean.valueOf(val.toString());

            boolean wroteTooltip = false;
            if (isTooltip) {
                writer.startElement("span", component);
                String title = (String) component.getAttributes().get("title");
                if (title == null || title.length() == 0) {
                    writer.writeAttribute("title", detail, "title");
                }
                writer.flush();
                writer.writeText("\t", component, null);
                wroteTooltip = true;
            }

            if (showSummary) {
                writer.writeText("\t", component, null);
                writer.writeText(summary, component, null);
                writer.writeText(" ", component, null);
            }
            if (showDetail) {
                writer.writeText(detail, component, null);
            }

            if (wroteTooltip) {
                writer.endElement("span");
            }

            // close table row if present
            if (wroteTable) {
                writer.endElement("td");
                writer.endElement("tr");
            } else {
                writer.endElement("li");
            }

        } // messageIter

        // close table if present
        if (wroteTable) {
            writer.endElement("table");
        } else {
            writer.endElement("ul");
        }

    }

} // end of class MessagesRenderer
