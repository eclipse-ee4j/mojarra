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

import javax.faces.component.UIData;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import java.util.List;
import java.util.ArrayList;

public class InputBean extends Object {

    /**
     * so we know when to reset our counters
     */

    protected int max;

    protected BeanList list;

    public InputBean(BeanList list, int max, String stringProperty) {
        this.list = list;
        this.max = max;
        this.stringProperty = stringProperty;
    }

    protected String stringProperty;

    public String getStringProperty() {
        String result = null;
        if (null != stringProperty) {
            result = stringProperty;
            return result;
        }

        FacesContext context = FacesContext.getCurrentInstance();
        int
                index = getFlatIndex();
        List inputValues = null;

        if (null == (inputValues = list.getInputValues())) {
            result = null;
        } else {
            result = (String) inputValues.get(index);
        }

        return result;
    }

    public void setStringProperty(String newStringProperty) {
        FacesContext context = FacesContext.getCurrentInstance();
        int
                size = getFlatSize(),
                index = getFlatIndex();
        List inputValues = null;

        if (null == (inputValues = list.getInputValues())) {
            list.setInputValues(inputValues = new ArrayList(size));
            for (int i = 0; i < size; i++) {
                inputValues.add(new Object());
            }
        }

        inputValues.set(index, newStringProperty);
        this.stringProperty = null;
    }

    private int getFlatIndex() {
        FacesContext context = FacesContext.getCurrentInstance();
        UIViewRoot root = context.getViewRoot();

        UIData
                outerData = (UIData) root.findComponent(list.getOuterDataName()),
                innerData = (UIData) root.findComponent(list.getInnerDataName());

        int
                outerIndex = outerData.getRowIndex(),
                innerIndex = innerData.getRowIndex(),
                innerRowCount = innerData.getRowCount(),
                index = innerRowCount * outerIndex + innerIndex;

        return index;
    }

    private int getFlatSize() {
        FacesContext context = FacesContext.getCurrentInstance();
        UIViewRoot root = context.getViewRoot();

        UIData
                outerData = (UIData) root.findComponent(list.getOuterDataName()),
                innerData = (UIData) root.findComponent(list.getInnerDataName());

        int size = outerData.getRowCount() * innerData.getRowCount();
        return size;
    }


}
