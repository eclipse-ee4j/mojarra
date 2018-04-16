/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: MockLifecycle.java,v 1.1 2005/10/18 17:47:58 edburns Exp $
 */



package com.sun.faces.mock;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseListener;
import javax.faces.lifecycle.Lifecycle;
import javax.servlet.http.HttpServletResponse;


public class MockLifecycle extends Lifecycle {


    // ------------------------------------------------------------- Properties


    // --------------------------------------------------------- Public Methods


    public void addPhaseListener(PhaseListener listener) {
        throw new UnsupportedOperationException();
    }


    public void execute(FacesContext context) throws FacesException {
        
    }


    public PhaseListener[] getPhaseListeners() {
        throw new UnsupportedOperationException();
    }

    public void removePhaseListener(PhaseListener listener) {
        throw new UnsupportedOperationException();
    }


    public void render(FacesContext context) throws FacesException {
        if (null != context) {
            ExternalContext extContext = context.getExternalContext();
            if (null != extContext) {
                Object response = extContext.getResponse();
                if (response instanceof MockHttpServletResponse) {
                    ((MockHttpServletResponse)response).setStatus(HttpServletResponse.SC_OK);
                }
            }
        }

    }


}
