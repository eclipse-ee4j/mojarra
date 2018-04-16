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

@ManagedBean
@ViewScoped
public class ViewScopeBean implements Serializable{

    public ViewScopeBean() {

        if (null == System.getProperty("RESULT")) {
            System.setProperty("RESULT", "VIEWCOPEBEAN() CALLED ");
        } else {
            String result = System.getProperty("RESULT");
            result += "VIEWSCOPEBEAN() CALLED ";
            System.setProperty("RESULT", result);
        }

    }

    public String getBeanVal() {
        if (null == System.getProperty("RESULT")) {
            System.setProperty("RESULT", "");
        }
       
        return System.getProperty("RESULT");
    }

    // Reset property to the single constructor load so we can determine if the 
    // constructor is called more than once.
    public void reset() {
        System.setProperty("RESULT", "VIEWSCOPEBEAN() CALLED ");
    }
    
}
