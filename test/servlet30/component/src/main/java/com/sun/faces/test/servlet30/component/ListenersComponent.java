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

package com.sun.faces.test.servlet30.component;

import java.util.Map;
import javax.faces.component.FacesComponent;
import javax.faces.component.html.HtmlInputText;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;
import javax.faces.event.ListenerFor;
import javax.faces.event.ListenersFor;
import javax.faces.event.PostValidateEvent;
import javax.faces.event.PreValidateEvent;

@FacesComponent(value = "com.sun.faces.test.servlet30.component.ListenersComponent")
@ListenersFor({
    @ListenerFor(systemEventClass = PreValidateEvent.class),
    @ListenerFor(systemEventClass = PostValidateEvent.class)
})
public class ListenersComponent extends HtmlInputText {

    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException {
        Map<String, Object> requestMap = FacesContext.getCurrentInstance().getExternalContext().getRequestMap();
        if (event instanceof PreValidateEvent) {
            requestMap.put("preValidateEvent", "preValidateEvent");
        } else if (event instanceof PostValidateEvent) {
            requestMap.put("postValidateEvent", "postValidateEvent");
        } else {
            super.processEvent(event);
        }
    }
}
