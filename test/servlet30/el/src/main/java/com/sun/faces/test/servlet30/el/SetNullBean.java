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

package com.sun.faces.test.servlet30.el;

import java.io.Serializable;
import javax.el.ELException;
import javax.el.ValueExpression;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

@ManagedBean(name = "setNullBean")
@RequestScoped
public class SetNullBean implements Serializable {

    public String getTest1() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        SetNullTestBean setNullTestBean = new SetNullTestBean();

        facesContext.getExternalContext().
                getSessionMap().put("setNullTestBean", setNullTestBean);

        ValueBinding valueBinding = facesContext.
                getApplication().createValueBinding("#{sessionScope.setNullTestBean.one}");
        valueBinding.setValue(facesContext, null);

        return setNullTestBean.getOne() == null ? "SUCCESS" : "FAILED";
    }

    public String getTest2() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        SetNullTestBean setNullTestBean = new SetNullTestBean();
        SetNullInnerTestBean inner = new SetNullInnerTestBean();

        facesContext.getExternalContext().
                getSessionMap().put("setNullTestBean", setNullTestBean);

        ValueBinding valueBinding = facesContext.
                getApplication().createValueBinding("#{sessionScope.setNullTestBean.inner}");
        valueBinding.setValue(facesContext, inner);

        return setNullTestBean.getInner() != null ? "SUCCESS" : "FAILED";
    }

    public String getTest3() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        SetNullTestBean setNullTestBean = new SetNullTestBean();
        SetNullInnerTestBean inner = new SetNullInnerTestBean();

        facesContext.getExternalContext().
                getSessionMap().put("setNullTestBean", setNullTestBean);

        ValueBinding valueBinding = facesContext.
                getApplication().createValueBinding("#{sessionScope.setNullTestBean.inner}");
        valueBinding.setValue(facesContext, inner);

        valueBinding = facesContext.
                getApplication().createValueBinding("#{sessionScope.setNullTestBean.inner}");
        valueBinding.setValue(facesContext, null);

        return setNullTestBean.getInner() == null ? "SUCCESS" : "FAILED";
    }

    public String getTest4() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        SetNullTestBean setNullTestBean = new SetNullTestBean();
        SetNullInnerTestBean inner = new SetNullInnerTestBean();

        facesContext.getExternalContext().
                getSessionMap().put("setNullTestBean", setNullTestBean);

        ValueBinding valueBinding = facesContext.
                getApplication().createValueBinding("#{sessionScope.setNullTestBean.inner}");
        valueBinding.setValue(facesContext, inner);

        valueBinding = facesContext.
                getApplication().createValueBinding("#{sessionScope.setNullTestBean.inner}");
        valueBinding.setValue(facesContext, inner);

        boolean exceptionThrown = false;
        valueBinding = facesContext.
                getApplication().createValueBinding("#{sessionScope.setNullTestBean.inner.test4}");
        try {
            valueBinding.setValue(facesContext, null);
        } catch (javax.faces.el.EvaluationException ee) {
            exceptionThrown = true;
        }
        return exceptionThrown ? "SUCCESS" : "FAILED";
    }
    
    public String getTest5() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        SetNullTestBean setNullTestBean = new SetNullTestBean();

        facesContext.getExternalContext().
                getSessionMap().put("setNullTestBean", setNullTestBean);

        ValueExpression valueExpression = facesContext.getApplication().getExpressionFactory().
                createValueExpression(facesContext.getELContext(), "#{sessionScope.setNullTestBean.one}", Object.class);
        valueExpression.setValue(facesContext.getELContext(), null);

        return setNullTestBean.getOne() == null ? "SUCCESS" : "FAILED";
    }

    public String getTest6() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        SetNullTestBean setNullTestBean = new SetNullTestBean();
        SetNullInnerTestBean inner = new SetNullInnerTestBean();

        facesContext.getExternalContext().
                getSessionMap().put("setNullTestBean", setNullTestBean);

        ValueExpression valueExpression = facesContext.getApplication().getExpressionFactory().
                createValueExpression(facesContext.getELContext(), "#{sessionScope.setNullTestBean.inner}", Object.class);
        valueExpression.setValue(facesContext.getELContext(), inner);

        return setNullTestBean.getInner() != null ? "SUCCESS" : "FAILED";
    }

    public String getTest7() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        SetNullTestBean setNullTestBean = new SetNullTestBean();
        SetNullInnerTestBean inner = new SetNullInnerTestBean();

        facesContext.getExternalContext().
                getSessionMap().put("setNullTestBean", setNullTestBean);

        ValueExpression valueExpression = facesContext.getApplication().getExpressionFactory().
                createValueExpression(facesContext.getELContext(), "#{sessionScope.setNullTestBean.inner}", Object.class);
        valueExpression.setValue(facesContext.getELContext(), inner);
        
        valueExpression = facesContext.getApplication().getExpressionFactory().
                createValueExpression(facesContext.getELContext(), "#{sessionScope.setNullTestBean.inner}", Object.class);
        valueExpression.setValue(facesContext.getELContext(), null);

        return setNullTestBean.getInner() == null ? "SUCCESS" : "FAILED";
    }

    public String getTest8() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        SetNullTestBean setNullTestBean = new SetNullTestBean();
        SetNullInnerTestBean inner = new SetNullInnerTestBean();

        facesContext.getExternalContext().
                getSessionMap().put("setNullTestBean", setNullTestBean);

        ValueExpression valueExpression = facesContext.getApplication().getExpressionFactory().
                createValueExpression(facesContext.getELContext(), "#{sessionScope.setNullTestBean.inner}", Object.class);
        valueExpression.setValue(facesContext.getELContext(), inner);

        valueExpression = facesContext.getApplication().getExpressionFactory().
                createValueExpression(facesContext.getELContext(), "#{sessionScope.setNullTestBean.inner}", Object.class);
        valueExpression.setValue(facesContext.getELContext(), inner);

        boolean exceptionThrown = false;
        valueExpression = facesContext.getApplication().getExpressionFactory().
                createValueExpression(facesContext.getELContext(), "#{sessionScope.setNullTestBean.inner.test4}", Object.class);
        try {
            valueExpression.setValue(facesContext.getELContext(), null);
        } catch (ELException ee) {
            exceptionThrown = true;
        }
        return exceptionThrown ? "SUCCESS" : "FAILED";
    }
}
