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

package com.sun.faces.test.servlet30.faceletsFindChild;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import jakarta.faces.bean.*;

@SessionScoped
@ManagedBean(name = "pageAjaxTest2")
public class PageAjaxTest implements Serializable {

    private final NumberFormat format;

    public PageAjaxTest() {
        format = new DecimalFormat("#.##");
    }

    public void setInputTextValue(String textVal) {
    }

    public String getInputTextValue() {
        String result = "test test test test";

        return (result);
    }

    public String getRandomNumber() {
        Double x = Math.random() * 100;
        String retVal = x.toString();
        return retVal;
    }

    public String getRenderTime() {
        FacesContext context = FacesContext.getCurrentInstance();
        Long requestStart = (Long) context.getAttributes().get(TimerPhaseListener.REQUEST_START);
        double elapsedSeconds = ((double) (System.currentTimeMillis() - requestStart)) / 1000;
        return format.format(elapsedSeconds) + " seconds";
    }

    public void buttonSubmit() {
    }

}
