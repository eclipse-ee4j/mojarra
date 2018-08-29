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

package com.sun.faces.test.servlet30.facelets;

import static java.lang.Boolean.TRUE;

import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.inject.Named;

@Named
@RequestScoped
public class DynamicValidatorBean {

    private transient UIComponent parentContainer;

    private String value;

    private UIInput validatedInput;

    public UIComponent getParent() {
        return this.parentContainer;
    }

    public void setParent(UIComponent container) {
        this.parentContainer = container;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void add() {
        FacesContext context = FacesContext.getCurrentInstance();

        Map<String, Object> appMap = context.getExternalContext().getApplicationMap();
        appMap.put("javax.faces.private.BEANS_VALIDATION_AVAILABLE", TRUE);

        Validator<?> dynamicValidator = context.getApplication().createValidator("javax.faces.Required");
        validatedInput = (UIInput) parentContainer.findComponent("validatedInput");
        validatedInput.addValidator(dynamicValidator);

    }

}
