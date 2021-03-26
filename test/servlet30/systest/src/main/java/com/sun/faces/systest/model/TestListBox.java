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

import javax.faces.model.SelectItem;

import java.util.ArrayList;
import java.util.List;

public class TestListBox {
        private List<TestBean2> listResults;
        private TestBean2[] arrayResults;

        public List<TestBean2> getListResults() {
                return listResults;
        }
        public void setListResults(List<TestBean2> listResults) {
                this.listResults = listResults;
        }
        public TestBean2[] getArrayResults() {
                return arrayResults;
        }
        public void setArrayResults(TestBean2[] arrayResults) {
                this.arrayResults = arrayResults;
        }


        public List getSelectItems() {
                List selectItems = new ArrayList();
                selectItems.add(new SelectItem(new TestBean2(10, "joe"), "joe"));
                selectItems.add(new SelectItem(new TestBean2(20, "bob"), "bob"));
                selectItems.add(new SelectItem(new TestBean2(30, "fred"), "fred"));

                return selectItems;
        }
}
