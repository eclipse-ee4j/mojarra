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

package com.sun.faces.config;

public class SimpleBean {

    private String simpleProperty;


    public SimpleBean() {
    }


    public String getSimpleProperty() {
        return simpleProperty;
    }


    public void setSimpleProperty(String simpleProperty) {
        this.simpleProperty = simpleProperty;
    }


    Integer intProp = null;


    public void setIntProperty(Integer newVal) {
        intProp = newVal;
    }


    public Integer getIntProperty() {
        return intProp;
    }


    public boolean getTrueValue() {
        return true;
    }


    public boolean getFalseValue() {
        return false;
    }

    private NonManagedBean nonManagedBean = null;
    public NonManagedBean getNonManagedBean() {
        return nonManagedBean;
    }
    public void setNonManagedBean(NonManagedBean nmb) {
        nonManagedBean = nmb;
    }

    private String headerClass = "column-header";
    public String getHeaderClass() {
        return headerClass;
    }
    public void setHeaderClass(String headerClass) {
        this.headerClass = headerClass;
    }
    private String footerClass = "column-footer";
    public String getFooterClass() {
        return footerClass;
    }
    public void setFooterClass(String footerClass) {
        this.footerClass = footerClass;
    }
}
