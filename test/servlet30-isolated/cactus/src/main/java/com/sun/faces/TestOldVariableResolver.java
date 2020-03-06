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

// TestOldVariableResolver.java

package com.sun.faces;

import javax.faces.el.VariableResolver;
import javax.faces.el.EvaluationException;
import javax.faces.context.FacesContext;

public class TestOldVariableResolver extends VariableResolver {
   
    VariableResolver resolver = null;
    public TestOldVariableResolver(VariableResolver variableResolver) {
       this.resolver = variableResolver;
    }
    
    //
    // Relationship Instance Variables
    // 

    // Specified by jakarta.faces.el.VariableResolver.resolveVariable()
    public Object resolveVariable(FacesContext context, String name)
            throws EvaluationException {
        if (name.equals("customVRTest2")) {
            return "TestOldVariableResolver";
        }
        return resolver.resolveVariable(context, name);
    }

}
