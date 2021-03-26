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

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

/**
 * <B>GridRenderer</B> is a class that renders <code>UIPanel</code> component as a "Grid".
 */

public class GridRenderer extends BaseTableRenderer {

    private static final Attribute[] ATTRIBUTES = AttributeManager.getAttributes(AttributeManager.Key.PANELGRID);

    // ---------------------------------------------------------- Public Methods

    @Override
    public void encodeBegin(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (!shouldEncode(component)) {
            return;
        }

        // Render the beginning of this panel
        ResponseWriter writer = context.getResponseWriter();
        renderTableStart(context, component, writer, ATTRIBUTES);

        // render the caption facet (if present)
        renderCaption(context, component, writer);

        // Render the header facet (if any)
        renderHeader(context, component, writer);

        // Render the footer facet (if any)
        renderFooter(context, component, writer);

    }

    @Override
    public void encodeChildren(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (!shouldEncodeChildren(component)) {
            return;
        }

        // Set up the variables we will need
        ResponseWriter writer = context.getResponseWriter();
        TableMetaInfo info = getMetaInfo(context, component);
        int columnCount = info.columns.size();
        boolean open = false;
        int i = 0;

        // Render our children, starting a new row as needed
        renderTableBodyStart(context, component, writer);
        boolean rowRendered = false;
        for (Iterator<UIComponent> kids = getChildren(component); kids.hasNext();) {

            UIComponent child = kids.next();
            if (!child.isRendered()) {
                continue;
            }
            if (i % columnCount == 0) {
                if (open) {
                    renderRowEnd(context, component, writer);
                }
                renderRowStart(context, component, writer);
                rowRendered = true;
                open = true;
                info.newRow();
            }
            renderRow(context, component, child, writer);
            i++;
        }
        if (open) {
            renderRowEnd(context, component, writer);
        }
        if (!rowRendered) {
            renderEmptyTableRow(writer, component);
        }
        renderTableBodyEnd(context, component, writer);
    }

    @Override
    public void encodeEnd(FacesContext context, UIComponent component) throws IOException {

        rendererParamsNotNull(context, component);

        if (!shouldEncode(component)) {
            return;
        }

        // Render the ending of this panel
        renderTableEnd(context, component, context.getResponseWriter());

        clearMetaInfo(context, component);

    }

    @Override
    public boolean getRendersChildren() {

        return true;

    }

    // ------------------------------------------------------- Protected Methods

    @Override
    protected void renderRow(FacesContext context, UIComponent table, UIComponent child, ResponseWriter writer) throws IOException {

        TableMetaInfo info = getMetaInfo(context, table);
        writer.startElement("td", table);
        String columnClass = info.getCurrentColumnClass();
        if (columnClass != null) {
            writer.writeAttribute("class", columnClass, "columns");
        }
        encodeRecursive(context, child);
        writer.endElement("td");
        writer.writeText("\n", table, null);

    }

    @Override
    protected void renderHeader(FacesContext context, UIComponent table, ResponseWriter writer) throws IOException {

        TableMetaInfo info = getMetaInfo(context, table);
        UIComponent header = getFacet(table, "header");
        String headerClass = (String) table.getAttributes().get("headerClass");
        if (header != null) {
            writer.startElement("thead", table);
            writer.writeText("\n", table, null);
            writer.startElement("tr", header);
            writer.startElement("th", header);
            if (headerClass != null) {
                writer.writeAttribute("class", headerClass, "headerClass");
            }
            writer.writeAttribute("colspan", String.valueOf(info.columns.size()), null);
            writer.writeAttribute("scope", "colgroup", null);
            encodeRecursive(context, header);
            writer.endElement("th");
            writer.endElement("tr");
            writer.writeText("\n", table, null);
            writer.endElement("thead");
            writer.writeText("\n", table, null);
        }

    }

    @Override
    protected void renderFooter(FacesContext context, UIComponent table, ResponseWriter writer) throws IOException {

        TableMetaInfo info = getMetaInfo(context, table);
        UIComponent footer = getFacet(table, "footer");
        String footerClass = (String) table.getAttributes().get("footerClass");
        if (footer != null) {
            writer.startElement("tfoot", table);
            writer.writeText("\n", table, null);
            writer.startElement("tr", footer);
            writer.startElement("td", footer);
            if (footerClass != null) {
                writer.writeAttribute("class", footerClass, "footerClass");
            }
            writer.writeAttribute("colspan", String.valueOf(info.columns.size()), null);
            encodeRecursive(context, footer);
            writer.endElement("td");
            writer.endElement("tr");
            writer.writeText("\n", table, null);
            writer.endElement("tfoot");
            writer.writeText("\n", table, null);
        }

    }

    // ------------------------------------------------------- Private Methods

    private void renderEmptyTableRow(final ResponseWriter writer, final UIComponent component) throws IOException {

        writer.startElement("tr", component);
        writer.startElement("td", component);
        writer.endElement("td");
        writer.endElement("tr");

    }

}
