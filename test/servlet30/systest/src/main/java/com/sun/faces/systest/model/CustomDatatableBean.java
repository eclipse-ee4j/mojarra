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

package com.sun.faces.systest.model;

import java.util.List;
import java.util.ArrayList;

import javax.faces.component.html.HtmlDataTable;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlColumn;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.application.Application;
import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import javax.el.MethodExpression;
import javax.el.ELContext;


public class CustomDatatableBean {

    private HtmlDataTable table;

        public List<String> getList() {
            List<String> result = new ArrayList<String>();
            result.add("abc");
            result.add("def");
            result.add("ghi");
            return result;
        }

        public UIComponent getTable() {
            if (table == null) {
                FacesContext context = FacesContext.getCurrentInstance();
                Application app = context.getApplication();
                ExpressionFactory factory = app.getExpressionFactory();
                table = new HtmlDataTable();
                table.setVar("p");
                ELContext elContext = context.getELContext();
                table.setValueExpression("value", factory.createValueExpression(
                        elContext, "#{customDataTable.list}", Object.class));
                HtmlColumn c1 = new HtmlColumn();
                HtmlCommandLink l = new HtmlCommandLink();
                MethodExpression expr = factory.createMethodExpression(elContext,
                        "ok", String.class, new Class<?>[] {});
                l.setActionExpression(expr);
                ValueExpression source = factory.createValueExpression(elContext,
                        "#{p}", String.class);
                l.setValueExpression("value", source);
                c1.getChildren().add(l);
                table.getChildren().add(c1);
            }
            return table;
        }

        public void setTable(UIComponent table) {
            this.table = (HtmlDataTable) table;
        }
    
}
