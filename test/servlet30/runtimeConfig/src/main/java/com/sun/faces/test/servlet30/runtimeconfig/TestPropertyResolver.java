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

// TestPropertyResolver.java

package com.sun.faces.test.servlet30.runtimeconfig;

import javax.faces.el.PropertyResolver;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;

public class TestPropertyResolver extends PropertyResolver {
   
    PropertyResolver root = null;
    public TestPropertyResolver(PropertyResolver propertyResolver) {       
        root = propertyResolver;
    }
    
    // Specified by javax.faces.el.PropertyResolver.getValue(Object,String)
    public Object getValue(Object base, Object property) {
         if (property.equals("customPRTest1")) {
            return "TestPropertyResolver";
         }
         return root.getValue(base, property);
    }

    public Object getValue(Object base, int index)
        throws EvaluationException, PropertyNotFoundException {
        return root.getValue(base, index);
    }


    public void setValue(Object base, Object name, Object value)
        throws EvaluationException, PropertyNotFoundException {
        root.setValue(base, name, value);
    }


    public void setValue(Object base, int index, Object value)
        throws EvaluationException, PropertyNotFoundException {
        root.setValue(base, index, value);
    }


    public boolean isReadOnly(Object base, Object name)
        throws PropertyNotFoundException {
        return root.isReadOnly(base, name);
    }


    public boolean isReadOnly(Object base, int index)
        throws PropertyNotFoundException {
        return root.isReadOnly(base, index);
    }


    public Class getType(Object base, Object name)
        throws PropertyNotFoundException {
        return root.getType(base, name);
    }


    public Class getType(Object base, int index)
        throws PropertyNotFoundException {
        return root.getType(base, index);
    }
    
}
