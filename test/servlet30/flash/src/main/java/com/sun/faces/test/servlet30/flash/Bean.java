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

package com.sun.faces.test.servlet30.flash;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.enterprise.context.RequestScoped;
import javax.faces.event.ComponentSystemEvent;

@RequestScoped
@Named
public class Bean {

    protected String stringVal;

    private Long selectedEventId;

    public void loadTrainingEvent(ComponentSystemEvent cse) {
        Long eventId = getSelectedEventId();
        FacesContext context = FacesContext.getCurrentInstance();
        if (null == eventId) {
            context.addMessage(null, new FacesMessage("The training event you requested is invalid"));
            context.getExternalContext().getFlash().setKeepMessages(true);
            context.getApplication().getNavigationHandler().handleNavigation(context, null, "/index?faces-redirect=true");
        }
    }

    public String getStringVal() {
        return stringVal;
    }

    public void setStringVal(String stringVal) {
        this.stringVal = stringVal;

        if (null != stringVal && stringVal.equals("addMessage")) {
            FacesContext context = FacesContext.getCurrentInstance();
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "test that this persists across the redirect",
                    "This message must persist across the redirect");
            context.addMessage(null, message);
            context.getExternalContext().getFlash().setKeepMessages(true);
        }
    }

    public Long getSelectedEventId() {
        return selectedEventId;
    }

    public void setSelectedEventId(Long selectedEventId) {
        this.selectedEventId = selectedEventId;
    }

    public String start() {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("wizardId", 4711);
        return "flash12?faces-redirect=true";
    }

    public String test2087() {
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("foo", "read strobist");
        ;
        return "flash13?faces-redirect=true";
    }
}
