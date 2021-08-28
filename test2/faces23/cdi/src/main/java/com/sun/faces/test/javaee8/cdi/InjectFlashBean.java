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

package com.sun.faces.test.javaee8.cdi;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.Flash;

@Named
@RequestScoped
public class InjectFlashBean {
    
    @Inject
    private Flash flash;
    
    @Inject
    private ExternalContext context;
    
    public void preRenderView() {
        if (context.getRequestParameterMap().containsKey("setFlash")) {
            flash.put("foo", "bar");
        }
    }
    
    public String getFlashValue() {
        if (context.getRequestParameterMap().containsKey("getFlash")) {
            return (String) flash.get("foo");
        }
        
        return "";
    }

}
