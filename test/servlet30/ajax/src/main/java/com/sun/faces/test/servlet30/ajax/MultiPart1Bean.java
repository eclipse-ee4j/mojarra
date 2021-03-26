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

import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

@ManagedBean(name="multiPart1Bean")
@ViewScoped
public class MultiPart1Bean implements Serializable {
    
    public void delay() {
        
        Integer count = (Integer) FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get("multiPart1Count");
        if (count == null) {
            count = new Integer(1);
            FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().put("multiPart1Count", count);
        } else {
            count = count + 1;
            FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().put("multiPart1Count", count);
        }
        
        try {
            Thread.sleep(2000);
            
        } catch(InterruptedException ex) {
            ex.printStackTrace();
        }
    }
    
    public String getCount() {
        return "count is " + FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().get("multiPart1Count");
    }
}
