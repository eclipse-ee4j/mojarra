/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

@Named
@ApplicationScoped
public class AppBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private String sessionDestroyedMessage;

    public String getSessionDestroyedMessage() {
        return sessionDestroyedMessage;
    }

    public void setSessionDestroyedMessage(String sessionDestroyedMessage) {
        this.sessionDestroyedMessage = sessionDestroyedMessage;
    }

    public void invalidateSession() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession mySession = (HttpSession) context.getExternalContext().getSession(true);
        mySession.invalidate();
    }
}
