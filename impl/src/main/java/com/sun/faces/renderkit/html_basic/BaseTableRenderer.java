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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sun.faces.renderkit.Attribute;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.util.Util;

import jakarta.faces.component.UIColumn;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIData;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.ResponseWriter;

/**
 * Base class for concrete Grid and Table renderers.
 */
public abstract class BaseTableRenderer extends HtmlBasicRenderer {

    // ------------------------------------------------------- Protected Methods

    /**
     * Called to render the opening/closing <code>thead</code> elements and any content nested between.
     *
     * @param context the <code>FacesContext</code> for the current request
     * @param table the table that's being rendered
     * @param writer the current writer
     * @throws IOException if content cannot be written
     */
    protected abstract void renderHeader(FacesContext context, UIComponent table, ResponseWriter writer) throws IOException;

    /**
     * Called to render the opening/closing <code>tfoot</code> elements and any content nested between.
     *
     * @param context the <code>FacesContext</code> for the current request
     * @param table the table that's being rendered
     * @param writer the current writer
     * @throws IOException if content cannot be written
     */
    protected abstract void renderFooter(FacesContext context, UIComponent table, ResponseWriter writer) throws IOException;

    /**
     * Call to render the content that should be included between opening and closing <code>tr</code> elements.
     *
     * @param context the <code>FacesContext</code> for the current request
     * @param table the table that's being rendered
     * @param row the current row (if any - an implmenetation may not need this)
     * @param writer the current writer
     * @throws IOException if content cannot be written
     */
    protected abstract void renderRow(FacesContext context, UIComponent table, UIComponent row, ResponseWriter writer) throws IOException;

    /**
     * Renders the start of a table and applies the value of <code>styleClass</code> if available and renders any pass
     * through attributes that may be specified.
     *
     * @param context the <code>FacesContext</code> for the current request
     * @param table the table that's being rendered
     * @param writer the current writer
     * @param attributes pass-through attributes that the component supports
     * @throws IOException if content cannot be written
     */
    protected void renderTableStart(FacesContext context, UIComponent table, ResponseWriter writer, Attribute[] attributes) throws IOException {

        writer.startElement("table", table);
        writeIdAttributeIfNecessary(context, writer, table);
        String styleClass = (String) table.getAttributes().get("styleClass");
        if (styleClass != null) {
            writer.writeAttribute("class", styleClass, "styleClass");
        }
        RenderKitUtils.renderPassThruAttributes(context, writer, table, attributes);
        writer.writeText("\n", table, null);

    }

    /**
     * Renders the closing <code>table</code> element.
     *
     * @param context the <code>FacesContext</code> for the current request
     * @param table the table that's being rendered
     * @param writer the current writer
     * @throws IOException if content cannot be written
     */
    @SuppressWarnings({ "UnusedDeclaration" })
    protected void renderTableEnd(FacesContext context, UIComponent table, ResponseWriter writer) throws IOException {

        writer.endElement("table");
        writer.writeText("\n", table, null);

    }

    /**
     * Renders the caption of the table applying the values of <code>captionClass</code> as the class and
     * <code>captionStyle</code> as the style if either are present.
     *
     * @param context the <code>FacesContext</code> for the current request
     * @param table the table that's being rendered
     * @param writer the current writer
     * @throws IOException if content cannot be written
     */
    protected void renderCaption(FacesContext context, UIComponent table, ResponseWriter writer) throws IOException {

        UIComponent caption = getFacet(table, "caption");
        if (caption != null) {
            String captionClass = (String) table.getAttributes().get("captionClass");
            String captionStyle = (String) table.getAttributes().get("captionStyle");
            writer.startElement("caption", table);
            if (captionClass != null) {
                writer.writeAttribute("class", captionClass, "captionClass");
            }
            if (captionStyle != null) {
                writer.writeAttribute("style", captionStyle, "captionStyle");
            }
            encodeRecursive(context, caption);
            writer.endElement("caption");
        }

    }

    /**
     * Renders the starting <code>tbody</code> element.
     *
     * @param context the <code>FacesContext</code> for the current request
     * @param table the table that's being rendered
     * @param writer the current writer
     * @throws IOException if content cannot be written
     */
    @SuppressWarnings({ "UnusedDeclaration" })
    protected void renderTableBodyStart(FacesContext context, UIComponent table, ResponseWriter writer) throws IOException {

        writer.startElement("tbody", table);
        writer.writeText("\n", table, null);

    }

    /**
     * Renders the closing <code>tbody</code> element.
     *
     * @param context the <code>FacesContext</code> for the current request
     * @param table the table that's being rendered
     * @param writer the current writer
     * @throws IOException if content cannot be written
     */
    @SuppressWarnings({ "UnusedDeclaration" })
    protected void renderTableBodyEnd(FacesContext context, UIComponent table, ResponseWriter writer) throws IOException {

        writer.endElement("tbody");
        writer.writeText("\n", table, null);

    }

    /**
     * Renders the starting <code>tr</code> element applying any values from the <code>rowClasses</code> attribute.
     *
     * @param context the <code>FacesContext</code> for the current request
     * @param table the table that's being rendered
     * @param writer the current writer
     * @throws IOException if content cannot be written
     */
    protected void renderRowStart(FacesContext context, UIComponent table, ResponseWriter writer) throws IOException {

        TableMetaInfo info = getMetaInfo(context, table);
        writer.startElement("tr", table);

        final String tableRowClass = info.rowClasses.length > 0 ? info.getCurrentRowClass() : null;
        final String rowClass = (String) table.getAttributes().get("rowClass");

        if (tableRowClass != null) {
            if (rowClass != null) {
                throw new IOException("Cannot define both rowClasses on a table and rowClass");
            }
            writer.writeAttribute("class", tableRowClass, "rowClasses");
        }

        if (rowClass != null) {
            if (tableRowClass != null) {
                throw new IOException("Cannot define both rowClasses on a table and rowClass");
            }
            writer.writeAttribute("class", rowClass, "rowClass");
        }

        writer.writeText("\n", table, null);

    }

    /**
     * Renders the closing <code>rt</code> element.
     *
     * @param context the <code>FacesContext</code> for the current request
     * @param table the table that's being rendered
     * @param writer the current writer
     * @throws IOException if content cannot be written
     */
    @SuppressWarnings({ "UnusedDeclaration" })
    protected void renderRowEnd(FacesContext context, UIComponent table, ResponseWriter writer) throws IOException {

        writer.endElement("tr");
        writer.writeText("\n", table, null);

    }

    /**
     * Returns a <code>TableMetaInfo</code> object containing details such as row and column classes, columns, and a
     * mechanism for scrolling through the row/column classes.
     *
     * @param context the <code>FacesContext</code> for the current request
     * @param table the table that's being rendered
     * @return the <code>TableMetaInfo</code> for provided table
     */
    protected TableRenderer.TableMetaInfo getMetaInfo(FacesContext context, UIComponent table) {

        String key = createKey(table);
        Map<Object, Object> attributes = context.getAttributes();
        TableRenderer.TableMetaInfo info = (TableRenderer.TableMetaInfo) attributes.get(key);
        if (info == null) {
            info = new TableRenderer.TableMetaInfo(table);
            attributes.put(key, info);
        }
        return info;

    }

    /**
     * Removes the cached TableMetaInfo from the specified component.
     *
     * @param context the <code>FacesContext</code> for the current request
     * @param table the table from which the TableMetaInfo will be removed
     */
    protected void clearMetaInfo(FacesContext context, UIComponent table) {

        context.getAttributes().remove(createKey(table));

    }

    /**
     * Creates a unique key based on the provided <code>UIComponent</code> with which the TableMetaInfo can be looked up.
     *
     * @param table the table that's being rendered
     * @return a unique key to store the metadata in the request and still have it associated with a specific component.
     */
    protected String createKey(UIComponent table) {

        return TableMetaInfo.KEY + '_' + table.hashCode();

    }

    // ----------------------------------------------------------- Inner Classes

    protected static class TableMetaInfo {

        private static final String[] EMPTY_STRING_ARRAY = new String[0];
        public static final String KEY = TableMetaInfo.class.getName();

        public final String[] rowClasses;
        public final String[] columnClasses;
        public final List<UIColumn> columns;
        public final boolean hasHeaderFacets;
        public final boolean hasFooterFacets;
        public final int columnCount;
        public int columnStyleCounter;
        public int rowStyleCounter;

        // -------------------------------------------------------- Constructors

        public TableMetaInfo(UIComponent table) {
            rowClasses = getRowClasses(table);
            columnClasses = getColumnClasses(table);
            columns = getColumns(table);
            columnCount = columns.size();
            hasHeaderFacets = hasFacet("header", columns);
            hasFooterFacets = hasFacet("footer", columns);
        }

        // ------------------------------------------------------ Public Methods

        /**
         * Reset the counter used to apply column styles.
         */
        public void newRow() {

            columnStyleCounter = 0;

        }

        /**
         * Obtain the column class based on the current counter. Calling this method automatically moves the pointer to the next
         * style. If the counter is larger than the number of total classes, the counter will be reset.
         *
         * @return the current style
         */
        public String getCurrentColumnClass() {

            String style = null;
            if (columnStyleCounter < columnClasses.length && columnStyleCounter <= columnCount) {
                style = columnClasses[columnStyleCounter++];
            }
            return style != null && style.length() > 0 ? style : null;

        }

        /**
         * Obtain the row class based on the current counter. Calling this method automatically moves the pointer to the next
         * style. If the counter is larger than the number of total classes, the counter will be reset.
         *
         * @return the current style
         */
        public String getCurrentRowClass() {
            String style = rowClasses[rowStyleCounter++];
            if (rowStyleCounter >= rowClasses.length) {
                rowStyleCounter = 0;
            }
            return style;
        }

        // ----------------------------------------------------- Private Methods

        /**
         * <p>
         * Return an array of stylesheet classes to be applied to each column in the table in the order specified. Every column
         * may or may not have a stylesheet.
         * </p>
         *
         * @param table {@link jakarta.faces.component.UIComponent} component being rendered
         *
         * @return an array of column classes
         */
        private static String[] getColumnClasses(UIComponent table) {

            String values = (String) table.getAttributes().get("columnClasses");
            if (values == null) {
                return EMPTY_STRING_ARRAY;
            }
            Map<String, Object> appMap = FacesContext.getCurrentInstance().getExternalContext().getApplicationMap();
            return Util.split(appMap, values.trim(), ",");

        }

        /**
         * <p>
         * Return an Iterator over the <code>UIColumn</code> children of the specified <code>UIData</code> that have a
         * <code>rendered</code> property of <code>true</code>.
         * </p>
         *
         * @param table the table from which to extract children
         *
         * @return the List of all UIColumn children
         */
        private static List<UIColumn> getColumns(UIComponent table) {

            if (table instanceof UIData) {
                int childCount = table.getChildCount();
                if (childCount > 0) {
                    List<UIColumn> results = new ArrayList<>(childCount);
                    for (UIComponent kid : table.getChildren()) {
                        if (kid instanceof UIColumn && kid.isRendered()) {
                            results.add((UIColumn) kid);
                        }
                    }
                    return results;
                } else {
                    return Collections.emptyList();
                }
            } else {
                int count;
                Object value = table.getAttributes().get("columns");
                if (value != null && value instanceof Integer) {
                    count = (Integer) value;
                } else {
                    count = 2;
                }
                if (count < 1) {
                    count = 1;
                }
                List<UIColumn> result = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    result.add(new UIColumn());
                }
                return result;
            }

        }

        /**
         * <p>
         * Return the number of child <code>UIColumn</code> components nested in the specified <code>UIData</code> that have a
         * facet with the specified name.
         * </p>
         *
         * @param name Name of the facet being analyzed
         * @param columns the columns to search
         *
         * @return the number of columns associated with the specified Facet name
         */
        private static boolean hasFacet(String name, List<UIColumn> columns) {

            if (!columns.isEmpty()) {
                for (UIColumn column : columns) {
                    if (column.getFacetCount() > 0) {
                        if (column.getFacets().containsKey(name)) {
                            return true;
                        }
                    }
                }
            }
            return false;

        }

        /**
         * <p>
         * Return an array of stylesheet classes to be applied to each row in the table, in the order specified. Every row may
         * or may not have a stylesheet.
         * </p>
         *
         * @param table {@link jakarta.faces.component.UIComponent} component being rendered
         *
         * @return an array of row classes
         */
        private static String[] getRowClasses(UIComponent table) {

            String values = (String) table.getAttributes().get("rowClasses");
            if (values == null) {
                return EMPTY_STRING_ARRAY;
            }
            Map<String, Object> appMap = FacesContext.getCurrentInstance().getExternalContext().getApplicationMap();
            return Util.split(appMap, values.trim(), ",");

        }

    } // END UIDataMetaInfo
}
