/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates.
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

package com.sun.faces.test.servlet30.composite;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;


@Named
@SessionScoped
public class Issue1943Bean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String groupToAdd = "select one";

    @Inject
    private FacesContext context;

    public void valueChange(ValueChangeEvent vce) throws AbortProcessingException {
        System.err.println("VALUECHANGE CALLED!!!");
        context.addMessage(null, new FacesMessage(SEVERITY_INFO, "valueChange Called...", null));
    }

    public String removeGroup() {
        System.err.println("REMOVEGROUP CALLED!!!");
        context.addMessage(null, new FacesMessage(SEVERITY_INFO, "removeGroup Called...", null));
        return null;
    }

    public String getGroupToAdd() {
        return groupToAdd;
    }

    public void setGroupToAdd(String groupToAdd) {
        this.groupToAdd = groupToAdd;
    }
}
