/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates.
 * Copyright (c) 2018 Payara Services Limited.
 * All rights reserved.
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
import java.util.Map;

import javax.el.ELException;
import javax.el.ValueExpression;
import javax.enterprise.context.RequestScoped;
import javax.faces.annotation.SessionMap;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequestScoped
public class SetNullBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    @SessionMap
    private Map<String, Object> sessionMap;

    @Inject
    private FacesContext facesContext;


    public String getTest1() {
        SetNullTestBean setNullTestBean = new SetNullTestBean();

        sessionMap.put("setNullTestBean", setNullTestBean);

        ValueExpression valueExpression =
                facesContext.getApplication()
                            .getExpressionFactory()
                            .createValueExpression(
                                facesContext.getELContext(),
                                "#{sessionScope.setNullTestBean.one}",
                                SetNullTestBean.class);

        valueExpression.setValue(facesContext.getELContext(), null);

        return setNullTestBean.getOne() == null ? "SUCCESS" : "FAILED";
    }

    public String getTest2() {
        SetNullTestBean setNullTestBean = new SetNullTestBean();
        SetNullInnerTestBean inner = new SetNullInnerTestBean();

        sessionMap.put("setNullTestBean", setNullTestBean);

        ValueExpression valueBinding =
                facesContext.getApplication()
                            .getExpressionFactory()
                            .createValueExpression(
                                  facesContext.getELContext(),
                                  "#{sessionScope.setNullTestBean.inner}",
                                  SetNullTestBean.class);

        valueBinding.setValue(facesContext.getELContext(), inner);

        return setNullTestBean.getInner() != null ? "SUCCESS" : "FAILED";
    }

    public String getTest3() {
        SetNullTestBean setNullTestBean = new SetNullTestBean();
        SetNullInnerTestBean inner = new SetNullInnerTestBean();

        sessionMap.put("setNullTestBean", setNullTestBean);

        ValueExpression valueBinding =
                facesContext.getApplication()
                            .getExpressionFactory()
                            .createValueExpression(
                                facesContext.getELContext(),
                                "#{sessionScope.setNullTestBean.inner}",
                                Object.class);

        valueBinding.setValue(facesContext.getELContext(), inner);

        valueBinding =
                facesContext.getApplication()
                            .getExpressionFactory()
                            .createValueExpression(
                                facesContext.getELContext(),
                                "#{sessionScope.setNullTestBean.inner}",
                                Object.class);

        valueBinding.setValue(facesContext.getELContext(), null);

        return setNullTestBean.getInner() == null ? "SUCCESS" : "FAILED";
    }

    public String getTest4() {
        SetNullTestBean setNullTestBean = new SetNullTestBean();
        SetNullInnerTestBean inner = new SetNullInnerTestBean();

        sessionMap.put("setNullTestBean", setNullTestBean);

        ValueExpression valueBinding = facesContext.getApplication().getExpressionFactory()
                .createValueExpression(facesContext.getELContext(), "#{sessionScope.setNullTestBean.inner}", Object.class);
        valueBinding.setValue(facesContext.getELContext(), inner);

        valueBinding = facesContext.getApplication().getExpressionFactory().createValueExpression(facesContext.getELContext(),
                "#{sessionScope.setNullTestBean.inner}", Object.class);
        valueBinding.setValue(facesContext.getELContext(), inner);

        boolean exceptionThrown = false;
        valueBinding = facesContext.getApplication().getExpressionFactory().createValueExpression(facesContext.getELContext(),
                "#{sessionScope.setNullTestBean.inner.test4}", Object.class);
        try {
            valueBinding.setValue(facesContext.getELContext(), null);
        } catch (Exception ee) {
            exceptionThrown = true;
        }
        return exceptionThrown ? "SUCCESS" : "FAILED";
    }

    public String getTest5() {
        SetNullTestBean setNullTestBean = new SetNullTestBean();

        sessionMap.put("setNullTestBean", setNullTestBean);

        ValueExpression valueExpression = facesContext.getApplication().getExpressionFactory()
                .createValueExpression(facesContext.getELContext(), "#{sessionScope.setNullTestBean.one}", Object.class);
        valueExpression.setValue(facesContext.getELContext(), null);

        return setNullTestBean.getOne() == null ? "SUCCESS" : "FAILED";
    }

    public String getTest6() {
        SetNullTestBean setNullTestBean = new SetNullTestBean();
        SetNullInnerTestBean inner = new SetNullInnerTestBean();

        sessionMap.put("setNullTestBean", setNullTestBean);

        ValueExpression valueExpression = facesContext.getApplication().getExpressionFactory()
                .createValueExpression(facesContext.getELContext(), "#{sessionScope.setNullTestBean.inner}", Object.class);
        valueExpression.setValue(facesContext.getELContext(), inner);

        return setNullTestBean.getInner() != null ? "SUCCESS" : "FAILED";
    }

    public String getTest7() {
        SetNullTestBean setNullTestBean = new SetNullTestBean();
        SetNullInnerTestBean inner = new SetNullInnerTestBean();

        sessionMap.put("setNullTestBean", setNullTestBean);

        ValueExpression valueExpression = facesContext.getApplication().getExpressionFactory()
                .createValueExpression(facesContext.getELContext(), "#{sessionScope.setNullTestBean.inner}", Object.class);
        valueExpression.setValue(facesContext.getELContext(), inner);

        valueExpression = facesContext.getApplication().getExpressionFactory().createValueExpression(facesContext.getELContext(),
                "#{sessionScope.setNullTestBean.inner}", Object.class);
        valueExpression.setValue(facesContext.getELContext(), null);

        return setNullTestBean.getInner() == null ? "SUCCESS" : "FAILED";
    }

    public String getTest8() {
        SetNullTestBean setNullTestBean = new SetNullTestBean();
        SetNullInnerTestBean inner = new SetNullInnerTestBean();

        sessionMap.put("setNullTestBean", setNullTestBean);

        ValueExpression valueExpression = facesContext.getApplication().getExpressionFactory()
                .createValueExpression(facesContext.getELContext(), "#{sessionScope.setNullTestBean.inner}", Object.class);
        valueExpression.setValue(facesContext.getELContext(), inner);

        valueExpression = facesContext.getApplication().getExpressionFactory().createValueExpression(facesContext.getELContext(),
                "#{sessionScope.setNullTestBean.inner}", Object.class);
        valueExpression.setValue(facesContext.getELContext(), inner);

        boolean exceptionThrown = false;
        valueExpression = facesContext.getApplication().getExpressionFactory().createValueExpression(facesContext.getELContext(),
                "#{sessionScope.setNullTestBean.inner.test4}", Object.class);
        try {
            valueExpression.setValue(facesContext.getELContext(), null);
        } catch (ELException ee) {
            exceptionThrown = true;
        }
        return exceptionThrown ? "SUCCESS" : "FAILED";
    }
}
