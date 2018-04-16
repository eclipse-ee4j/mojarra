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

package com.sun.faces.el;

import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.PropertyResolver;

import javax.faces.context.FacesContext;

/**
 * Default propertyResolver implementation that gets the ELContext from the 
 * argument FacesContext and calls setPropertyResolved(false) on it. This is
 * provided to ensure that the legacy property resolvers continue to work with
 * unfied EL API
 */
@SuppressWarnings("deprecation")
public class DummyPropertyResolverImpl extends PropertyResolver {
    
    @Override
    public Object getValue(Object base, Object property)
        throws EvaluationException, PropertyNotFoundException {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getELContext().setPropertyResolved(false);
        return null;
    }


    @Override
    public Object getValue(Object base, int index)
        throws EvaluationException, PropertyNotFoundException {
       FacesContext context = FacesContext.getCurrentInstance();
       context.getELContext().setPropertyResolved(false);
       return null;         
    }


    @Override
    public void setValue(Object base, Object property, Object value)
        throws EvaluationException, PropertyNotFoundException {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getELContext().setPropertyResolved(false);
    }

    @Override
    public void setValue(Object base, int index, Object value)
        throws EvaluationException, PropertyNotFoundException {
       FacesContext context = FacesContext.getCurrentInstance();
       context.getELContext().setPropertyResolved(false);
    }

    @Override
    public boolean isReadOnly(Object base, Object property)
        throws EvaluationException, PropertyNotFoundException {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getELContext().setPropertyResolved(false);        
        return false;
    }

    @Override
    public boolean isReadOnly(Object base, int index)
        throws EvaluationException, PropertyNotFoundException {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getELContext().setPropertyResolved(false);        
        return false;        
    }


    @Override
    public Class getType(Object base, Object property)
        throws EvaluationException, PropertyNotFoundException {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getELContext().setPropertyResolved(false);        
        return null;         
    }

    @Override
    public Class getType(Object base, int index)
        throws EvaluationException, PropertyNotFoundException {
        FacesContext context = FacesContext.getCurrentInstance();
        context.getELContext().setPropertyResolved(false);        
        return null;
    }
    
}
