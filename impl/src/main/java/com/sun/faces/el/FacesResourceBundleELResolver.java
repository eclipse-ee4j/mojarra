/*
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

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.application.ApplicationResourceBundle;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.Util;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.PropertyNotWritableException;
import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;

/**
 * @author edburns
 */
public class FacesResourceBundleELResolver extends ELResolver {

    /** Creates a new instance of FacesResourceBundleELResolver */
    public FacesResourceBundleELResolver() {
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        if (null != base) {
            return null;
        }
        if (null == property) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "base and property"); // ?????
            throw new PropertyNotFoundException(message);
        }
        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        Application app = facesContext.getApplication();

        ResourceBundle result = app.getResourceBundle(facesContext, property.toString());
        if (null != result) {
            context.setPropertyResolved(true);
        }

        return result;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) throws ELException {

        if (null != base) {
            return null;
        }

        if (null == property) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "base and property"); // ?????
            throw new PropertyNotFoundException(message);
        }

        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        Application app = facesContext.getApplication();

        ResourceBundle result = app.getResourceBundle(facesContext, property.toString());
        if (null != result) {
            context.setPropertyResolved(true);
            return ResourceBundle.class;
        }

        return null;

    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object val) throws ELException {
        String message;

        if (base == null && property == null) {
            message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "base and property"); // ?????
            throw new PropertyNotFoundException(message);
        }

        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        Application app = facesContext.getApplication();

        ResourceBundle result = app.getResourceBundle(facesContext, property.toString());
        if (null != result) {
            context.setPropertyResolved(true);
            message = MessageUtils.getExceptionMessageString(MessageUtils.OBJECT_IS_READONLY);
            message = message + " base " + base + " property " + property;
            throw new PropertyNotWritableException(message);
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
        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        Application app = facesContext.getApplication();

        ResourceBundle result = app.getResourceBundle(facesContext, property.toString());
        if (null != result) {
            context.setPropertyResolved(true);
            return true;
        }

        return false;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {

        if (base != null) {
            return null;
        }

        ArrayList<FeatureDescriptor> list = new ArrayList<>();

        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        ApplicationAssociate associate = ApplicationAssociate.getCurrentInstance();
        Map<String, ApplicationResourceBundle> rbMap = associate.getResourceBundles();
        if (rbMap == null) {
            return list.iterator();
        }
        // iterate over the list of managed beans
        for (Iterator<Map.Entry<String, ApplicationResourceBundle>> i = rbMap.entrySet().iterator(); i.hasNext();) {
            Map.Entry<String, ApplicationResourceBundle> entry = i.next();
            String var = entry.getKey();
            ApplicationResourceBundle bundle = entry.getValue();
            if (bundle != null) {
                Locale curLocale = Util.getLocaleFromContextOrSystem(facesContext);

                String description = bundle.getDescription(curLocale);
                String displayName = bundle.getDisplayName(curLocale);

                list.add(Util.getFeatureDescriptor(var, displayName, description, false, false, true, ResourceBundle.class, Boolean.TRUE));
            }
        }
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
