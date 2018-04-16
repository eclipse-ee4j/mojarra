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

package com.sun.faces;

import javax.faces.event.SystemEventListener;
import javax.faces.event.SystemEvent;
import javax.faces.event.AbortProcessingException;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;

public class SystemEventListener1 implements SystemEventListener {
    public void processEvent(SystemEvent event)
          throws AbortProcessingException {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ctx.getAttributes().put("SystemEventListener1", Boolean.TRUE);
    }

    public boolean isListenerForSource(Object source) {
        return (source instanceof UIOutput);
    }
}
