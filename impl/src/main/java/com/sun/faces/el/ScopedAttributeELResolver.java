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

import java.util.ArrayList;
import java.util.Iterator;
import java.beans.FeatureDescriptor;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Map;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.component.UIViewRoot;

import javax.el.ELException;
import javax.el.PropertyNotFoundException;
import javax.el.ELContext;
import javax.el.ELResolver;

import com.sun.faces.util.Util;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.application.ApplicationAssociate;

public class ScopedAttributeELResolver extends ELResolver {

    public ScopedAttributeELResolver() {
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property)
        throws ELException {
        if (base != null) {
            return null;
        }
        if ( property == null) {
            String message = MessageUtils.getExceptionMessageString
                (MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "base and property"); // ?????
            throw new PropertyNotFoundException(message);
        }
        context.setPropertyResolved(true);
        String attribute = property.toString();
        FacesContext facesContext = (FacesContext)
            context.getContext(FacesContext.class);
        ExternalContext ec = facesContext.getExternalContext();

        // check request
        Object result = ec.getRequestMap().get(attribute);
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
        result = ec.getSessionMap().get(attribute);
        if (result != null) {
            return result;
        }

        // check application
        result = ec.getApplicationMap().get(attribute);
        if (result != null) {
            return result;
        }

        return null;
        
    }


    @Override
    public Class<?> getType(ELContext context, Object base, Object property)
        throws ELException {
        if (base != null) {
            return null;
        }
        if ( property == null) {
            String message = MessageUtils.getExceptionMessageString
                (MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "base and property"); // ?????
            throw new PropertyNotFoundException(message);
        }

        context.setPropertyResolved(true);
        return Object.class;
    }

    @Override
    public void  setValue(ELContext context, Object base, Object property,
                          Object val) throws ELException {
        if (base != null) {
            return;
        }
        if (property == null) {
            String message = MessageUtils.getExceptionMessageString
                (MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "base and property"); // ?????
            throw new PropertyNotFoundException(message);
        }

        context.setPropertyResolved(true);

        String attribute = (String) property;
        FacesContext facesContext = (FacesContext)
            context.getContext(FacesContext.class);
        ExternalContext ec = facesContext.getExternalContext();
        if ((ec.getRequestMap().get(attribute)) != null) {
            ec.getRequestMap().put(attribute, val);
        } else if ((facesContext.getViewRoot()) != null && (facesContext.getViewRoot().getViewMap().get(attribute)) != null) {
            facesContext.getViewRoot().getViewMap().put(attribute, val);
        } else if ((ec.getSessionMap().get(attribute)) != null) {
            ec.getSessionMap().put(attribute, val);
        } else if ((ec.getApplicationMap().get(attribute)) != null) {
            ec.getApplicationMap().put(attribute, val);
        } else {
            // if the property doesn't exist in any of the scopes, put it in
            // request scope.
            ec.getRequestMap().put(attribute, val);
        }

    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property)
        throws ELException {
        if (base != null) {
            return false;
        }
        if (property == null) {
            String message = MessageUtils.getExceptionMessageString
                (MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "base and property"); // ?????
            throw new PropertyNotFoundException(message);
        }
        context.setPropertyResolved(true);
        return false;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {

       ArrayList<FeatureDescriptor> list = new ArrayList<>();

       FacesContext facesContext = (FacesContext)
           context.getContext(FacesContext.class);
       ExternalContext ec = facesContext.getExternalContext();

       // add attributes in request scope.
       Set<Entry<String,Object>> attrs = ec.getRequestMap().entrySet();
        for (Entry<String, Object> entry : attrs) {
            String attrName = entry.getKey();
            Object attrValue = entry.getValue();
            list.add(Util.getFeatureDescriptor(attrName, attrName,
                                               "request scope attribute", false, false, true, attrValue.getClass(),
                                               Boolean.TRUE));
        }
        
       // add attributes in view scope.
        UIViewRoot root = facesContext.getViewRoot();
        if (root != null) {
            Map<String, Object> viewMap = root.getViewMap(false);
            if (viewMap != null && viewMap.size() != 0) {
                attrs = viewMap.entrySet();
                for (Entry<String, Object> entry : attrs) {
                    String attrName = entry.getKey();
                    Object attrValue = entry.getValue();
                    list.add(Util.getFeatureDescriptor(attrName, attrName,
                                                       "view scope attribute", false, false, true, attrValue.getClass(),
                                                       Boolean.TRUE));
                }
            }
        }
        

       // add attributes in session scope.
       attrs = ec.getSessionMap().entrySet();
        for (Entry<String, Object> entry : attrs) {
            String attrName = entry.getKey();
            Object attrValue = entry.getValue();
            list.add(Util.getFeatureDescriptor(attrName, attrName,
                                               "session scope attribute", false, false, true, attrValue.getClass(),
                                               Boolean.TRUE));
        }

       // add attributes in application scope.
       attrs = ec.getApplicationMap().entrySet();
       for (Entry<String, Object> entry : attrs) {
            String attrName = entry.getKey();
            Object attrValue = entry.getValue();
            list.add(Util.getFeatureDescriptor(attrName, attrName,
                                               "application scope attribute", false, false, true, attrValue.getClass(),
                                               Boolean.TRUE));
        }

       return list.iterator();
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        if (base != null) {
            return null;
        }
        return Object.class;
    }

}
