/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named
@RequestScoped
public class RepeatRemovedBean implements Serializable {

    private static final long serialVersionUID = 1L;
    List<Integer> ints = new ArrayList<Integer>();
    ValueHolderList vl = new ValueHolderList();

    public RepeatRemovedBean() {
        ints.add(1);
    }

    public ValueHolderList getList() {
        return vl;
    }

    public void removeInt(int index) {
        ints.remove(index);
    }

    public class ValueHolder {

        int index;

        public ValueHolder(int index) {
            super();
            this.index = index;
        }

        public int getValue() {
            return ints.get(index);
        }

        public void setValue(int value) {
            ints.set(index, value);
        }
    }

    public class ValueHolderList extends AbstractList<ValueHolder> {

        public ValueHolderList() {
            super();
        }

        @Override
        public ValueHolder get(int index) {
            return new ValueHolder(index);
        }

        @Override
        public int size() {
            return ints.size();
        }
    }
}
