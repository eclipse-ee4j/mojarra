/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.servlet30.facelets;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.el.ValueExpression;
import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UINamingContainer;
import javax.faces.component.UIViewRoot;
import javax.faces.component.html.HtmlColumn;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.inject.Named;


@Named
@SessionScoped
public class DataTableDynamicBean implements Serializable {

    private static final long serialVersionUID = 1L;

    public String getTitle() {
        return "Can not add table dynamically";
    }

    public String getAddValue() {
        return "Add Datatable";
    }

    public void addTable() {
        FacesContext fc = FacesContext.getCurrentInstance();
        UIViewRoot root = fc.getViewRoot();
        UIComponent container = root.findComponent("form:dtcontainer");
        container.getChildren().add(creatTable(fc));
    }

    public HtmlDataTable creatTable(FacesContext fc) {
        HtmlDataTable table = new HtmlDataTable();
        ValueExpression ve = fc.getApplication().getExpressionFactory().createValueExpression(fc.getELContext(),
                "#{dataTableDynamicBean.testStrings}", Object.class);
        table.setId("table");
        table.setValueExpression("value", ve);
        table.setVar("str");

        UINamingContainer nc = new UINamingContainer();
        nc.setId("nc");

        HtmlPanelGroup ncPanel = new HtmlPanelGroup();
        ncPanel.setId("ncpanel");

        HtmlOutputText text = new HtmlOutputText();
        text.setId("strv");
        ValueExpression textve = fc.getApplication().getExpressionFactory().createValueExpression(fc.getELContext(), "#{str}",
                Object.class);
        text.setValueExpression("value", textve);
        ncPanel.getChildren().add(text);

        nc.getChildren().add(ncPanel);

        HtmlPanelGroup panel = new HtmlPanelGroup();
        panel.getChildren().add(nc);

        HtmlColumn column = new HtmlColumn();
        column.getChildren().add(panel);
        table.getChildren().add(column);
        return table;
    }

    public List<String> getTestStrings() {
        String vs[] = { "one", "two", "three", "four" };
        return Arrays.asList(vs);
    }
}
