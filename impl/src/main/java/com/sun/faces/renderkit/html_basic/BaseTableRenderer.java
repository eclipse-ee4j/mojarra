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
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import com.sun.faces.renderkit.Attributes;
import com.sun.faces.renderkit.RenderKitUtils;
import com.sun.faces.util.Util;

import jakarta.faces.component.UIColumn;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIData;
import jakarta.faces.component.html.HtmlColumn;
import jakarta.faces.component.html.HtmlDataTable;
import jakarta.faces.component.html.HtmlPanelGrid;
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
    protected void renderTableStart(FacesContext context, UIComponent table, ResponseWriter writer, Attributes attributes) throws IOException {

        writer.startElement("table", table);
        writeIdAttributeIfNecessary(context, writer, table);
        writeStyleClassAttributeIfNecessary(writer, table);
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

        RenderKitUtils.flushPendingBehaviorEventListeners(context, table, null);
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
            String captionClass = table instanceof HtmlDataTable t ? t.getCaptionClass()
                    : table instanceof HtmlPanelGrid g ? g.getCaptionClass() : (String) table.getAttributes().get("captionClass");
            String captionStyle = table instanceof HtmlDataTable t ? t.getCaptionStyle()
                    : table instanceof HtmlPanelGrid g ? g.getCaptionStyle() : (String) table.getAttributes().get("captionStyle");
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
        final String rowClass = table instanceof HtmlDataTable t ? t.getRowClass()
                : table instanceof HtmlPanelGrid g ? g.getRowClass() : (String) table.getAttributes().get("rowClass");

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

        Map<Object, Object> attributes = context.getAttributes();
        if (CUSTOM_KEY.get(getClass())) {
            String key = createKey(table);
            TableRenderer.TableMetaInfo info = (TableRenderer.TableMetaInfo) attributes.get(key);
            if (info == null) {
                info = new TableRenderer.TableMetaInfo(table);
                attributes.put(key, info);
            }
            return info;
        }

        // Looked up once per row: an identity map keyed by the table itself avoids building, hashing and comparing a
        // fresh String key (createKey) on every call.
        Map<UIComponent, TableMetaInfo> byTable = metaInfoByTable(attributes, true);
        TableMetaInfo info = byTable.get(table);
        if (info == null) {
            info = new TableRenderer.TableMetaInfo(table);
            byTable.put(table, info);
        }
        return info;

    }

    private static final String META_INFO_BY_TABLE_KEY = TableMetaInfo.KEY + ".byTable";

    /** Whether a renderer class overrides {@link #createKey}, in which case its keys are honored as before. */
    private static final ClassValue<Boolean> CUSTOM_KEY = new ClassValue<>() {
        @Override
        protected Boolean computeValue(Class<?> type) {
            for (Class<?> c = type; c != null && c != BaseTableRenderer.class; c = c.getSuperclass()) {
                try {
                    c.getDeclaredMethod("createKey", UIComponent.class);
                    return true;
                } catch (NoSuchMethodException e) {
                    // keep looking
                }
            }
            return false;
        }
    };

    @SuppressWarnings("unchecked")
    private static Map<UIComponent, TableMetaInfo> metaInfoByTable(Map<Object, Object> attributes, boolean create) {
        Map<UIComponent, TableMetaInfo> byTable = (Map<UIComponent, TableMetaInfo>) attributes.get(META_INFO_BY_TABLE_KEY);
        if (byTable == null && create) {
            byTable = new IdentityHashMap<>(4);
            attributes.put(META_INFO_BY_TABLE_KEY, byTable);
        }
        return byTable;
    }

    /**
     * Removes the cached TableMetaInfo from the specified component.
     *
     * @param context the <code>FacesContext</code> for the current request
     * @param table the table from which the TableMetaInfo will be removed
     */
    protected void clearMetaInfo(FacesContext context, UIComponent table) {

        Map<Object, Object> attributes = context.getAttributes();
        if (CUSTOM_KEY.get(getClass())) {
            attributes.remove(createKey(table));
            return;
        }
        Map<UIComponent, TableMetaInfo> byTable = metaInfoByTable(attributes, false);
        if (byTable != null) {
            byTable.remove(table);
        }

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

        // Per-column attributes read for every cell. A column attribute that is not a value expression cannot change
        // from row to row (unless the table preserves full row state), so it is read once per render.
        private static final byte UNKNOWN = 0;
        private static final byte FALSE = 1;
        private static final byte TRUE = 2;
        private static final byte DYNAMIC = 3;
        private static final Object UNSET = new Object();

        private byte[] rowHeaders;
        private Object[] styleClasses;

        /**
         * Whether the cells of the column at <code>index</code> are row headers for the current row.
         */
        public boolean isRowHeader(UIComponent table, int index, UIColumn column) {
            if (rowHeaders == null) {
                rowHeaders = new byte[columnCount];
            }
            byte state = rowHeaders[index];
            if (state == UNKNOWN) {
                state = isConstant(table, column, "rowHeader") ? readRowHeader(column) ? TRUE : FALSE : DYNAMIC;
                rowHeaders[index] = state;
            }
            return state == DYNAMIC ? readRowHeader(column) : state == TRUE;
        }

        /**
         * The <code>styleClass</code> of the column at <code>index</code> for the current row, if set.
         */
        public String getColumnStyleClass(UIComponent table, int index, UIColumn column) {
            if (styleClasses == null) {
                styleClasses = new Object[columnCount];
            }
            Object styleClass = styleClasses[index];
            if (styleClass == null) {
                if (!isConstant(table, column, "styleClass")) {
                    return (String) RenderKitUtils.getAttributeIfSet(column, "styleClass");
                }
                styleClass = RenderKitUtils.getAttributeIfSet(column, "styleClass");
                styleClasses[index] = styleClass == null ? UNSET : styleClass;
            }
            return styleClass == UNSET ? null : (String) styleClass;
        }

        private static boolean isConstant(UIComponent table, UIColumn column, String attribute) {
            return column.getValueExpression(attribute) == null && !(table instanceof UIData && ((UIData) table).isRowStatePreserved());
        }

        private static boolean readRowHeader(UIColumn column) {
            return column instanceof HtmlColumn htmlColumn ? htmlColumn.isRowHeader() : RenderKitUtils.attributeIsTrue(column, "rowHeader", false);
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

            String values = table instanceof HtmlDataTable t ? t.getColumnClasses()
                    : table instanceof HtmlPanelGrid g ? g.getColumnClasses() : (String) table.getAttributes().get("columnClasses");
            if (values == null) {
                return EMPTY_STRING_ARRAY;
            }
            return Util.split(values.trim(), ',');

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
                if (table instanceof HtmlPanelGrid g) {
                    count = g.getColumns();
                } else {
                    Object value = table.getAttributes().get("columns");
                    count = value instanceof Integer integer ? integer : 2;
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

            String values = table instanceof HtmlDataTable t ? t.getRowClasses()
                    : table instanceof HtmlPanelGrid g ? g.getRowClasses() : (String) table.getAttributes().get("rowClasses");
            if (values == null) {
                return EMPTY_STRING_ARRAY;
            }
            return Util.split(values.trim(), ',');

        }

    } // END UIDataMetaInfo
}
