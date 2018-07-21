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

package com.sun.faces.test.servlet30.ajax;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;

import javax.enterprise.context.SessionScoped;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.event.ActionEvent;
import javax.inject.Named;

@Named
@SessionScoped
public class Issue2578Bean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Date date;
    private Date date2;
    private String text;
    private String text2;

    public String getText2() {
        return text2;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public Date getDate2() {
        return date2;
    }

    public void setDate2(Date date2) {
        this.date2 = date2;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void altClear(ActionEvent event) {
        this.date = null;
        this.date2 = null;
        this.text = null;
        this.text2 = null;
    }

    public void clear(ActionEvent event) {
        altClear(event);
    }

    private UIComponent getContainingForm(UIComponent component) {
        UIComponent previous = component;
        UIComponent parent = component.getParent();

        while (parent != null) {
            if (parent instanceof UIForm) {
                return parent;
            }
            previous = parent;
            parent = parent.getParent();
        }
        return previous;
    }

    protected void resetChildren(UIComponent component) {
        Iterator<UIComponent> kids = component.getFacetsAndChildren();
        while (kids.hasNext()) {
            UIComponent kid = kids.next();
            if (kid instanceof EditableValueHolder) {
                EditableValueHolder editable = (EditableValueHolder) kid;
                editable.resetValue();
            }
            resetChildren(kid);
        }
    }
}
