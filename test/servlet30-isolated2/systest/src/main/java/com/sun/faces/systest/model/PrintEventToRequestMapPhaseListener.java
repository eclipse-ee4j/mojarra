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

import javax.faces.event.PhaseListener;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseEvent;

import javax.faces.context.FacesContext;

public class PrintEventToRequestMapPhaseListener extends Object implements PhaseListener {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public void afterPhase(PhaseEvent event) {
        String attr = (String) event.getFacesContext().getExternalContext().getRequestMap().get("afterPhaseEvent");
        if (null == attr) {
            attr = "";
        }
        event.getFacesContext().getExternalContext().getRequestMap().put("afterPhaseEvent", attr + " afterPhase: " + event.getPhaseId());
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        String attr = (String) event.getFacesContext().getExternalContext().getRequestMap().get("beforePhaseEvent");
        if (null == attr) {
            attr = "";
        }
        event.getFacesContext().getExternalContext().getRequestMap().put("beforePhaseEvent", attr + " beforePhase: " + event.getPhaseId());
    }

    private String phaseIdString = null;

    public void setPhaseIdString(String phaseIdString) {
        this.phaseIdString = phaseIdString;
    }

    /**
     * <p>
     * Look at our phaseIdString ivar. If non-null, use this as the phaseId for the substring search
     * below. If null, look in the request map for an attribute called "phaseId". If not found, return
     * <code>PhaseId.ANY_PHASE</code>. If found, see if it is a substring of any of the known phases, if
     * so, return the corresponding phase Id. If not return <code>PhaseId.ANY_PHASE</code>.
     * </p>
     */

    @Override
    public PhaseId getPhaseId() {

        String phaseId = (null != phaseIdString) ? phaseIdString
                : (String) FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("phaseId");
        PhaseId result = PhaseId.ANY_PHASE;
        if (null == phaseId) {
            return result;
        }
        if (-1 != PhaseId.ANY_PHASE.toString().indexOf(phaseId)) {
            result = PhaseId.ANY_PHASE;
        } else if (-1 != PhaseId.APPLY_REQUEST_VALUES.toString().indexOf(phaseId)) {
            result = PhaseId.APPLY_REQUEST_VALUES;
        } else if (-1 != PhaseId.PROCESS_VALIDATIONS.toString().indexOf(phaseId)) {
            result = PhaseId.PROCESS_VALIDATIONS;
        } else if (-1 != PhaseId.UPDATE_MODEL_VALUES.toString().indexOf(phaseId)) {
            result = PhaseId.UPDATE_MODEL_VALUES;
        } else if (-1 != PhaseId.INVOKE_APPLICATION.toString().indexOf(phaseId)) {
            result = PhaseId.INVOKE_APPLICATION;
        }
        return result;
    }

    public PhaseListener getInstance() {
        return this;
    }

    protected PhaseListener otherListener = null;

    public PhaseListener getOtherListener() {
        return otherListener;
    }

    public void setOtherListener(PhaseListener newOtherListener) {
        otherListener = newOtherListener;
    }

}
