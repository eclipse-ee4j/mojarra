/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.util.ArrayList;
import java.util.List;
import jakarta.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

@ManagedBean(name = "nestedForEachBean")
@ViewScoped
public class NestedForEachBean implements Serializable {

    List<List<NestedForEachItem>> items;

    @PostConstruct
    public void init() {
        items = new ArrayList();

        for (int i = 0; i < 3; i++) {
            List<NestedForEachItem> list = new ArrayList();
            list.add(new NestedForEachItem("list" + i + "item1"));
            list.add(new NestedForEachItem("list" + i + "item2"));
            list.add(new NestedForEachItem("list" + i + "item3"));
            items.add(list);
        }
    }

    public void add(List<NestedForEachItem> list) {
        list.add(new NestedForEachItem("new" + list.size()));
    }

    public List<List<NestedForEachItem>> getItems() {
        return items;
    }
}
