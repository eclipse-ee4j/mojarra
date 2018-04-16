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

package com.sun.faces.test.javaee7.cdimethodvalidation;

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolationException;

@Named
@RequestScoped
public class UserBean implements Serializable {
    
    @Inject
    HelloService hello;
    
    protected String methodValidationValue = "foo";
    protected String valueValidationValue = "foo";
    
    public UserBean() {}

    @FooConstraint
    public String getValueValidationValue() {
        return valueValidationValue;
    }

    public void setValueValidationValue(String ValueValidationValue) {
        this.valueValidationValue = ValueValidationValue;
    }

    public String getMethodValidationValue() {
        return methodValidationValue;
    }

    public void setMethodValidationValue(String firstName) {
        this.methodValidationValue = firstName;
    }
    
    public String getHelloValue() {
        return hello.sayHello(getMethodValidationValue());
    }
    
    public void preRenderViewListener() {
        FacesContext context = FacesContext.getCurrentInstance();
        
        String value = (String) context.getExternalContext().getRequestMap().get("value");
        if (null != value && value.equals("bar")) {
            try {
                hello.sayHello(value);
            } catch (ConstraintViolationException e) {
                FacesMessage m = new FacesMessage(e.getMessage());
                context.addMessage(null, m);
            }
        }
        
    }
    
}

