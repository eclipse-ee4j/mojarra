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

package com.sun.faces.application.view;

import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.SystemEvent;
import jakarta.faces.event.ViewMapListener;

/**
 * The ViewScope event listener.
 *
 * <p>
 * To make it so the UIViewRoot.getViewMap is as independent as possible from implementation specific code we need to
 * get notified when a view map is created or destroyed. This ViewMapListener is registered so we get notified of these
 * events.
 * </p>
 *
 * <p>
 * See MojarraRuntimePopulator for the actual registration of this listener.
 * </p>
 */
public class ViewScopeEventListener implements ViewMapListener {

    /**
     * Handle the system event.
     *
     * @param se the system event.
     * @throws AbortProcessingException
     */
    @Override
    public void processEvent(SystemEvent se) throws AbortProcessingException {
        ViewScopeManager.getInstance(FacesContext.getCurrentInstance()).processEvent(se);
    }

    /**
     * Is listener for.
     *
     * @param source the source.
     * @return true if UIViewRoot, false otherwise.
     */
    @Override
    public boolean isListenerForSource(Object source) {
        return source instanceof UIViewRoot;
    }
}
