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

package com.sun.faces.test.javaee6web.flowmethodcall;

import javax.faces.flow.FlowScoped;
import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named
@FlowScoped("flow-a")
public class Flow_a_Bean implements Serializable {

    public String getName() {
        return "Flow_a_Bean";
    }

    public String getReturnValue() {
        return "/return1";
    }

    public String methodWithOutcome() {
        return "next_b";
    }

    public void voidMethod() {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().getRequestMap().put("message", "voidMethod called in flow-a");
    }
}
