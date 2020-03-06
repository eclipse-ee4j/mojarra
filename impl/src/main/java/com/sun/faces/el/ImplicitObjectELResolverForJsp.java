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

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Iterator;

import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.Util;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.PropertyNotWritableException;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.FacesContext;

public class ImplicitObjectELResolverForJsp extends ImplicitObjectELResolver {

    public ImplicitObjectELResolverForJsp() {
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) throws ELException {
        // variable resolution is a special case of property resolution
        // where the base is null.
        if (base != null) {
            return null;
        }
        if (property == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "property");
            throw new PropertyNotFoundException(message);
        }

        Integer index = IMPLICIT_OBJECTS.get(property.toString());

        if (index == null) {
            return null;
        }

        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);

        switch (index.intValue()) {
        case FACES_CONTEXT:
            context.setPropertyResolved(true);
            return facesContext;
        case FLASH:
            context.setPropertyResolved(true);
            return facesContext.getExternalContext().getFlash();
        case VIEW:
            context.setPropertyResolved(true);
            return facesContext.getViewRoot();
        case VIEW_SCOPE:
            context.setPropertyResolved(true);
            return facesContext.getViewRoot().getViewMap();
        case RESOURCE:
            context.setPropertyResolved(true);
            return facesContext.getApplication().getResourceHandler();
        default:
            return null;
        }

    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) throws ELException {
        if (base != null) {
            return null;
        }
        if (property == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "property");
            throw new PropertyNotFoundException(message);
        }

        Integer index = IMPLICIT_OBJECTS.get(property.toString());

        if (index == null) {
            return null;
        }
        switch (index) {
        case FACES_CONTEXT:
        case VIEW:
            context.setPropertyResolved(true);
            return null;
        default:
            return null;
        }
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object val) throws ELException {
        if (base != null) {
            return;
        }
        if (property == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "property");
            throw new PropertyNotFoundException(message);
        }

        Integer index = IMPLICIT_OBJECTS.get(property.toString());
        if (index == null) {
            return;
        }
        switch (index) {
        case FACES_CONTEXT:
            throw new PropertyNotWritableException(MessageUtils.getExceptionMessageString(MessageUtils.OBJECT_IS_READONLY, "facesContext"));
        case VIEW:
            throw new PropertyNotWritableException(MessageUtils.getExceptionMessageString(MessageUtils.OBJECT_IS_READONLY, "view"));
        default:
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) throws ELException {
        if (base != null) {
            return false;
        }
        if (property == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "property");
            throw new PropertyNotFoundException(message);
        }
        // return value will be ignored unless context.propertyResolved is
        // set to true.
        Integer index = IMPLICIT_OBJECTS.get(property.toString());
        if (index == null) {
            return false;
        }
        switch (index) {
        case FACES_CONTEXT:
            context.setPropertyResolved(true);
            return true;
        case VIEW:
            context.setPropertyResolved(true);
            return true;
        default:
            return false;
        }
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        if (base != null) {
            return null;
        }

        ArrayList<FeatureDescriptor> list = new ArrayList<>(2);
        list.add(Util.getFeatureDescriptor("facesContext", "facesContext", "facesContext", false, false, true, FacesContext.class, Boolean.TRUE));
        list.add(Util.getFeatureDescriptor("view", "view", "root", false, false, true, UIViewRoot.class, Boolean.TRUE));
        return list.iterator();

    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        if (base != null) {
            return null;
        }
        return String.class;
    }

}
