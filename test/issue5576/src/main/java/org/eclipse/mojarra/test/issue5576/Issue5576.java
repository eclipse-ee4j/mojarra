/*
 * Copyright (c) Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GPL-2.0 with Classpath-exception-2.0 which
 * is available at https://openjdk.java.net/legal/gplv2+ce.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 or Apache-2.0
 */
package org.eclipse.mojarra.test.issue5576;

import java.io.Serializable;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

@Named
@ViewScoped
public class Issue5576 implements Serializable {

    private static final long serialVersionUID = 1L;
    private String group;
    private String subgroup1Item1;
    private String subgroup1Item2;
    private String subgroup2Item1;
    private String string1;

    public void save() {
    }

    public void clean() {
        this.group = null;
        this.subgroup1Item1 = null;
        this.subgroup1Item2 = null;
        this.subgroup2Item1 = null;
        this.string1 = null;
    }

    public String getGroup() {
        return this.group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getSubgroup1Item1() {
        return this.subgroup1Item1;
    }

    public void setSubgroup1Item1(String subgroup1Item1) {
        this.subgroup1Item1 = subgroup1Item1;
    }

    public String getSubgroup1Item2() {
        return this.subgroup1Item2;
    }

    public void setSubgroup1Item2(String subgroup1Item2) {
        this.subgroup1Item2 = subgroup1Item2;
    }

    public String getSubgroup2Item1() {
        return this.subgroup2Item1;
    }

    public void setSubgroup2Item1(String subgroup2Item1) {
        this.subgroup2Item1 = subgroup2Item1;
    }
    
    public String getString1() {
        return this.string1;
    }
    
    public void setString1(String string1) {
        this.string1 = string1;
    }
}