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
import jakarta.el.PropertyNotFoundException;

import java.beans.FeatureDescriptor;
import java.util.*;

import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.RequestStateManager;

import jakarta.faces.context.FacesContext;
import jakarta.faces.el.EvaluationException;
import jakarta.faces.el.VariableResolver;

public class VariableResolverChainWrapper extends ELResolver {

    @SuppressWarnings("deprecation")
    private VariableResolver legacyVR = null;

    @SuppressWarnings("deprecation")
    public VariableResolverChainWrapper(VariableResolver variableResolver) {
        this.legacyVR = variableResolver;
    }

    public void setWrapped(VariableResolver newVR) {
        this.legacyVR = newVR;
    }

    @Override
    @SuppressWarnings("deprecation")
    public Object getValue(ELContext context, Object base, Object property) throws ELException {

        // Don't call into the chain unless it's been decorated.
        if (legacyVR instanceof ChainAwareVariableResolver) {
            return null;
        }

        if (base != null) {
            return null;
        }
        if (base == null && property == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "base and property"); // ?????
            throw new PropertyNotFoundException(message);
        }
        context.setPropertyResolved(true);
        Object result = null;

        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        String propString = property.toString();
        Map<String, Object> stateMap = RequestStateManager.getStateMap(facesContext);
        try {
            // If we are already in the midst of an expression evaluation
            // that touched this resolver...
            // noinspection unchecked
            List<String> varNames = (List<String>) stateMap.get(RequestStateManager.REENTRANT_GUARD);
            if (varNames != null && !varNames.isEmpty() && varNames.contains(propString)) {
                // take no action and return.
                context.setPropertyResolved(false);
                return null;
            }
            // Make sure subsequent calls don't take action.
            if (varNames == null) {
                varNames = new ArrayList<>();
                stateMap.put(RequestStateManager.REENTRANT_GUARD, varNames);
            }
            varNames.add(propString);

            result = legacyVR.resolveVariable(facesContext, propString);
        } catch (EvaluationException ex) {
            context.setPropertyResolved(false);
            throw new ELException(ex);
        } finally {
            // Make sure to remove the guard after the call returns
            // noinspection unchecked
            List<String> varNames = (List<String>) stateMap.get(RequestStateManager.REENTRANT_GUARD);
            if (varNames != null && !varNames.isEmpty()) {
                varNames.remove(propString);
            }
            // Make sure that the ELContext "resolved" indicator is set
            // in accordance wth the result of the resolution.
            context.setPropertyResolved(result != null);
        }
        return result;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) throws ELException {

        // Don't call into the chain unless it's been decorated.
        if (legacyVR instanceof ChainAwareVariableResolver) {
            return null;
        }

        Object result = getValue(context, base, property);
        context.setPropertyResolved(result != null);
        if (result != null) {
            return result.getClass();
        }
        return null;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object val) throws ELException {
        // Don't call into the chain unless it's been decorated.
        if (legacyVR instanceof ChainAwareVariableResolver) {
            return;
        }

        if (null == base && null == property) {
            throw new PropertyNotFoundException();
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) throws ELException {

        // Don't call into the chain unless it's been decorated.
        if (legacyVR instanceof ChainAwareVariableResolver) {
            return false;
        }

        if (null == base && null == property) {
            throw new PropertyNotFoundException();
        }
        return false;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        return null;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {

        // Don't call into the chain unless it's been decorated.
        if (legacyVR instanceof ChainAwareVariableResolver) {
            return null;
        }

        if (base == null) {
            return String.class;
        }
        return null;
    }

}
