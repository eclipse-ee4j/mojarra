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

import javax.faces.event.PhaseListener;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;

/**
 * Created by IntelliJ IDEA. User: rlubke Date: Dec 2, 2008 Time: 2:00:51 PM To
 * change this template use File | Settings | File Templates.
 */
public class PhaseListenerB implements PhaseListener {

    public void afterPhase(PhaseEvent event) {
        // no-op
    }

    public void beforePhase(PhaseEvent event) {
        // no-op
    }

    public PhaseId getPhaseId() {
        return PhaseId.ANY_PHASE;
    }

}
