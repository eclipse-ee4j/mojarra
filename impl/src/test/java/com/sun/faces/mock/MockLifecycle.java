/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.mock;

import jakarta.faces.FacesException;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseListener;
import jakarta.faces.lifecycle.Lifecycle;
import jakarta.servlet.http.HttpServletResponse;

public class MockLifecycle extends Lifecycle {

    // ------------------------------------------------------------- Properties
    // --------------------------------------------------------- Public Methods
    @Override
    public void addPhaseListener(PhaseListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void execute(FacesContext context) throws FacesException {

    }

    @Override
    public PhaseListener[] getPhaseListeners() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removePhaseListener(PhaseListener listener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void render(FacesContext context) throws FacesException {
        if (null != context) {
            ExternalContext extContext = context.getExternalContext();
            if (null != extContext) {
                Object response = extContext.getResponse();
                if (response instanceof MockHttpServletResponse) {
                    ((MockHttpServletResponse) response).setStatus(HttpServletResponse.SC_OK);
                }
            }
        }
    }
}
