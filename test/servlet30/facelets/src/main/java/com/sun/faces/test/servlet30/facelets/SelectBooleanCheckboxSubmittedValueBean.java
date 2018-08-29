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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.faces.test.servlet30.facelets;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Named;

@Named
@SessionScoped
public class SelectBooleanCheckboxSubmittedValueBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean box1;
    private Boolean box2;

    public Boolean getBox1() {
        return box1;
    }

    public Boolean getBox2() {
        return box2;
    }

    public void setBox1(Boolean box1) {
        this.box1 = box1;
    }

    public void setBox2(Boolean box2) {
        this.box2 = box2;
    }

    public void valueChange(ValueChangeEvent event) throws AbortProcessingException {
        FacesContext.getCurrentInstance().renderResponse();
    }
}
