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

package com.sun.faces.test.servlet30.composite;

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.component.UIOutput;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ComponentSystemEvent;

@ManagedBean(name = "event1Bean")
public class Event1Bean implements Serializable {

    public void listener(ComponentSystemEvent event)
            throws AbortProcessingException {
        FacesContext context = FacesContext.getCurrentInstance();
        UIOutput output = (UIOutput) context.getViewRoot().findComponent("form:out");
        output.setValue("LISTENER CALLED");
    }
}
