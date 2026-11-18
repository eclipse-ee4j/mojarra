/*
 * Copyright (c) Contributors to Eclipse Foundation.
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

package org.eclipse.mojarra.test.issue6176;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;
import jakarta.inject.Named;

@Named
@RequestScoped
public class Issue6176 {

    public static class Item {

        private final String value;
        private final boolean flag;

        public Item(String value, boolean flag) {
            this.value = value;
            this.flag = flag;
        }

        public String getValue() {
            return value;
        }

        public boolean isFlag() {
            return flag;
        }
    }

    public static class Category {

        private final String name;
        private final List<String> values;

        public Category(String name, List<String> values) {
            this.name = name;
            this.values = values;
        }

        public String getName() {
            return name;
        }

        public List<String> getValues() {
            return values;
        }
    }

    private final List<Item> items = List.of(new Item("a", true), new Item("b", false));
    private final List<String> strings = List.of("a", "b");
    private final List<SelectItem> selectItems = List.of(
        new SelectItem("1", "one"),
        new SelectItemGroup("group", null, false, new SelectItem[] { new SelectItem("2", "two"), new SelectItem("3", "three") }));
    private final List<Category> categories = List.of(new Category("x", List.of("x1", "x2")), new Category("y", List.of("y1")));
    private String one;
    private List<String> many;

    public List<Item> getItems() {
        return items;
    }

    public List<String> getStrings() {
        return strings;
    }

    public List<SelectItem> getSelectItems() {
        return selectItems;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public String getOne() {
        return one;
    }

    public void setOne(String one) {
        this.one = one;
    }

    public List<String> getMany() {
        return many;
    }

    public void setMany(List<String> many) {
        this.many = many;
    }
}
