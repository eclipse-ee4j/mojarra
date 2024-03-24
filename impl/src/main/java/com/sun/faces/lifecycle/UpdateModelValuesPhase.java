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

package com.sun.faces.lifecycle;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;

import jakarta.faces.FacesException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.PhaseId;

/**
 * UpdateModelValuesPhase executes <code>processUpdates</code> on each component in the tree so that it may have a
 * chance to update its model value.
 */
public class UpdateModelValuesPhase extends Phase {

    // Log instance for this class
    private static Logger LOGGER = FacesLogger.LIFECYCLE.getLogger();

    // ---------------------------------------------------------- Public Methods

    @Override
    public void execute(FacesContext facesContext) {

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Entering UpdateModelValuesPhase");
        }
        UIComponent component = facesContext.getViewRoot();
        assert null != component;

        try {
            component.processUpdates(facesContext);
        } catch (RuntimeException re) {
            String exceptionMessage = re.getMessage();
            if (null != exceptionMessage) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, exceptionMessage, re);
                }
            }
            throw new FacesException(exceptionMessage, re);
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Exiting UpdateModelValuesPhase");
        }

    }

    @Override
    public PhaseId getId() {

        return PhaseId.UPDATE_MODEL_VALUES;

    }

// The testcase for this class is TestUpdateModelValuesPhase.java

} // end of class UpdateModelValuesPhase
