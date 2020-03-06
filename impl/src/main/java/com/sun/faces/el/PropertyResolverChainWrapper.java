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

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;

import jakarta.faces.context.FacesContext;
import jakarta.faces.el.EvaluationException;
import jakarta.faces.el.PropertyResolver;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.List;

public class PropertyResolverChainWrapper extends ELResolver {

    @SuppressWarnings("deprecation")
    private PropertyResolver legacyPR = null;

    @SuppressWarnings("deprecation")
    public PropertyResolverChainWrapper(PropertyResolver propertyResolver) {
        legacyPR = propertyResolver;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Object getValue(ELContext context, Object base, Object property) throws ELException {
        if (base == null || property == null) {
            return null;
        }
        Object result;
        context.setPropertyResolved(true);
        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        ELContext jsfEL = facesContext.getELContext();
        jsfEL.setPropertyResolved(true);
        if (base instanceof List || base.getClass().isArray()) {
            Object indexObj = facesContext.getApplication().getExpressionFactory().coerceToType(property, Integer.class);
            int index = ((Integer) indexObj).intValue();
            try {
                result = legacyPR.getValue(base, index);
            } catch (EvaluationException ex) {
                context.setPropertyResolved(false);
                throw new ELException(ex);
            }
        } else {
            try {
                result = legacyPR.getValue(base, property);
            } catch (EvaluationException ex) {
                context.setPropertyResolved(false);
                throw new ELException(ex);
            }
        }
        context.setPropertyResolved(jsfEL.isPropertyResolved());
        return result;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Class<?> getType(ELContext context, Object base, Object property) throws ELException {
        if (base == null || property == null) {
            return null;
        }

        Class result;
        context.setPropertyResolved(true);
        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        ELContext jsfEL = facesContext.getELContext();
        jsfEL.setPropertyResolved(true);
        if (base instanceof List || base.getClass().isArray()) {
            Object indexObj = facesContext.getApplication().getExpressionFactory().coerceToType(property, Integer.class);
            int index = ((Integer) indexObj).intValue();
            try {
                result = legacyPR.getType(base, index);
            } catch (EvaluationException ex) {
                context.setPropertyResolved(false);
                throw new ELException(ex);
            }
        } else {
            try {
                result = legacyPR.getType(base, property);
            } catch (EvaluationException ex) {
                context.setPropertyResolved(false);
                throw new ELException(ex);
            }
        }
        context.setPropertyResolved(jsfEL.isPropertyResolved());
        return result;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void setValue(ELContext context, Object base, Object property, Object val) throws ELException {
        if (base == null || property == null) {
            return;
        }

        context.setPropertyResolved(true);
        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        ELContext jsfEL = facesContext.getELContext();
        jsfEL.setPropertyResolved(true);
        if (base instanceof List || base.getClass().isArray()) {
            Object indexObj = facesContext.getApplication().getExpressionFactory().coerceToType(property, Integer.class);
            int index = ((Integer) indexObj).intValue();
            try {
                legacyPR.setValue(base, index, val);
            } catch (EvaluationException ex) {
                context.setPropertyResolved(false);
                throw new ELException(ex);
            }
        } else {
            try {
                legacyPR.setValue(base, property, val);
            } catch (EvaluationException ex) {
                context.setPropertyResolved(false);
                throw new ELException(ex);
            }
        }
        context.setPropertyResolved(jsfEL.isPropertyResolved());
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isReadOnly(ELContext context, Object base, Object property) throws ELException {
        if (base == null || property == null) {
            return false;
        }
        boolean result;
        context.setPropertyResolved(true);
        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        ELContext jsfEL = facesContext.getELContext();
        jsfEL.setPropertyResolved(true);
        if (base instanceof List || base.getClass().isArray()) {
            Object indexObj = facesContext.getApplication().getExpressionFactory().coerceToType(property, Integer.class);
            int index = ((Integer) indexObj).intValue();
            try {
                result = legacyPR.isReadOnly(base, index);
            } catch (EvaluationException ex) {
                context.setPropertyResolved(false);
                throw new ELException(ex);
            }
        } else {
            try {
                result = legacyPR.isReadOnly(base, property);
            } catch (EvaluationException ex) {
                context.setPropertyResolved(false);
                throw new ELException(ex);
            }
        }
        context.setPropertyResolved(jsfEL.isPropertyResolved());
        return result;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return null;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        if (base == null) {
            return Object.class;
        }
        return null;
    }

}
