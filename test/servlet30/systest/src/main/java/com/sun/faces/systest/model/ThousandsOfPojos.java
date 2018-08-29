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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named
@SessionScoped
public class ThousandsOfPojos implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Item> items;

    public ThousandsOfPojos() {
        int size = 2000;
        items = new ArrayList<Item>(size);
        Item cur;
        String curStr;
        for (int i = 0; i < size; i++) {
            curStr = Long.toHexString(System.currentTimeMillis());
            cur = new Item("a" + curStr, "b" + curStr, "c" + curStr);
            items.add(cur);
        }
    }

    public List<Item> getItems() {
        return items;
    }

    public class Item {
        String a, b, c;
        InnerItem inner;

        public Item(String a, String b, String c) {
            this.a = a;
            this.b = b;
            this.c = c;

            inner = new InnerItem(a + b, a + c);
        }

        public String getA() {
            return a;
        }

        public void setA(String a) {
            this.a = a;
        }

        public String getB() {
            return b;
        }

        public void setB(String b) {
            this.b = b;
        }

        public String getC() {
            return c;
        }

        public void setC(String c) {
            this.c = c;
        }

        public InnerItem getInner() {
            return inner;
        }

        public void setInner(InnerItem inner) {
            this.inner = inner;
        }

    }

    public class InnerItem {
        String d, e;

        public InnerItem(String d, String e) {
            this.d = d;
            this.e = e;
        }

        public String getD() {
            return d;
        }

        public void setD(String d) {
            this.d = d;
        }

        public String getE() {
            return e;
        }

        public void setE(String e) {
            this.e = e;
        }

    }

    // </editor-fold>

}
