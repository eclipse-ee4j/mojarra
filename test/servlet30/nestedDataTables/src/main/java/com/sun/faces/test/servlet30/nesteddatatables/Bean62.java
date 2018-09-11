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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.faces.model.ListDataModel;
import javax.inject.Named;


/**
 * @author edburns
 */
@Named("outer62")
@SessionScoped
public class Bean62 implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Holds value of property root.
     */
    private boolean root = true;

    /**
     * Holds value of property model.
     */
    private ListDataModel<Object> model;

    /**
     * Holds value of property label.
     */
    private String label = "root";

    /**
     * Holds value of property curStatus.
     */
    private Object curStatus;


    public Bean62() {
    }

    public Bean62(String label) {
        this.label = label;
    }

    /**
     * Getter for property model.
     *
     * @return Value of property model.
     */
    public ListDataModel<Object> getModel() {
        if (model == null) {
            List<Object> list = new ArrayList<>();
            if (isRoot()) {
                list.add(new Bean62(label + ".one"));
                list.add(new Bean62(label + ".two"));
                list.add(new Bean62(label + ".three"));
            } else {
                list.add("leaf1");
                list.add("leaf2");
                list.add("leaf3");
            }
            model = new ListDataModel<>(list);
        }
        return model;
    }

    /**
     * Setter for property model.
     *
     * @param model New value of property model.
     */
    public void setModel(ListDataModel<Object> model) {
        this.model = model;
    }

    /**
     * Getter for property root.
     *
     * @return Value of property root.
     */
    public boolean isRoot() {
        return root;
    }

    /**
     * Setter for property root.
     *
     * @param root New value of property root.
     */
    public void setRoot(boolean root) {
        this.root = root;
    }

    /**
     * Getter for property label.
     *
     * @return Value of property label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Setter for property label.
     *
     * @param label New value of property label.
     */
    public void setLabel(String label) {
        this.label = label;
    }

    public String action() {
        Bean62 yyyInstance = (Bean62) model.getRowData();
        Object wwwInstance = yyyInstance.getModel().getRowData();

        setCurStatus(wwwInstance);

        return null;
    }

    /**
     * Getter for property curStatus.
     *
     * @return Value of property curStatus.
     */
    public Object getCurStatus() {
        return curStatus;
    }

    /**
     * Setter for property curStatus.
     *
     * @param curStatus New value of property curStatus.
     */
    public void setCurStatus(Object curStatus) {
        this.curStatus = curStatus;
    }
}
