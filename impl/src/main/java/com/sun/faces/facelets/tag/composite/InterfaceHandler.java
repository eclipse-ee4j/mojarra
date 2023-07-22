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

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.faces.application.view.FaceletViewHandlingStrategy;
import com.sun.faces.facelets.tag.TagHandlerImpl;
import com.sun.faces.facelets.tag.faces.ComponentSupport;
import com.sun.faces.util.MessageUtils;

import jakarta.el.ValueExpression;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.application.Resource;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.AttachedObjectTarget;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.Tag;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;

public class InterfaceHandler extends TagHandlerImpl {

    private static final String[] ATTRIBUTES_DEV = { "displayName", "expert", "hidden", "preferred", "shortDescription", "name", "componentType" };

    private static final PropertyHandlerManager INTERFACE_HANDLERS = PropertyHandlerManager.getInstance(ATTRIBUTES_DEV);

    public final static String Name = "interface";

    public InterfaceHandler(TagConfig config) {
        super(config);
    }

    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        FacesContext context = ctx.getFacesContext();
        // only process if it's been created
        // Do not process if we're simply building metadata
        if (FaceletViewHandlingStrategy.isBuildingMetadata(context)) {
            imbueComponentWithMetadata(ctx, parent);
            nextHandler.apply(ctx, parent);
        } else {
            if (ProjectStage.Development == context.getApplication().getProjectStage()) {
                validateComponent(context, parent);
            }
        }
    }

    private void validateComponent(FacesContext context, UIComponent ccParent) throws TagException {
        UIComponent cc = ccParent.getParent();
        if (null == cc) {
            String clientId = ccParent.getClientId(context);

            throw new TagException(tag, MessageUtils.getExceptionMessageString(MessageUtils.COMPONENT_NOT_FOUND_ERROR_MESSAGE_ID, clientId + ".getParent()"));
        }
        Tag usingPageTag = ComponentSupport.getTagForComponent(context, cc);
        Map<String, Object> attrs = cc.getAttributes();
        BeanInfo componentMetadata = (BeanInfo) attrs.get(UIComponent.BEANINFO_KEY);

        if (null == componentMetadata) {
            String clientId = ccParent.getClientId(context);

            throw new TagException(usingPageTag, MessageUtils.getExceptionMessageString(MessageUtils.MISSING_COMPONENT_METADATA, clientId));
        }

        PropertyDescriptor[] declaredAttributes = componentMetadata.getPropertyDescriptors();
        String key;
        Object requiredValue;
        boolean found = false, required = false;
        StringBuilder buf = null;
        String attrMessage = "", facetMessage = "";

        // Traverse the attributes of this component
        for (PropertyDescriptor cur : declaredAttributes) {
            required = false;
            requiredValue = cur.getValue("required");
            if (null != requiredValue) {
                if (requiredValue instanceof ValueExpression) {
                    requiredValue = ((ValueExpression) requiredValue).getValue(context.getELContext());
                    required = Boolean.parseBoolean(requiredValue.toString());
                }
            }
            if (required) {
                key = cur.getName();
                found = false;
                // Is the attribute a method expression?
                if (null != cur.getValue("method-signature") && null == cur.getValue("type")) {
                    // Yes, look for it as an EL expression.
                    found = null != cc.getValueExpression(key);
                } else {
                    // No, look for it as an actual attribute
                    found = attrs.containsKey(key);
                    // Special case: nested composite components
                    if (!found) {
                        // Check if an EL expression was given.
                        found = null != cc.getValueExpression(key);
                    }
                }
                if (!found) {
                    if (null == buf) {
                        buf = new StringBuilder();
                        buf.append(key);
                    } else {
                        buf.append(", ").append(key);
                    }
                }
            }
        }
        if (null != buf) {
            attrMessage = MessageUtils.getExceptionMessageString(MessageUtils.MISSING_COMPONENT_ATTRIBUTE_VALUE, buf.toString());
        }

        buf = null;

        // Traverse the declared facets
        Map<String, PropertyDescriptor> declaredFacets = (Map<String, PropertyDescriptor>) componentMetadata.getBeanDescriptor()
                .getValue(UIComponent.FACETS_KEY);
        if (null != declaredFacets) {
            for (PropertyDescriptor cur : declaredFacets.values()) {
                required = false;
                requiredValue = cur.getValue("required");
                if (null != requiredValue) {
                    if (requiredValue instanceof ValueExpression) {
                        requiredValue = ((ValueExpression) requiredValue).getValue(context.getELContext());
                        required = Boolean.parseBoolean(requiredValue.toString());
                    }
                }
                if (required) {
                    key = cur.getName();
                    if (!cc.getFacets().containsKey(key)) {
                        if (null == buf) {
                            buf = new StringBuilder();
                            buf.append(key);
                        } else {
                            buf.append(", ").append(key);
                        }
                    }
                }
            }
        }
        if (null != buf) {
            facetMessage = MessageUtils.getExceptionMessageString(MessageUtils.MISSING_COMPONENT_FACET, buf.toString());
        }

        if (0 < attrMessage.length() || 0 < facetMessage.length()) {
            throw new TagException(usingPageTag, attrMessage + " " + facetMessage);
        }
    }

    @SuppressWarnings({ "unchecked" })
    private void imbueComponentWithMetadata(FaceletContext ctx, UIComponent parent) {
        // only process if it's been created
        if (null == parent || null == (parent = parent.getParent()) || !ComponentHandler.isNew(parent)) {
            return;
        }

        Map<String, Object> attrs = parent.getAttributes();

        CompositeComponentBeanInfo componentBeanInfo = (CompositeComponentBeanInfo) attrs.get(UIComponent.BEANINFO_KEY);

        if (componentBeanInfo == null) {

            componentBeanInfo = new CompositeComponentBeanInfo();
            attrs.put(UIComponent.BEANINFO_KEY, componentBeanInfo);
            BeanDescriptor componentDescriptor = new BeanDescriptor(parent.getClass());
            // PENDING(edburns): Make sure attributeNames() returns the right content
            // per the javadocs for ViewDeclarationLanguage.getComponentMetadata()
            componentBeanInfo.setBeanDescriptor(componentDescriptor);

            for (TagAttribute tagAttribute : tag.getAttributes().getAll()) {
                String attributeName = tagAttribute.getLocalName();
                PropertyHandler handler = INTERFACE_HANDLERS.getHandler(ctx, attributeName);
                if (handler != null) {
                    handler.apply(ctx, attributeName, componentDescriptor, tagAttribute);
                }

            }

            List<AttachedObjectTarget> targetList = (List<AttachedObjectTarget>) componentDescriptor.getValue(AttachedObjectTarget.ATTACHED_OBJECT_TARGETS_KEY);
            if (null == targetList) {
                targetList = new ArrayList<>();
                componentDescriptor.setValue(AttachedObjectTarget.ATTACHED_OBJECT_TARGETS_KEY, targetList);
            }

            Resource componentResource = (Resource) attrs.get(Resource.COMPONENT_RESOURCE_KEY);
            if (null == componentResource) {
                throw new NullPointerException("Unable to find Resource for composite component");
            }
        }
    }

}
