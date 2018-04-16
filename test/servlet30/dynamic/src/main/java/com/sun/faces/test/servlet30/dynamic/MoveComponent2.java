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

package com.sun.faces.test.servlet30.dynamic;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.PreRenderViewEvent;
import javax.faces.event.SystemEvent;
import javax.faces.event.SystemEventListener;

@FacesComponent(value = "com.sun.faces.test.servlet30.dynamic.MoveComponent2")
public class MoveComponent2 extends UIComponentBase implements SystemEventListener {

    @Override
    public String getFamily() {
        return "HTML_BASIC";
    }

    public MoveComponent2() {
        FacesContext context = FacesContext.getCurrentInstance();

        if (!context.isPostback()) {
            context.getViewRoot().subscribeToViewEvent(PreRenderViewEvent.class, this);
        }
    }

    @Override
    public boolean isListenerForSource(Object source) {
        return true;
    }

    @Override
    public void processEvent(SystemEvent event) throws AbortProcessingException {

        String forValue = (String) getAttributes().get("for");

        if (forValue != null) {
            UIComponent component = FacesContext.getCurrentInstance().getViewRoot().findComponent(forValue);

            for (UIComponent childComponent : getChildren()) {
                component.getChildren().add(childComponent);
            }
        }
    }
}
