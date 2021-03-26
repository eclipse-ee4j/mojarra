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

/*
 * NewVariableResolver.java
 *
 * Created on April 29, 2006, 1:50 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.faces.test.servlet30.customvariableresolver;

import com.sun.faces.config.ConfigureListener;
import com.sun.faces.util.Util;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.VariableResolver;

/**
 *
 * @author edburns
 */
public class NewVariableResolver extends VariableResolver {
    
    private VariableResolver original = null;
    
    /** Creates a new instance of NewVariableResolver */
    public NewVariableResolver(VariableResolver original) {
        this.original = original;
        
        FacesContext.getCurrentInstance().getExternalContext().getApplicationMap().put("newVR", this);
    }

    public Object resolveVariable(FacesContext context, String name) throws EvaluationException {
        Object result = null;
        
        // This expects a plain old bean that is not configured as a Faces
        // managed bean.  However, an additional check is done to make sure
        // that is not configured as a managed bean.  So, we've resolved 
        // the name as a "non" managed bean, but want to do some additional
        // checks to make sure that name does not also resolve to a 
        // managed bean. 
        //  
        if (name.equals("nonmanaged")) {
            Object bean = null;
            Object managedBean = null;             
            try {
                Class clazz = Util.loadClass("com.sun.faces.systest.model.TestBean", context);
                bean = clazz.newInstance();
            } catch (Exception e) {
            } 
            managedBean = original.resolveVariable(context, name); 
            if (bean == null) {
                if (managedBean==null) {
                    return null;
                } else {
                    result = managedBean;
                }
            } else {
                result = bean;
            }
            return result;
        }

        if (name.equals("custom")) {
            result = "custom";
        }
        else {
            result = original.resolveVariable(context, name);
        }
        
        return result;
    }
    
}
