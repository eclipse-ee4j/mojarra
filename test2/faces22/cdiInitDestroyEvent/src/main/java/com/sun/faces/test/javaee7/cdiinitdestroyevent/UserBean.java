/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.test.javaee7.cdiinitdestroyevent;

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

    private String firstName = "Duke";
    private String lastName = "Java";
    private Date dob;
    private String sex = "Unknown";
    private String email;
    private String serviceLevel = "medium";

    private String initMessage;
    private String destroyMessage;
    private String destroyViewScopeMessage;
    private String initFlowMessage;
    private String destroyIssue2997FlowMessage;

    public void validateEmail(FacesContext context, UIComponent toValidate, Object value) throws ValidatorException {
        String emailStr = (String) value;
        if (emailStr.indexOf("@") == -1) {
            throw new ValidatorException(new FacesMessage("Invalid email address"));
        }
    }

    public String addConfirmedUser() {
        // This method would call a database or other service and add the
        // confirmed user information.
        // For now, we just place an informative message in request scope
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Successfully added new user"));
        return "done";
    }

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

    public String getInitMessage() {
        return initMessage;
    }

    public void setInitMessage(String initMessage) {
        this.initMessage = initMessage;
    }

    public String getDestroyMessage() {
        return destroyMessage;
    }

    public void setDestroyMessage(String destroyMessage) {
        this.destroyMessage = destroyMessage;
    }

    private String flowId;

    public String getFlowId() {
        return flowId;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }



    public String getInitFlowMessage() {
        return initFlowMessage;
    }

    public void setInitFlowMessage(String initFlowMessage) {
        this.initFlowMessage = initFlowMessage;
    }

    private String destroyFlowMessage;

    public String getDestroyFlowMessage() {
        return destroyFlowMessage;
    }

    public void setDestroyFlowMessage(String destroyFlowMessage) {
        this.destroyFlowMessage = destroyFlowMessage;
    }

    private String initViewScopeMesasge;

    public String getInitViewScopeMesasge() {
        return initViewScopeMesasge;
    }

    public void setInitViewScopeMesasge(String initViewScopeMesasge) {
        this.initViewScopeMesasge = initViewScopeMesasge;
    }

    public String getDestroyViewScopeMessage() {
        return destroyViewScopeMessage;
    }

    public void setDestroyViewScopeMessage(String destroyViewScopeMessage) {
        this.destroyViewScopeMessage = destroyViewScopeMessage;
    }

    public String getDestroyIssue2997FlowMessage() {
        return destroyIssue2997FlowMessage;
    }

    public void setDestroyIssue2997FlowMessage(String destroyIssue2997FlowMessage) {
        this.destroyIssue2997FlowMessage = destroyIssue2997FlowMessage;
    }

}
