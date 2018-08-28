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

package com.sun.faces.systest;

import static javax.faces.application.FacesMessage.SEVERITY_INFO;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@RequestScoped
public class MessageBean {

    @Inject
    private FacesContext context;

    protected String value;

    public MessageBean() {
        FacesMessage message = new FacesMessage(SEVERITY_INFO, "This is the summary", "This is the detail");
        context.addMessage("output1", message);
        context.addMessage("output2", message);
    }

    public String getValue() {
        return "";
    }

    public void setValue(String value) {
        this.value = value;
    }

}
