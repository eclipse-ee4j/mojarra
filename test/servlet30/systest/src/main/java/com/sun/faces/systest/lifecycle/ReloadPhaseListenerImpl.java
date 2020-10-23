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

package com.sun.faces.systest.lifecycle;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;


/**
 * <B>ReloadPhaseListener</B> is a class that looks for a Restore or Render
 * phase. If it finds that a phase has been entered other than a Restore or
 * Render, it sets a system property to false.
 *
 * This listener is used to determine whether a client refresh with no
 * request parameters or save state has occurred.
 *
 */
public class ReloadPhaseListenerImpl implements PhaseListener {

    PhaseId phaseId = null;
    String pageRefresh;


    public ReloadPhaseListenerImpl(PhaseId newPhaseId) {
        phaseId = newPhaseId;
        pageRefresh = "true";
    }


    public void afterPhase(PhaseEvent event) {
        System.setProperty("PageRefreshPhases", pageRefresh);
    }


    public void beforePhase(PhaseEvent event) {
        if (event.getPhaseId() == PhaseId.RESTORE_VIEW) {
            //reset System property to true when starting phase processing
            pageRefresh = "true";
            return;
        } else if (event.getPhaseId() == PhaseId.RENDER_RESPONSE) {
            //no other phases should be called
            return;
        }

        //phase other than Restore or Render is called
        pageRefresh = "false";
    }


    public PhaseId getPhaseId() {
        return phaseId;
    }

} // end of class ReloadPhaseListenerImpl

