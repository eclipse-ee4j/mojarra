/*
 * Copyright (c) 2023 Contributors to Eclipse Foundation.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

import static com.sun.faces.util.MessageUtils.getExceptionMessageString;

import com.sun.faces.util.MessageUtils;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.PropertyNotFoundException;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import java.util.Map;

public class ScopedAttributeELResolver extends ELResolver {

    public ScopedAttributeELResolver() {
    }

    @Override
    public Object getValue(ELContext elContext, Object base, Object property) throws ELException {
        if (base != null) {
            return null;
        }

        if (property == null) {
            String message = getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "base and property"); // ?????
            throw new PropertyNotFoundException(message);
        }

        elContext.setPropertyResolved(true);
        String attribute = property.toString();
        FacesContext facesContext = (FacesContext) elContext.getContext(FacesContext.class);
        ExternalContext externalContext = facesContext.getExternalContext();

        // check request
        Object result = externalContext.getRequestMap().get(attribute);
        if (result != null) {
            return result;
        }

        // check UIViewRoot
        UIViewRoot root = facesContext.getViewRoot();
        if (root != null) {
            Map<String, Object> viewMap = root.getViewMap(false);
            if (viewMap != null) {
                result = viewMap.get(attribute);
            }
        }
        if (result != null) {
            return result;
        }

        // check session
        result = externalContext.getSessionMap().get(attribute);
        if (result != null) {
            return result;
        }

        // check application
        result = externalContext.getApplicationMap().get(attribute);
        if (result != null) {
            return result;
        }

        return null;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) throws ELException {
        if (base != null) {
            return null;
        }

        if (property == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "base and property"); // ?????
            throw new PropertyNotFoundException(message);
        }

        context.setPropertyResolved(true);
        return Object.class;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object val) throws ELException {
        if (base != null) {
            return;
        }
        if (property == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "base and property"); // ?????
            throw new PropertyNotFoundException(message);
        }

        context.setPropertyResolved(true);

        String attribute = (String) property;
        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        ExternalContext externalContext = facesContext.getExternalContext();
        if (externalContext.getRequestMap().get(attribute) != null) {
            externalContext.getRequestMap().put(attribute, val);
        } else if (facesContext.getViewRoot() != null && facesContext.getViewRoot().getViewMap().get(attribute) != null) {
            facesContext.getViewRoot().getViewMap().put(attribute, val);
        } else if (externalContext.getSessionMap().get(attribute) != null) {
            externalContext.getSessionMap().put(attribute, val);
        } else if (externalContext.getApplicationMap().get(attribute) != null) {
            externalContext.getApplicationMap().put(attribute, val);
        } else {
            // if the property doesn't exist in any of the scopes, put it in
            // request scope.
            externalContext.getRequestMap().put(attribute, val);
        }

    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) throws ELException {
        if (base != null) {
            return false;
        }

        if (property == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "base and property"); // ?????
            throw new PropertyNotFoundException(message);
        }

        context.setPropertyResolved(true);
        return false;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        if (base != null) {
            return null;
        }

        return Object.class;
    }

}
