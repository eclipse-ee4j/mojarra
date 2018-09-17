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

package com.sun.faces.component;

import javax.faces.component.FacesComponent;
import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ComponentSystemEventListener;
import javax.faces.event.ListenerFor;
import javax.faces.event.PostAddToViewEvent;

@ListenerFor(systemEventClass = PostAddToViewEvent.class)
@FacesComponent("PostAddTester")
public class PostAddTester extends UIComponentBase implements ComponentSystemEventListener {

    @Override
    public String getFamily() {
        return "PostAddTester";
    }

    @Override
    public void processEvent(ComponentSystemEvent cse) throws AbortProcessingException {
        UIComponent source = cse.getComponent();
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().getRequestMap().put("1682message", "source id: " + source.getId());

    }

}
