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

package com.sun.faces.test.javaee6web.facelets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.component.html.HtmlDataTable;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

@Named
@ViewScoped
public class DuplicateController implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> itemsList = new ArrayList<>();
    private HtmlDataTable dataTable;

    public void createNewRow() {
        incRows();
        itemsList.add(0, "new:" + (new Object()).hashCode());
    }

    private void incRows() {
        int rows = this.dataTable.getRows();
        this.dataTable.setRows(rows + 1);
    }

    public HtmlDataTable getDataTable() {
        return dataTable;
    }

    public void setDataTable(HtmlDataTable dataTable) {
        this.dataTable = dataTable;
    }

    public List<String> getItemsList() {
        return itemsList;
    }

    public void setItemsList(List<String> itemsList) {
        this.itemsList = itemsList;
    }
}
