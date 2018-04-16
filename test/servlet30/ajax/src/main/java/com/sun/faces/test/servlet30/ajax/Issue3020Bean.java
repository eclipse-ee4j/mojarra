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

package com.sun.faces.test.servlet30.ajax;
    
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.RequestScoped;

@ManagedBean
@RequestScoped
public class Issue3020Bean implements Serializable {
    private String product;
    private List<String> products;

    @PostConstruct
    public void init() {
        products = new ArrayList<String>();
        String p = "a";
        for (int i = 0; i < 10; i++) {
            products.add(p);
            p += "a";
        }
    }

    public List<String> getMatchingProducts() {
        List<String> matchingProducts = new ArrayList<String>();
        if (product != null) {
            for (String p : products) {
                if (p.startsWith(product)) {
                    matchingProducts.add(p);
                }
            }
        }
        return matchingProducts;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }    
    
}
