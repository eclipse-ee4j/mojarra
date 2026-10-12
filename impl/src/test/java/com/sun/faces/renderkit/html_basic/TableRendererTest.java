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

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.faces.junit.JUnitFacesTestCaseBase;
import com.sun.faces.mock.MockRenderKit;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.faces.FactoryFinder;
import jakarta.faces.component.UIOutput;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.html.HtmlColumn;
import jakarta.faces.component.html.HtmlDataTable;
import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKitFactory;

/**
 * The cells {@link TableRenderer} renders for the <code>rowHeader</code> and <code>styleClass</code> attributes of the
 * columns.
 */
class TableRendererTest extends JUnitFacesTestCaseBase {

    private static final Pattern CELL = Pattern.compile("<(td|th)([^>]*)>");

    /** The current row, prefixed. */
    static class RowExpression extends ValueExpression {
        private static final long serialVersionUID = 1L;
        private final String prefix;

        RowExpression(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Object getValue(ELContext context) {
            return prefix + FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("row");
        }

        @Override
        public void setValue(ELContext context, Object value) {
        }

        @Override
        public boolean isReadOnly(ELContext context) {
            return true;
        }

        @Override
        public Class<?> getType(ELContext context) {
            return String.class;
        }

        @Override
        public Class<?> getExpectedType() {
            return String.class;
        }

        @Override
        public String getExpressionString() {
            return "#{'" + prefix + "'.concat(row)}";
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

    /**
     * Changes the <code>styleClass</code> and <code>rowHeader</code> of its column when it is rendered, and renders no
     * output.
     */
    static class ColumnChanger extends UIOutput {
        @Override
        public void encodeBegin(FacesContext context) throws IOException {
            HtmlColumn column = (HtmlColumn) getParent();
            column.setStyleClass("changed");
            column.setRowHeader(false);
        }
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        RenderKitFactory renderKitFactory = (RenderKitFactory) FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        renderKitFactory.addRenderKit(RenderKitFactory.HTML_BASIC_RENDER_KIT, new MockRenderKit());
    }

    private HtmlDataTable table(HtmlColumn... columns) {
        UIViewRoot root = new UIViewRoot();
        root.setRenderKitId(RenderKitFactory.HTML_BASIC_RENDER_KIT);
        facesContext.setViewRoot(root);
        HtmlDataTable table = new HtmlDataTable();
        root.getChildren().add(table);
        table.setId("table");
        table.setVar("row");
        table.setValue(List.of("a", "b", "c"));
        for (HtmlColumn column : columns) {
            table.getChildren().add(column);
        }
        return table;
    }

    private String render(HtmlDataTable table) throws IOException {
        StringWriter output = new StringWriter();
        facesContext.setResponseWriter(new HtmlResponseWriter(output, "text/html", "UTF-8"));
        TableRenderer renderer = new TableRenderer();
        renderer.encodeBegin(facesContext, table);
        renderer.encodeChildren(facesContext, table);
        renderer.encodeEnd(facesContext, table);
        return output.toString();
    }

    private static String cells(String html) {
        StringBuilder cells = new StringBuilder();
        Matcher matcher = CELL.matcher(html);
        while (matcher.find()) {
            cells.append('<').append(matcher.group(1)).append(matcher.group(2)).append('>');
        }
        return cells.toString();
    }

    @Test
    void constantAndRowDependentColumnAttributes() throws IOException {
        HtmlColumn header = new HtmlColumn();
        header.setRowHeader(true);
        header.setStyleClass("header");
        HtmlColumn rowDependent = new HtmlColumn();
        rowDependent.setValueExpression("styleClass", new RowExpression("cell-"));

        String cells = cells(render(table(header, rowDependent)));

        assertEquals("<th scope=\"row\" class=\"header\"><td class=\"cell-a\">"
                + "<th scope=\"row\" class=\"header\"><td class=\"cell-b\">"
                + "<th scope=\"row\" class=\"header\"><td class=\"cell-c\">", cells);
    }

    @Test
    void constantColumnAttributeChangedDuringTheRowsAppliesFromTheNextRender() throws IOException {
        HtmlColumn column = new HtmlColumn();
        column.setRowHeader(true);
        column.setStyleClass("header");
        column.getChildren().add(new ColumnChanger());
        HtmlDataTable table = table(column);

        String first = cells(render(table));
        String second = cells(render(table));

        assertEquals("<th scope=\"row\" class=\"header\"><th scope=\"row\" class=\"header\"><th scope=\"row\" class=\"header\">", first);
        assertEquals("<td class=\"changed\"><td class=\"changed\"><td class=\"changed\">", second);
    }
}
