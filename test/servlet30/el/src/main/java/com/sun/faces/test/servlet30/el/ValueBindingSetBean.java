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

package com.sun.faces.test.servlet30.el;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

@ManagedBean(name = "valueBindingSetBean")
@RequestScoped
public class ValueBindingSetBean {

    private String value1;

    private String value2 = "two";

    private String value3;

    private String value4 = "four";

    public String getTest1() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ValueBinding valueBinding = facesContext.getApplication().createValueBinding("#{valueBindingSetBean.value1}");
        valueBinding.setValue(facesContext, "one");
        ValueBindingSetBean bean = (ValueBindingSetBean) facesContext.getExternalContext().getRequestMap().get("valueBindingSetBean");
        return bean.getValue1();
    }

    public String getTest2() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ValueBinding valueBinding = facesContext.getApplication().createValueBinding("#{valueBindingSetBean.value2}");
        valueBinding.setValue(facesContext, null);
        ValueBindingSetBean bean = (ValueBindingSetBean) facesContext.getExternalContext().getRequestMap().get("valueBindingSetBean");
        String value = bean.getValue2();
        if (value == null || value.trim().equals("")) {
            value = "NULL";
        }
        return value;
    }
    
    public String getTest3() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ValueBinding valueBinding = facesContext.getApplication().createValueBinding("#{requestScope['valueBindingSetBean'].value3}");
        valueBinding.setValue(facesContext, "three");
        ValueBindingSetBean bean = (ValueBindingSetBean) facesContext.getExternalContext().getRequestMap().get("valueBindingSetBean");
        return bean.getValue3();
    }
    
    public String getTest4() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ValueBinding valueBinding = facesContext.getApplication().createValueBinding("#{requestScope['valueBindingSetBean'].value4}");
        valueBinding.setValue(facesContext, null);
        ValueBindingSetBean bean = (ValueBindingSetBean) facesContext.getExternalContext().getRequestMap().get("valueBindingSetBean");
        String value = bean.getValue4();
        if (value == null || value.trim().equals("")) {
            value = "NULL";
        }
        return value;
    }
    
    public String getTest5() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ValueBinding valueBinding = facesContext.getApplication().createValueBinding("#{value5}");
        valueBinding.setValue(facesContext, "five");
        return (String) facesContext.getExternalContext().getRequestMap().get("value5");
    }
    
    public String getTest6() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.getExternalContext().getRequestMap().put("value6", "five");
        ValueBinding valueBinding = facesContext.getApplication().createValueBinding("#{value6}");
        valueBinding.setValue(facesContext, "six");
        return (String) facesContext.getExternalContext().getRequestMap().get("value6");
    }
    
    public String getValue1() {
        return value1;
    }
    
    public String getValue2() {
        return value2;
    }
    
    public String getValue3() {
        return value3;
    }
    
    public String getValue4() {
        return value4;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }

    public void setValue3(String value3) {
        this.value3 = value3;
    }

    public void setValue4(String value4) {
        this.value4 = value4;
    }
}
