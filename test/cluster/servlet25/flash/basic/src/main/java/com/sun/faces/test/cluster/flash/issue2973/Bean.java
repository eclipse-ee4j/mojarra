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

package com.sun.faces.test.cluster.flash.issue2973;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.context.FacesContext;

@ManagedBean(name="bean")
public class Bean implements Serializable {
    
    private static final long serialVersionUID = -4183580262084818534L;
    
    private String text;
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }
    
    public String nextpage (){
        FacesContext.getCurrentInstance().getExternalContext().getFlash().put("bean", this);
        return "page2?faces-redirect=true";
    }
    
    public String simulateServerRestart() {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getExternalContext().getApplicationMap().remove("csfcff");
        try {
            Thread.currentThread().sleep(3000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Bean.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
}
