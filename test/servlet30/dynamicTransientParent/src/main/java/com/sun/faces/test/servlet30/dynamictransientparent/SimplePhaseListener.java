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

package com.sun.faces.test.servlet30.dynamictransientparent;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIForm;
import javax.faces.component.UIPanel;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

public class SimplePhaseListener implements PhaseListener {

    private boolean afterInvokeAppPhase = false;

    public SimplePhaseListener() {
    }

    public void afterPhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();
        String msg = null;
        if (event.getPhaseId() == PhaseId.INVOKE_APPLICATION) {
            afterInvokeAppPhase = true;
            if (transientSubTreeExists(context)) {
                msg = " After "+event.getPhaseId()+" Transient Subtree Exists";
                context.addMessage("ia", new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
            } else {
                msg = " After "+event.getPhaseId()+" Transient Subtree Does Not Exist";
                context.addMessage("ia", new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
            }
        }
        if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
            if (afterInvokeAppPhase) {
                if (transientSubTreeExists(event.getFacesContext())) {
                    msg = " After "+event.getPhaseId()+" Transient Subtree Exists";
                    context.addMessage("rv", new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
                } else {
                    msg = " After "+event.getPhaseId()+" Transient Subtree Does Not Exist";
                    context.addMessage("rv", new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
                }
                afterInvokeAppPhase = false;
            }
        }
    }

    public void beforePhase(PhaseEvent event) {
    }


    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

    private boolean transientSubTreeExists(FacesContext context) {
        UIViewRoot root = context.getViewRoot();
        UIForm form = (UIForm)root.findComponent("helloForm");
        if (null == form) {
            System.err.println("FORM IS NULL");
            return false;
        }
        UIPanel panel = (UIPanel)form.findComponent("addto");
        if (panel.getChildren().size() > 0) {
            return true;
        } else {
            return false;
        }
    }

}
