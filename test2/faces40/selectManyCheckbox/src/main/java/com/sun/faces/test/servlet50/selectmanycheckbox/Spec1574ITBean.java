/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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

package com.sun.faces.test.servlet50.selectmanycheckbox;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class Spec1574ITBean {

    private List<Category> categories = new ArrayList<>();
    private Long selectedProductId;

    @PostConstruct
    public void init() {
        categories.add(new Category("Animals", new Product(1L, "Dog"), new Product(2L, "Cat"), new Product(3L, "Fish")));
        categories.add(new Category("Cars", new Product(4L, "Alfa Romeo"), new Product(5L, "Audi"), new Product(6L, "BMW")));
    }

    public List<Category> getCategories() {
        return categories;
    }

    public Long getSelectedProductId() {
        return selectedProductId;
    }

    public void setSelectedProductId(Long selectedProductId) {
        this.selectedProductId = selectedProductId;
    }
}
