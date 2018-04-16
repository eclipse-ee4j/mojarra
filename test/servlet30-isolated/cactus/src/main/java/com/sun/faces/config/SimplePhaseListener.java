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

package com.sun.faces.config;

import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

public class SimplePhaseListener implements PhaseListener {

    private static final String HANDLED_BEFORE_AFTER = "Handled Before After";

    private boolean handledBefore = false;
    private boolean handledAfter = false;


    public SimplePhaseListener() {
    }


    public void afterPhase(PhaseEvent event) {
        handledAfter = true;
        if (handledBefore && handledAfter) {
            System.setProperty(HANDLED_BEFORE_AFTER, HANDLED_BEFORE_AFTER);
        }
    }


    public void beforePhase(PhaseEvent event) {
        handledBefore = true;
    }


    public PhaseId getPhaseId() {
        return PhaseId.APPLY_REQUEST_VALUES;
    }
}
