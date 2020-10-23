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

package com.sun.faces.test.servlet30.ajax;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.AjaxBehaviorEvent;

@ManagedBean(name = "issue2666Bean")
@SessionScoped
public class Issue2666Bean {

    
    public Issue2666Bean() {
    }

    public String msg = "";

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void processAjax(AjaxBehaviorEvent event) throws AbortProcessingException {
        boolean buttonParamExists = false;
        FacesContext context = FacesContext.getCurrentInstance();
        Iterator iter = context.getExternalContext().getRequestParameterNames();
        while (iter.hasNext()) {
            String name = (String)iter.next();
            if (name.equals("button")) {
                buttonParamExists = true;
                break;
            }
            if (buttonParamExists) {
                msg = "Request parameter name 'button' exists";
            } else {
                msg = "Request parameter name 'button' does not exist";
            }
        }
        
    } 
}
