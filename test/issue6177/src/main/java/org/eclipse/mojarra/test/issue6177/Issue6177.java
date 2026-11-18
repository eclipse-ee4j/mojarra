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

package org.eclipse.mojarra.test.issue6177;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;
import jakarta.inject.Named;

@Named
@RequestScoped
public class Issue6177 {

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
    private final List<Category> categories = List.of(new Category("x", List.of("x1", "x2")), new Category("y", List.of("y1")));
    private final List<SelectItem> selectItems = List.of(
        new SelectItem("1", "one"),
        new SelectItemGroup("group", null, false, new SelectItem[] { new SelectItem("2", "two"), new SelectItem("3", "three") }));
    private final Map<String, String> map = new TreeMap<>(Map.of("m1", "1", "m2", "2"));
    private final List<SelectItem> plainSelectItems = List.of(new SelectItem("1", "one"), new SelectItem("2", "two"));
    private String fromSelectItems;
    private String fromMap;
    private List<String> many;
    private String one;
    private String grouped;

    public List<Item> getItems() {
        return items;
    }

    public List<Category> getCategories() {
        return categories;
    }

    public List<SelectItem> getSelectItems() {
        return selectItems;
    }

    public List<SelectItem> getPlainSelectItems() {
        return plainSelectItems;
    }

    public String getFromSelectItems() {
        return fromSelectItems;
    }

    public void setFromSelectItems(String fromSelectItems) {
        this.fromSelectItems = fromSelectItems;
    }

    public String getFromMap() {
        return fromMap;
    }

    public void setFromMap(String fromMap) {
        this.fromMap = fromMap;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public List<String> getMany() {
        return many;
    }

    public void setMany(List<String> many) {
        this.many = many;
    }

    public String getOne() {
        return one;
    }

    public void setOne(String one) {
        this.one = one;
    }

    public String getGrouped() {
        return grouped;
    }

    public void setGrouped(String grouped) {
        this.grouped = grouped;
    }
}
