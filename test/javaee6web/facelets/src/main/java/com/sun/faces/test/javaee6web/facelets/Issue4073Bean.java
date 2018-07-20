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

package com.sun.faces.test.javaee6web.facelets;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

@Named("sampleMBean")
@SessionScoped
public class Issue4073Bean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String button2SetValue;

    public String getButton2SetValue() {
        return button2SetValue;
    }

    public void setButton2SetValue(String button2SetValue) {
        this.button2SetValue = button2SetValue;
    }

    public void initialize() {
        System.out.println("# called initialize()");
    }

    public String button1() {
        System.out.println("# called button1()");
        return null;
    }

    public String button2(String value) {
        System.out.println("# called button2() value=" + value);
        button2SetValue = value;
        return null;
    }

    public String getText() {
        return "Hello World";
    }

}
