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
import java.util.LinkedList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.NumberConverter;
import javax.inject.Named;

@Named
@SessionScoped
public class RepeatDynamicConverterBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private int counter = 0;
    private List<RepeatDynamicConverterItem> items = new LinkedList<RepeatDynamicConverterItem>();

    public List<RepeatDynamicConverterItem> getItems() {
        return items;
    }

    public void add() {
        items.add(new RepeatDynamicConverterItem(++counter));
    }

    public String getString() {
        StringBuilder sb = new StringBuilder();

        for (RepeatDynamicConverterItem item : items) {
            sb.append("[");

            String value = "null";
            if (item.getValue() != null) {
                value = item.getValue().toString();
            }

            sb.append(value);
            sb.append("] ");
        }

        return sb.toString();
    }

    UIComponent findRepeatDynamicConverterItemValue(UIComponent root) {
        if ("itemValue".equals(root.getId())) {
            return root;
        }

        for (UIComponent child : root.getChildren()) {
            UIComponent ret = findRepeatDynamicConverterItemValue(child);
            if (ret != null) {
                return ret;
            }
        }

        return null;
    }

    public void addConverters() {
        FacesContext ctx = FacesContext.getCurrentInstance();

        if (!ctx.isPostback()) {
            UIComponent component = findRepeatDynamicConverterItemValue(ctx.getViewRoot());
            if (component instanceof ValueHolder) {
                ValueHolder parentValueHolder = (ValueHolder) component;
                Converter parentConverter = parentValueHolder.getConverter();
                if (parentConverter == null) {
                    NumberConverter numberConverter = new NumberConverter();
                    numberConverter.setMaxFractionDigits(2);
                    numberConverter.setPattern("##.00");
                    parentValueHolder.setConverter(numberConverter);
                }
            }
        }
    }
}
