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

package com.sun.faces.composite;

import java.util.Map;
import javax.faces.component.UINamingContainer;
import javax.faces.component.FacesComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;

@FacesComponent(value = "javaTopLevelComponent")
public class JavaTopLevelComponent extends UINamingContainer {

    public JavaTopLevelComponent() {

    }

    public String getFamily() {
        return "jakarta.faces.NamingContainer";
    }
    private String item = "Value hard coded in Java source file";

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    private int intv;

    public void setInt(int intv) {
        this.intv = intv;
    }

    public int getInt() {
        return intv;
    }

    public void forwardIfNotInRole(ComponentSystemEvent cse) {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, Object> requestMap = context.getExternalContext().getRequestMap();
        String message = "Received event: " + cse.getClass().getName() + " for component: " +
                cse.getComponent().getClass().getName();
        requestMap.put("message", message);
    }

}
