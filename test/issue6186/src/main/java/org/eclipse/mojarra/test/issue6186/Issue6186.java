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
package org.eclipse.mojarra.test.issue6186;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;
import jakarta.inject.Named;

@Named
@RequestScoped
public class Issue6186 {

    public static class Product {

        private final String name;
        private final String sku;

        public Product(String name, String sku) {
            this.name = name;
            this.sku = sku;
        }

        public String getName() {
            return name;
        }

        public String getSku() {
            return sku;
        }
    }

    public static class Category {

        private final String name;
        private final List<Product> products;

        public Category(String name, List<Product> products) {
            this.name = name;
            this.products = products;
        }

        public String getName() {
            return name;
        }

        public List<Product> getProducts() {
            return products;
        }
    }

    private final List<Category> categories = List.of(
        new Category("fruit", List.of(new Product("Apple", "a1"), new Product("Pear", "p1"))),
        new Category("veg", List.of(new Product("Leek", "l1"))));
    private final List<SelectItem> selectItems = List.of(
        new SelectItem("1", "one"),
        new SelectItemGroup("group", null, false, new SelectItem[] { new SelectItem("2", "two"), new SelectItem("3", "three") }),
        new SelectItem("4", "four"));
    private String one;
    private List<String> many;

    public List<Category> getCategories() {
        return categories;
    }

    public List<SelectItem> getSelectItems() {
        return selectItems;
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
