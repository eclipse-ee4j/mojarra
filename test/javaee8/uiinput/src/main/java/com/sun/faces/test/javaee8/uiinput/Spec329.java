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

package com.sun.faces.test.javaee8.uiinput;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Model;
import javax.faces.model.SelectItem;

@Model
public class Spec329 {

    private List<Spec329Entity> entityList;
    private List<SelectItem> selectItemList;

    private Spec329Entity selectedEntity;
    private String selectedItem1;
    private String selectedItem2;
    private String selectedItem3;
    private String selectedItem4;

    @PostConstruct
    public void init() {
        entityList = new ArrayList<>();
        entityList.add(new Spec329Entity("one"));
        entityList.add(new Spec329Entity("two"));
        entityList.add(new Spec329Entity("three"));

        selectItemList = new ArrayList<>();
        selectItemList.add(new SelectItem("value1", "label1"));
        selectItemList.add(new SelectItem("value2", "label2"));
        selectItemList.add(new SelectItem("value3", "label3"));
    }

    public List<Spec329Entity> getEntityList() {
        return entityList;
    }

    public List<SelectItem> getSelectItemList() {
        return selectItemList;
    }

    public Spec329Entity getSelectedEntity() {
        return selectedEntity;
    }

    public void setSelectedEntity(Spec329Entity selectedEntity) {
        this.selectedEntity = selectedEntity;
    }

    public String getSelectedItem1() {
        return selectedItem1;
    }

    public void setSelectedItem1(String selectedItem1) {
        this.selectedItem1 = selectedItem1;
    }

    public String getSelectedItem2() {
        return selectedItem2;
    }

    public void setSelectedItem2(String selectedItem2) {
        this.selectedItem2 = selectedItem2;
    }

    public String getSelectedItem3() {
        return selectedItem3;
    }

    public void setSelectedItem3(String selectedItem3) {
        this.selectedItem3 = selectedItem3;
    }

    public String getSelectedItem4() {
        return selectedItem4;
    }

    public void setSelectedItem4(String selectedItem4) {
        this.selectedItem4 = selectedItem4;
    }

}
