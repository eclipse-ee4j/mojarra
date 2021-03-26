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

package com.sun.faces.systest.model.ajax;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;


@ManagedBean
@SessionScoped
public class AjaxTable {


    private Info[] list = new Info[]{
            new Info(101, "Ryan", "Mountain Fastness", true),
            new Info(102, "Jim", "Santa Clara", true),
            new Info(103, "Roger", "Boston", true),
            new Info(104, "Ed", "Orlando", false),
            new Info(105, "Barbera", "Mountain View", true),
    };

    public Info[] getList() {
        return list;
    }

    public class Info {
        int id;
        String name;
        String city;
        boolean likescheese;

        public Info(int id, String name, String city, boolean likescheese) {
            this.id = id;
            this.name = name;
            this.city = city;
            this.likescheese = likescheese;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }


        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public boolean getLikesCheese() {
            return likescheese;
        }

        public void setLikesCheese(boolean likescheese) {
            this.likescheese = likescheese;
        }

        public String getCheesePreference() {
            return (likescheese ? "Cheese Please" : "Eww");
        }

    }


}
