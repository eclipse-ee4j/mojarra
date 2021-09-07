/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.application;

import java.io.IOException;

import jakarta.faces.application.StateManager;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * A <code>StateManager</code> implementation to meet the requirements of the specification.
 * </p>
 */
public class StateManagerImpl extends StateManager {

    /**
     * Write out the state.
     *
     * @param context the Faces context.
     * @param state the state.
     * @throws IOException when an I/O error occurs.
     */
    @Override
    public void writeState(FacesContext context, Object state) throws IOException {
        context.getRenderKit()
               .getResponseStateManager()
               .writeState(context, state);
    }

}
