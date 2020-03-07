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

package com.sun.faces.facelets.tag.composite;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.facelets.tag.TagHandlerImpl;
import com.sun.faces.facelets.util.ReflectionUtil;
import com.sun.faces.util.FacesLogger;

import jakarta.el.ELContext;
import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagAttributeException;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;

public class AttributeHandler extends TagHandlerImpl {

    private final Logger LOGGER = FacesLogger.TAGLIB.getLogger();

    private static final String[] COMPOSITE_ATTRIBUTE_ATTRIBUTES = { "required", "targets", "targetAttributeName", "default", "displayName", "preferred",
            "hidden", "expert", "shortDescription", "method-signature", "type",

    };

    private static final PropertyHandlerManager ATTRIBUTE_MANAGER = PropertyHandlerManager.getInstance(COMPOSITE_ATTRIBUTE_ATTRIBUTES);

    private TagAttribute name;

    public AttributeHandler(TagConfig config) {
        super(config);
        name = getRequiredAttribute("name");
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        // only process if it's been created
        if (null == parent || null == (parent = parent.getParent()) || !ComponentHandler.isNew(parent)) {
            return;
        }

        Map<String, Object> attrs = parent.getAttributes();

        CompositeComponentBeanInfo componentBeanInfo = (CompositeComponentBeanInfo) attrs.get(UIComponent.BEANINFO_KEY);
        assert null != componentBeanInfo;
        List<PropertyDescriptor> declaredAttributes = componentBeanInfo.getPropertyDescriptorsList();

        // Get the value of required the name propertyDescriptor
        ValueExpression ve = name.getValueExpression(ctx, String.class);
        String strValue = (String) ve.getValue(ctx);

        // Search the propertyDescriptors for one for this attribute
        for (PropertyDescriptor cur : declaredAttributes) {
            if (strValue.endsWith(cur.getName())) {
                // If we have a match, no need to waste time
                // duplicating and replacing it.
                return;
            }
        }

        PropertyDescriptor propertyDescriptor;
        try {
            propertyDescriptor = new CCAttributePropertyDescriptor(strValue, null, null);
            declaredAttributes.add(propertyDescriptor);
        } catch (IntrospectionException ex) {
            throw new TagException(tag, "Unable to create property descriptor for property " + strValue, ex);
        }

        TagAttribute defaultTagAttribute = null;
        PropertyHandler defaultHandler = null;
        for (TagAttribute tagAttribute : tag.getAttributes().getAll()) {
            String attributeName = tagAttribute.getLocalName();
            if ("default".equals(attributeName)) {
                // store the TagAttribute and the PropertyHandler for later
                // execution, as the handler for the default-attribute requires,
                // that the PropertyHandler for 'type' - if it exists - has been
                // applied first.
                defaultTagAttribute = tagAttribute;
                defaultHandler = ATTRIBUTE_MANAGER.getHandler(ctx, "default");
            } else {
                PropertyHandler handler = ATTRIBUTE_MANAGER.getHandler(ctx, attributeName);
                if (handler != null) {
                    handler.apply(ctx, attributeName, propertyDescriptor, tagAttribute);
                }
            }
        }
        if (defaultHandler != null) {
            // If the 'default'-attribute of cc:attribute was set, apply the
            // previously stored PropertyHandler (see above) now, as now it is
            // guaranteed that if a 'type'-attribute existed, that its handler
            // was already applied
            try {
                defaultHandler.apply(ctx, "default", propertyDescriptor, defaultTagAttribute);
            } catch (IllegalArgumentException ex) {
                // If the type (according to the type-attribute) can not be
                // found, the DefaultPropertyHandler will wrapp the
                // ClassNotFoundException into an IllegalArgumentException,
                // which is unwrapped into a TagException here.
                throw new TagException(tag, "'type' could not be resolved: " + ex.getCause(), ex.getCause());
            }
        }

        nextHandler.apply(ctx, parent);

    }

    private class CCAttributePropertyDescriptor extends PropertyDescriptor {

        public CCAttributePropertyDescriptor(String propertyName, Method readMethod, Method writeMethod) throws IntrospectionException {
            super(propertyName, readMethod, writeMethod);
        }

        @Override
        public Object getValue(String attributeName) {
            Object result = super.getValue(attributeName);
            if ("type".equals(attributeName)) {
                if (null != result && !(result instanceof Class)) {
                    FacesContext context = FacesContext.getCurrentInstance();
                    ELContext elContext = context.getELContext();
                    String classStr = (String) ((ValueExpression) result).getValue(elContext);
                    if (null != classStr) {
                        try {
                            result = ReflectionUtil.forName(classStr);

                            setValue(attributeName, result);
                        } catch (ClassNotFoundException ex) {
                            classStr = "java.lang." + classStr;
                            boolean throwException = false;
                            try {
                                result = ReflectionUtil.forName(classStr);

                                setValue(attributeName, result);
                            } catch (ClassNotFoundException ex2) {
                                throwException = true;
                            }
                            if (throwException) {
                                String message = "Unable to obtain class for " + classStr;
                                if (LOGGER.isLoggable(Level.INFO)) {
                                    LOGGER.log(Level.INFO, message, ex);
                                }
                                throw new TagAttributeException(tag, name, message, ex);
                            }
                        }
                    }

                }
            }
            return result;
        }

    }

}
