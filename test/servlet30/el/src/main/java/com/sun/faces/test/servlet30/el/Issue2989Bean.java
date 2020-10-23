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

import java.util.Date;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.NoneScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

@ManagedBean
@NoneScoped
public class Issue2989Bean {
    
    private static final int FACES_PHASE = 0;
    
    
    private ELContext[] getELContexts(FacesContext context) {
        
        ELContext[] contexts = new ELContext[1];
        contexts[FACES_PHASE] = context.getELContext();
        

        return contexts;

    }
    
    private ELResolver[] getELResolvers(ELContext[] contexts) {
        
        ELResolver[] resolvers = new ELResolver[1];
        resolvers[FACES_PHASE] = contexts[FACES_PHASE].getELResolver();
        
        return resolvers;
        
    }
    
    public String getTestResult() {
        FacesContext context = FacesContext.getCurrentInstance();
        
        ELContext[] elContexts = getELContexts(context);
        ELResolver[] resolvers = getELResolvers(elContexts);
        boolean passed = true;
        
        for (int i = 0, size = resolvers.length; i < size; i++) {
            
            // if an object is associated with the name "description" in
            // either request, session, or application return that object
            // (searching in that order)
            ExternalContext extContext = context.getExternalContext();
            extContext.getRequestMap().put("description", "request");
            extContext.getSessionMap().put("description", "session");
            extContext.getApplicationMap().put("description", "application");
            
            String result = (String)
                    resolvers[i].getValue(elContexts[i], null, "description");
            
            if (!"request".equals(result)) {
                passed = false;
                throw new IllegalStateException("Test FAILED.  Expected managed bean resolver to " +
                        "search request scope first, but instead it searched" +
                        result);
            }
            
            extContext.getRequestMap().remove("description");
            
            result = (String)
                    resolvers[i].getValue(elContexts[i], null, "description");
            
            if (!"session".equals(result)) {
                passed = false;
                throw new IllegalStateException("Test FAILED.  Expected managed bean resolver to " +
                        "search session scope first, but instead it searched" +
                        result);
            }
            
            extContext.getSessionMap().remove("description");
            
            result = (String)
                resolvers[i].getValue(elContexts[i], null, "description");

            if (!"application".equals(result)) {
                passed = false;
                throw new IllegalStateException("Test FAILED.  Expected managed bean resolver to " +
                            "search application scope first, but instead it searched" +
                            result);
            }

            extContext.getApplicationMap().remove("description");

            Date date = (Date)
                resolvers[i].getValue(elContexts[i], null, "description");

            if (date == null) {
                passed = false;
                throw new IllegalStateException("Test FAILED.  ELResolver.getValue() with a null " +
                        "base and a property matching the name of a managed" +
                            " bean did not result in the bean being instantiated" +
                            " and returned.");
            } else {
                if (!elContexts[i].isPropertyResolved()) {
                    passed = false;
                    throw new IllegalStateException("Test FAILED.  A new managed bean was created," +
                                " but isPropertyResolved() returned false.");
                }
            }
            passed = true;
        }        

        return passed ? "PASSED" : "FAILED";
    }
    
}
