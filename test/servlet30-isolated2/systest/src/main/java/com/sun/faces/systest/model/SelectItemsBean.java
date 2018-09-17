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

import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.faces.model.SelectItem;
import javax.faces.model.SelectItemGroup;

@Named
public class SelectItemsBean {

    private SelectItem selectedHobbit;
    private List<SelectItem> ctorHobbits;

    public List<SelectItem> getCtorHobbits() {
        return ctorHobbits;
    }

    public SelectItem getSelectedHobbit() {
        return selectedHobbit;
    }

    public void setSelectedHobbit(SelectItem selectedHobbit) {
        this.selectedHobbit = selectedHobbit;
    }

    public SelectItemsBean() {
        this.ctorHobbits = new ArrayList<SelectItem>();
        SelectItem initialValue = new SelectItem("Frodo");
        setSelectedHobbit(initialValue);
        this.ctorHobbits.add(initialValue);
        this.ctorHobbits.add(new SelectItem("Pippin"));
        this.ctorHobbits.add(new SelectItem("Bilbo"));
        this.ctorHobbits.add(new SelectItem("Merry"));
    }

    public List<SelectItem> getHobbits() {
        List<SelectItem> result = new ArrayList<SelectItem>(4);
        result.add(new SelectItem("Frodo"));
        result.add(new SelectItem("Pippin"));
        result.add(new SelectItem("Bilbo"));
        result.add(new SelectItem("Merry"));
        return result;
    }

    public List<SelectItem> getHobbitsNestedInGroup() {
        List<SelectItem> result = new ArrayList<SelectItem>();
        SelectItemGroup group = new SelectItemGroup("Hobbits");
        group.setSelectItems(getHobbits().toArray(new SelectItem[0]));
        result.add(group);
        return result;
    }

    public List<SelectItem> getHobbitsNoSelectionNestedInGroup() {
        List<SelectItem> result = new ArrayList<SelectItem>();
        SelectItemGroup group = new SelectItemGroup("Hobbits");
        group.setSelectItems(getHobbitsNoSelection().toArray(new SelectItem[0]));
        result.add(group);
        return result;
    }

    public List<SelectItem> getHobbitsNoSelection() {
        List<SelectItem> result = new ArrayList<SelectItem>(5);
        SelectItem noSelectionOption = new SelectItem("No Selection");
        noSelectionOption.setNoSelectionOption(true);
        result.add(noSelectionOption);
        result.addAll(getHobbits());
        return result;
    }

}
