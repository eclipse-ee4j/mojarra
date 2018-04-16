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

package com.sun.faces.test.servlet31.facelets;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.FacesContext;

@ManagedBean
@RequestScoped
public class InputTextStyleDisappearingBean {

    private static final long serialVersionUID = 1L;
    private HtmlInputText input;

    public HtmlInputText getInput() {
        return input;
    }

    public void setInput(HtmlInputText input) {
        this.input = input;
    }

    public void changeColor() {
        FacesContext fc = FacesContext.getCurrentInstance();
        HtmlInputText hit = (HtmlInputText) fc.getViewRoot().findComponent(":form:input");
        hit.setStyle("background-color:blue");
    }

    public void changeValue() {
        FacesContext fc = FacesContext.getCurrentInstance();
        HtmlInputText hit = (HtmlInputText) fc.getViewRoot().findComponent(":form:input");
        hit.setValue("InputText CHANGED !!");
    }

    public void dummy() {
        System.out.println("Dummy ...");
    }
}
