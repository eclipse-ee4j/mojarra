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

package com.sun.faces.systest.state;

import java.util.Map;
import javax.faces.application.StateManager;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import static javax.faces.application.StateManager.IS_SAVING_STATE;

@ManagedBean
@RequestScoped
public class CaptureIsSavingStateValueBean {

    public String getRemoveMessagesFromSession() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
        sessionMap.remove(StateManager.IS_SAVING_STATE);
        sessionMap.remove("beforeMessage");
        sessionMap.remove("afterMessage");
        return "";
    }

    public void afterPhase(PhaseEvent pe) {
        if (pe.getPhaseId() == PhaseId.RENDER_RESPONSE) {
            FacesContext context = FacesContext.getCurrentInstance();
            Map<Object, Object> contextAttrs = context.getAttributes();
            Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
            sessionMap.put("afterMessage",
                    null == contextAttrs.get(IS_SAVING_STATE) ? "no value" :
                        contextAttrs.get(IS_SAVING_STATE));
        }
    }

    public void beforePhase(PhaseEvent pe) {
        if (pe.getPhaseId() == PhaseId.RENDER_RESPONSE) {
            FacesContext context = FacesContext.getCurrentInstance();
            Map<Object, Object> contextAttrs = context.getAttributes();
            Map<String, Object> sessionMap = context.getExternalContext().getSessionMap();
            sessionMap.put("beforeMessage",
                    null == contextAttrs.get(IS_SAVING_STATE) ? "no value" :
                        contextAttrs.get(IS_SAVING_STATE));
        }
    }

}
