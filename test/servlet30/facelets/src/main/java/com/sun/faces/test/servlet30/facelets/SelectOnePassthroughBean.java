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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;

@Named
@RequestScoped
public class SelectOnePassthroughBean {

    private String name;
    private Map<String, String> map;

    private List<SelectItem> list;

    public List<SelectItem> getList() {
        list = new ArrayList<SelectItem>();
        SelectItem selectItem;
        selectItem = new SelectItem("aaaaa", "aaaaaaa");
        list.add(selectItem);
        selectItem = new SelectItem("bbbbbb", "bbbbbb");
        list.add(selectItem);
        selectItem = new SelectItem("cccccc", "cccccccc");
        list.add(selectItem);
        selectItem = new SelectItem("ddddddd", "dddddddd");
        list.add(selectItem);

        return list;
    }

    public void setList(List<SelectItem> list) {
        this.list = list;
    }

    public Map<String, String> getMap() {
        map = new LinkedHashMap<String, String>();
        map.put("aaaaaaa", "aaaaaaaaaa");
        map.put("bbbbbbb", "bbbbbbbbbb");
        map.put("ccccccc", "cccccccccc");
        map.put("ddddddd", "dddddddddd");
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
