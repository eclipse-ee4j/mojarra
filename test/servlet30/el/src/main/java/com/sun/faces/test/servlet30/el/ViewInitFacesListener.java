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

package com.sun.faces.test.servlet30.el;

import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;
import java.util.Map;

/**
 * A system event listener that registers as a preRenderComponent event listener.
 */
public class ViewInitFacesListener implements SystemEventListener {
    /**
     * Constructor.
     */
    public ViewInitFacesListener() {
        FacesContext fc = FacesContext.getCurrentInstance();
        UIViewRoot viewRoot = fc.getViewRoot();
        if (viewRoot != null) {
            Map viewMap = viewRoot.getViewMap();
            if (viewMap != null) {
                if (FacesContext.getCurrentInstance() != null &&
                        FacesContext.getCurrentInstance().getClass().getName().equals("com.sun.faces.config.InitFacesContext")) {
                    FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().put("viewMapCreated", "Yes");
                }
            }
        }
    }

    /**
     * Process the event.
     * 
     * @param event the system event.
     * @throws AbortProcessingException when processing needs to be aborted.
     */
    public void processEvent(SystemEvent event) throws AbortProcessingException {
    }

    /**
     * Is a listener for source.
     * 
     * @param source the source.
     * @return true or false.
     */
    public boolean isListenerForSource(Object source) {
        if (source instanceof UIViewRoot) {
            return true;
        }
        return false;
    }
}
