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

package com.sun.faces.test.servlet30.el;

import java.beans.FeatureDescriptor;
import java.io.Serializable;
import java.util.Iterator;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.NoneScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@NoneScoped
public class GetFeatureDescriptorsBean implements Serializable {
    private static final long serialVersionUID = -8697181848858606709L;
    
    
    String[] implictNames = new String[]{
        "application", "applicationScope", "cc", "component", "cookie", "facesContext",
        "flowScope",
        "header", "headerValues", "initParam", "param", "paramValues",
        "request", "requestScope", "resource", "session", "sessionScope", 
        "view", "viewScope" };
    
    public String getTopLevelVariables() {
        String result = "FAILED";
        FacesContext context = FacesContext.getCurrentInstance();
        ELResolver elResolver = context.getApplication().getELResolver();
        ELContext elContext = context.getELContext();
        
        Iterator<FeatureDescriptor> featureDescriptors = elResolver.getFeatureDescriptors(elContext, null);
        StringBuilder builder = new StringBuilder();
        while (featureDescriptors.hasNext()) {
            FeatureDescriptor cur = featureDescriptors.next();
            builder.append("<p>").append(cur.getName()).append("</p>");
        }
        if (0 < builder.length()) {
            result = builder.toString();
        }
        
        return result;
    }
        
}
