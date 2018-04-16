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

package com.sun.faces.test.cluster.flash.basic;

import java.io.Serializable;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@RequestScoped
public class FlashMessagesBean implements Serializable {
    
    private static final long serialVersionUID = 119534842073074733L;

    @ManagedProperty(value="#{facesContext}")
    protected FacesContext facesContext;

    public FacesContext getFacesContext() {
        return facesContext;
    }

    public void setFacesContext(FacesContext facesContext) {
        this.facesContext = facesContext;
    }


    protected String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Mesage 1", "survives redirect");
        getFacesContext().addMessage(null, message);
        message = new FacesMessage(FacesMessage.SEVERITY_INFO,
                "Mesage 2", "survives redirect");
        getFacesContext().addMessage(null, message);
        getFacesContext().getExternalContext().getFlash().setKeepMessages(true);

    }


}
