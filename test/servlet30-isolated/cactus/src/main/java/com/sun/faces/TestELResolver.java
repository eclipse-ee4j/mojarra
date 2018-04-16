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

package com.sun.faces;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.beans.FeatureDescriptor;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.component.UIViewRoot;

import javax.el.ELException;
import javax.el.PropertyNotWritableException;
import javax.el.PropertyNotFoundException;
import javax.el.PropertyNotFoundException;
import javax.el.ELContext;
import javax.el.ELResolver;

import com.sun.faces.el.ELConstants;
import com.sun.faces.util.Util;

public class TestELResolver extends ELResolver {

    public TestELResolver() {
    }

    public Object getValue(ELContext context,Object base, Object property) 
            throws ELException {
        return null;
    }

    public void  setValue(ELContext context, Object base, Object property,
        Object val) throws ELException {
       
    }

    public boolean isReadOnly(ELContext context, Object base, Object property) 
        throws ELException{
          
        return false;	
    }

    public Class getType(ELContext context, Object base, Object property) 
        throws ELException {
        return Object.class;
    } 

    public Iterator getFeatureDescriptors(ELContext context, Object base) {
        return null;
    }

    public Class getCommonPropertyType(ELContext context, Object base) {
        if (base != null) {
            return null;
        }
        return String.class;
    }
}
