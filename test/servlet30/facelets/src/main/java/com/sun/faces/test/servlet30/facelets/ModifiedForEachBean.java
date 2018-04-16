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

package com.sun.faces.test.servlet30.facelets;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean(name = "modifiedForEachBean")
@SessionScoped
public class ModifiedForEachBean {

    private boolean toggle;
    private Set<String> set = new HashSet<String>();
    private final Set<String> set1;
    private final Set<String> set2;
    private String setToShow = "";

    public ModifiedForEachBean() {
        toggle = true;
        setToShow = "SET1 - INIT";
        set1 = populateSet("-SET1");
        set2 = populateSet("-SET2");
        set.addAll(set1);
    }

    public Set<String> getSet() {
        return set;
    }

    public String getSetToShow() {
        return setToShow;
    }

    public String toggle() {
        this.toggle = !this.toggle;
        if (toggle) {
            setToShow = "SET1";
            set.clear();
            set.addAll(set1);
        } else {
            setToShow = "SET2";
            set.clear();
            set.addAll(set2);
        }
        return null;
    }

    private TreeSet<String> populateSet(String suffix) {
        TreeSet<String> newSet = new TreeSet<String>();
        for (int j = 0; j < 3; j++) {
            newSet.add(j + suffix);
        }
        return newSet;
    }
}
