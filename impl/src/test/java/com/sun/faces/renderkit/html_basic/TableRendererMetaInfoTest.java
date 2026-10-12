/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.faces.renderkit.html_basic.BaseTableRenderer.TableMetaInfo;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.faces.component.UIColumn;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.html.HtmlColumn;
import jakarta.faces.component.html.HtmlDataTable;
import jakarta.faces.context.FacesContext;

/**
 * The per-render table info of {@link BaseTableRenderer}: where it is kept, and when the column attributes read for
 * every cell are read once per render.
 */
class TableRendererMetaInfoTest {

    /** A value expression whose value the test changes between rows. */
    static class Variable extends ValueExpression {
        private static final long serialVersionUID = 1L;
        Object value;

        Variable(Object value) {
            this.value = value;
        }

        @Override
        public Object getValue(ELContext context) {
            return value;
        }

        @Override
        public void setValue(ELContext context, Object value) {
            this.value = value;
        }

        @Override
        public boolean isReadOnly(ELContext context) {
            return true;
        }

        @Override
        public Class<?> getType(ELContext context) {
            return Object.class;
        }

        @Override
        public Class<?> getExpectedType() {
            return Object.class;
        }

        @Override
        public String getExpressionString() {
            return "#{variable}";
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public boolean isLiteralText() {
            return false;
        }
    }

    static class CustomKeyRenderer extends TableRenderer {
        @Override
        protected String createKey(UIComponent table) {
            return "custom:" + table.getId();
        }
    }

    private FacesContext context;
    private final Map<Object, Object> attributes = new HashMap<>();

    @BeforeEach
    void setUp() throws Exception {
        context = mock(FacesContext.class);
        when(context.getAttributes()).thenReturn(attributes);
        setCurrentInstance(context);
    }

    @AfterEach
    void tearDown() throws Exception {
        setCurrentInstance(null);
    }

    private static void setCurrentInstance(FacesContext context) throws Exception {
        Method setCurrentInstance = FacesContext.class.getDeclaredMethod("setCurrentInstance", FacesContext.class);
        setCurrentInstance.setAccessible(true);
        setCurrentInstance.invoke(null, context);
    }

    private static HtmlDataTable table(UIColumn... columns) {
        HtmlDataTable table = new HtmlDataTable();
        table.setId("table");
        for (UIColumn column : columns) {
            table.getChildren().add(column);
        }
        return table;
    }

    @Test
    void metaInfoIsKeptPerTableUntilCleared() {
        TableRenderer renderer = new TableRenderer();
        HtmlDataTable table = table(new HtmlColumn());
        HtmlDataTable other = table(new HtmlColumn());

        TableMetaInfo info = renderer.getMetaInfo(context, table);
        assertSame(info, renderer.getMetaInfo(context, table));
        assertNotSame(info, renderer.getMetaInfo(context, other));
        assertNull(attributes.get(renderer.createKey(table)));

        renderer.clearMetaInfo(context, table);
        assertNotSame(info, renderer.getMetaInfo(context, table));
    }

    @Test
    void metaInfoIsKeptUnderTheKeyOfARendererWhichOverridesCreateKey() {
        CustomKeyRenderer renderer = new CustomKeyRenderer();
        HtmlDataTable table = table(new HtmlColumn());

        TableMetaInfo info = renderer.getMetaInfo(context, table);
        assertSame(info, attributes.get("custom:table"));
        assertSame(info, renderer.getMetaInfo(context, table));

        renderer.clearMetaInfo(context, table);
        assertFalse(attributes.containsKey("custom:table"));
    }

    @Test
    void constantColumnAttributesAreReadOncePerRender() {
        HtmlColumn column = new HtmlColumn();
        column.setStyleClass("first");
        HtmlDataTable table = table(column);
        TableMetaInfo info = new TableMetaInfo(table);

        assertEquals("first", info.getColumnStyleClass(table, 0, column));
        assertFalse(info.isRowHeader(table, 0, column));

        column.setStyleClass("second");
        column.setRowHeader(true);
        assertEquals("first", info.getColumnStyleClass(table, 0, column));
        assertFalse(info.isRowHeader(table, 0, column));

        TableMetaInfo nextRender = new TableMetaInfo(table);
        assertEquals("second", nextRender.getColumnStyleClass(table, 0, column));
        assertTrue(nextRender.isRowHeader(table, 0, column));
    }

    @Test
    void unsetColumnStyleClassStaysUnset() {
        HtmlColumn column = new HtmlColumn();
        HtmlDataTable table = table(column);
        TableMetaInfo info = new TableMetaInfo(table);

        assertNull(info.getColumnStyleClass(table, 0, column));
        assertNull(info.getColumnStyleClass(table, 0, column));
    }

    @Test
    void valueExpressionColumnAttributesAreReadPerRow() {
        HtmlColumn column = new HtmlColumn();
        Variable styleClass = new Variable("odd");
        Variable rowHeader = new Variable(false);
        column.setValueExpression("styleClass", styleClass);
        column.setValueExpression("rowHeader", rowHeader);
        HtmlDataTable table = table(column);
        TableMetaInfo info = new TableMetaInfo(table);

        assertEquals("odd", info.getColumnStyleClass(table, 0, column));
        assertFalse(info.isRowHeader(table, 0, column));

        styleClass.value = "even";
        rowHeader.value = true;
        assertEquals("even", info.getColumnStyleClass(table, 0, column));
        assertTrue(info.isRowHeader(table, 0, column));
    }

    @Test
    void columnAttributesAreReadPerRowWhenRowStateIsPreserved() {
        HtmlColumn column = new HtmlColumn();
        column.setStyleClass("first");
        HtmlDataTable table = table(column);
        table.setRowStatePreserved(true);
        TableMetaInfo info = new TableMetaInfo(table);

        assertEquals("first", info.getColumnStyleClass(table, 0, column));
        assertFalse(info.isRowHeader(table, 0, column));

        column.setStyleClass("second");
        column.setRowHeader(true);
        assertEquals("second", info.getColumnStyleClass(table, 0, column));
        assertTrue(info.isRowHeader(table, 0, column));
    }

    @Test
    void plainUIColumnReadsRowHeaderAsAttribute() {
        UIColumn column = new UIColumn();
        column.getAttributes().put("rowHeader", "true");
        HtmlDataTable table = table(column);
        TableMetaInfo info = new TableMetaInfo(table);

        assertTrue(info.isRowHeader(table, 0, column));
    }
}
