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

package com.sun.faces.test.javaee7.cdiinitdestroyevent;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.enterprise.context.Initialized;
import javax.enterprise.context.Destroyed;
import javax.faces.flow.Flow;
import javax.faces.flow.FlowScoped;
import javax.inject.Inject;

@Dependent
public class FlowLogger {
    
    @Inject UserBean userBean;

    public FlowLogger() {
    }
        
    
    public void observeFlowStart(@Observes
    @Initialized(FlowScoped.class) Flow currentFlow) {
        long currentTime = System.currentTimeMillis();
        userBean.setInitFlowMessage("" + currentTime);
        userBean.setFlowId(currentFlow.getId());
    }
    
    public void observeFlowEnd(@Observes
    @Destroyed(FlowScoped.class) Flow currentFlow) {
        long currentTime = System.currentTimeMillis();
        userBean.setDestroyFlowMessage("" + currentTime);
        userBean.setFlowId(currentFlow.getId());
    }    
    
    
}
