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

package com.sun.faces.test.javaee7.cdibeanvalidator;

import java.io.Serializable;
import java.util.Date;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.validator.ValidatorException;
import jakarta.inject.Named;

@Named
@SessionScoped
public class UserBean implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String firstName = "Duke";
    protected String lastName = "Java";
    protected Date dob;
    protected String sex = "Unknown";
    protected String email;
    protected String serviceLevel = "medium";

    public void validateEmail(FacesContext context, UIComponent toValidate, Object value) throws ValidatorException {
        String emailStr = (String) value;
        if (emailStr.indexOf("@") == -1) {
            throw new ValidatorException(new FacesMessage("Invalid email address"));
        }
    }

    public String addConfirmedUser() {
        // This method would call a database or other service and add the confirmed user information.
        // For now, we just place an informative message in request scope
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Successfully added new user"));
        return "done";
    }

    @FooConstraint
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getServiceLevel() {
        return serviceLevel;
    }

    public void setServiceLevel(String serviceLevel) {
        this.serviceLevel = serviceLevel;
    }

}
