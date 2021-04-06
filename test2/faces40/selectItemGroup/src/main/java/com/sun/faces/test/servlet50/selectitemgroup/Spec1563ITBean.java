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

package com.sun.faces.test.servlet50.selectitemgroup;

import static java.util.Arrays.asList;

import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class Spec1563ITBean {

    private List<Product> animals;
    private Long selectedProductId;

    @PostConstruct
    public void init() {
        animals = asList(new Product(1L, "Dog"), new Product(2L, "Cat"), new Product(3L, "Fish"));
    }

    public List<Product> getAnimals() {
        return animals;
    }

    public Long getSelectedProductId() {
        return selectedProductId;
    }

    public void setSelectedProductId(Long selectedProductId) {
        this.selectedProductId = selectedProductId;
    }
}
