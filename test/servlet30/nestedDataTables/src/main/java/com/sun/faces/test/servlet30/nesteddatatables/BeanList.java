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

package com.sun.faces.test.servlet30.nesteddatatables;

import java.util.ArrayList;
import java.util.List;

import javax.faces.model.ListDataModel;

public class BeanList extends Object {

    private ListDataModel listDataModel;
    protected String outerDataName;
    protected String innerDataName;
    protected String name = "name";
    protected int size = 10;
    protected List inputValues = null;

    public String getOuterDataName() {
        return outerDataName;
    }

    public void setOuterDataName(String newOuterDataName) {
        outerDataName = newOuterDataName;
    }

    public String getInnerDataName() {
        return innerDataName;
    }

    public void setInnerDataName(String newInnerDataName) {
        innerDataName = newInnerDataName;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        if (null == newName) {
            return;
        }
        name = newName;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int newSize) {
        size = newSize;
    }

    public ListDataModel getListDataModel() {
        if (null == listDataModel) {
            ArrayList beans = new ArrayList(size);
            InputBean curBean = null;

            for (int i = 0; i < size; i++) {
                curBean = new InputBean(this, size, getName() + " " + i);

                beans.add(curBean);
            }
            listDataModel = new ListDataModel(beans);

        }

        return listDataModel;
    }

    public void setListDataModel(ListDataModel newListDataModel) {
        listDataModel = newListDataModel;
    }

    public List getInputValues() {
        return inputValues;
    }

    public void setInputValues(List newInputValues) {
        inputValues = newInputValues;
    }

}
