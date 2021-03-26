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

package com.sun.faces.systest.model;

import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;
import javax.faces.component.UIViewRoot;
import javax.faces.event.PhaseListener;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.application.FacesMessage;
import java.util.List;

@ManagedBean
public class ListenerCheckBean {

    public void checkListeners() {

        FacesContext ctx = FacesContext.getCurrentInstance();
        UIViewRoot root = ctx.getViewRoot();
        List<PhaseListener> listeners = root.getPhaseListeners();
        if (listeners == null || listeners.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                "ERROR: No listeners registered",
                                                "ERROR: No listeners registered");
            ctx.addMessage(null, msg);
        }
        if (listeners.size() > 1) {
            String message = "ERROR: Expected one registered listener but found: " + listeners.size();
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                                message,
                                                message);
            ctx.addMessage(null, msg);
        }
    }

    public PhaseListener getListener() {

        return new PhaseListener() {
            public void afterPhase(PhaseEvent event) {

            }

            public void beforePhase(PhaseEvent event) {

            }

            public PhaseId getPhaseId() {
                return PhaseId.ANY_PHASE;
            }
        };
    }
}
