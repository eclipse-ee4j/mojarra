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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.sun.faces.component.CompositeComponentStackManager;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.Util;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.PropertyNotFoundException;
import jakarta.el.PropertyNotWritableException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.FlowHandler;

public class ImplicitObjectELResolver extends ELResolver implements ELConstants {

    protected static final Map<String, Integer> IMPLICIT_OBJECTS;

    static {
        String[] implictNames = new String[] { "application", "applicationScope", "cc", "component", "cookie", "facesContext", "flash", "flowScope", "header",
                "headerValues", "initParam", "param", "paramValues", "request", "requestScope", "resource", "session", "sessionScope", "view", "viewScope" };
        int nameCount = implictNames.length;

        Map<String, Integer> implicitObjects = new HashMap<>((int) (nameCount * 1.5f));

        for (int nameIndex = 0; nameIndex < nameCount; nameIndex++) {
            implicitObjects.put(implictNames[nameIndex], nameIndex);
        }

        IMPLICIT_OBJECTS = implicitObjects;
    }

    public ImplicitObjectELResolver() {
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
        } else {
            FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
            ExternalContext extCtx = facesContext.getExternalContext();
            switch (index) {
            case APPLICATION:
                context.setPropertyResolved(true);
                return extCtx.getContext();
            case APPLICATION_SCOPE:
                context.setPropertyResolved(true);
                return extCtx.getApplicationMap();
            case COMPOSITE_COMPONENT:
                context.setPropertyResolved(true);
                // The following five lines violate the specification.
                // The specification states that the 'cc' implicit object
                // always evaluates to the current composite component,
                // however, this isn't desirable behavior when passing
                // attributes between nested composite components, so we
                // need to alter the behavior so that the components behave
                // as the user would expect.
                /* BEGIN DEVIATION */
                CompositeComponentStackManager manager = CompositeComponentStackManager.getManager(facesContext);
                Object o = manager.peek();
                /* END DEVIATION */
                if (o == null) {
                    o = UIComponent.getCurrentCompositeComponent(facesContext);
                }
                return o;
            case COMPONENT:
                UIComponent c = UIComponent.getCurrentComponent(facesContext);
                context.setPropertyResolved(c != null);
                return c;
            case COOKIE:
                context.setPropertyResolved(true);
                return extCtx.getRequestCookieMap();
            case FACES_CONTEXT:
                context.setPropertyResolved(true);
                return facesContext;
            case FLASH:
                context.setPropertyResolved(true);
                return facesContext.getExternalContext().getFlash();
            case FACES_FLOW:
                context.setPropertyResolved(true);
                FlowHandler flowHandler = facesContext.getApplication().getFlowHandler();
                Map<Object, Object> flowScope = null;
                if (null != flowHandler) {
                    flowScope = flowHandler.getCurrentFlowScope();
                }
                return flowScope;
            case HEADER:
                context.setPropertyResolved(true);
                return extCtx.getRequestHeaderMap();
            case HEADER_VALUES:
                context.setPropertyResolved(true);
                return extCtx.getRequestHeaderValuesMap();
            case INIT_PARAM:
                context.setPropertyResolved(true);
                return extCtx.getInitParameterMap();
            case PARAM:
                context.setPropertyResolved(true);
                return extCtx.getRequestParameterMap();
            case PARAM_VALUES:
                context.setPropertyResolved(true);
                return extCtx.getRequestParameterValuesMap();
            case REQUEST:
                context.setPropertyResolved(true);
                return extCtx.getRequest();
            case REQUEST_SCOPE:
                context.setPropertyResolved(true);
                return extCtx.getRequestMap();
            case RESOURCE:
                context.setPropertyResolved(true);
                return facesContext.getApplication().getResourceHandler();
            case SESSION:
                context.setPropertyResolved(true);
                return extCtx.getSession(true);
            case SESSION_SCOPE:
                context.setPropertyResolved(true);
                return extCtx.getSessionMap();
            case VIEW:
                context.setPropertyResolved(true);
                return facesContext.getViewRoot();
            case VIEW_SCOPE:
                UIViewRoot root = facesContext.getViewRoot();
                if (root != null) {
                    context.setPropertyResolved(true);
                    return facesContext.getViewRoot().getViewMap();
                }
            default:
                return null;
            }
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
        if (index != null) {
            throw new PropertyNotWritableException((String) property);
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

        Integer index = IMPLICIT_OBJECTS.get(property.toString());

        if (index != null) {
            context.setPropertyResolved(true);
            return true;
        }
        return false;
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

        if (index != null) {
            context.setPropertyResolved(true);
        }
        return null;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        if (base != null) {
            return null;
        }
        ArrayList<FeatureDescriptor> list = new ArrayList<>(14);
        list.add(Util.getFeatureDescriptor("application", "application", "application", false, false, true, Object.class, Boolean.TRUE));
        list.add(Util.getFeatureDescriptor("applicationScope", "applicationScope", "applicationScope", false, false, true, Map.class, Boolean.TRUE));
        list.add(Util.getFeatureDescriptor("cc", "cc", "cc", false, false, true, UIComponent.class, Boolean.TRUE));
        list.add(Util.getFeatureDescriptor("component", "component", "component", false, false, true, UIComponent.class, Boolean.TRUE));
        list.add(Util.getFeatureDescriptor("cookie", "cookie", "cookie", false, false, true, Map.class, Boolean.TRUE));
        list.add(Util.getFeatureDescriptor("facesContext", "facesContext", "facesContext", false, false, true, FacesContext.class, Boolean.TRUE));
        list.add(Util.getFeatureDescriptor("flash", "flash", "flash", false, false, true, Map.class, Boolean.TRUE));
        list.add(Util.getFeatureDescriptor("flowScope", "flowScope", "flowScope", false, false, true, Map.class, Boolean.TRUE));
        list.add(Util.getFeatureDescriptor("view", "view", "root", false, false, true, UIViewRoot.class, Boolean.TRUE));
        list.add(Util.getFeatureDescriptor("header", "header", "header", false, false, true, Map.class, Boolean.TRUE));
        list.add(Util.getFeatureDescriptor("headerValues", "headerValues", "headerValues", false, false, true, Map.class, Boolean.TRUE));
        list.add(Util.getFeatureDescriptor("initParam", "initParam", "initParam", false, false, true, Map.class, Boolean.TRUE));
        list.add(Util.getFeatureDescriptor("param", "param", "param", false, false, true, Map.class, Boolean.TRUE));
        list.add(Util.getFeatureDescriptor("paramValues", "paramValues", "paramValues", false, false, true, Map.class, Boolean.TRUE));
        list.add(Util.getFeatureDescriptor("request", "request", "request", false, false, true, Object.class, Boolean.TRUE));
        list.add(Util.getFeatureDescriptor("requestScope", "requestScope", "requestScope", false, false, true, Map.class, Boolean.TRUE));
        list.add(Util.getFeatureDescriptor("resource", "resource", "resource", false, false, true, Object.class, Boolean.TRUE));
        list.add(Util.getFeatureDescriptor("session", "session", "session", false, false, true, Object.class, Boolean.TRUE));
        list.add(Util.getFeatureDescriptor("sessionScope", "sessionScope", "sessionScope", false, false, true, Map.class, Boolean.TRUE));
        list.add(Util.getFeatureDescriptor("viewScope", "viewScope", "viewScope", false, false, true, Map.class, Boolean.TRUE));

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
