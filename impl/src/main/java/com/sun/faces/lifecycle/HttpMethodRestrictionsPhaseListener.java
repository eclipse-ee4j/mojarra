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

package com.sun.faces.lifecycle;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletRequest;


public class HttpMethodRestrictionsPhaseListener implements PhaseListener {
    
    private static final long serialVersionUID = 179883834600711161L;

    public HttpMethodRestrictionsPhaseListener() {
    }
    
    @Override
    public void afterPhase(PhaseEvent event) {
    }

    @Override
    public void beforePhase(PhaseEvent event) {
        FacesContext context = event.getFacesContext();
        ExternalContext extContext = context.getExternalContext();
        
        Object requestObj = extContext.getRequest();
        if (requestObj instanceof HttpServletRequest) {
            String method = ((HttpServletRequest)requestObj).getMethod();
            if (method.equals("OPTIONS")) {
                context.responseComplete();
            }
        }

    }

    @Override
    public PhaseId getPhaseId() {
        return PhaseId.RESTORE_VIEW;
    }
    
    
    
}
