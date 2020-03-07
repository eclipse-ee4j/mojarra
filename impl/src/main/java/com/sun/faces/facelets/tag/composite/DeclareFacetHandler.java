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
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sun.faces.facelets.tag.TagHandlerImpl;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.view.facelets.ComponentHandler;
import jakarta.faces.view.facelets.FaceletContext;
import jakarta.faces.view.facelets.TagAttribute;
import jakarta.faces.view.facelets.TagConfig;
import jakarta.faces.view.facelets.TagException;

public class DeclareFacetHandler extends TagHandlerImpl {

    private static final String[] ATTRIBUTES = { "required", "displayName", "expert", "hidden", "preferred", "shortDescription", "default" };

    private static final PropertyHandlerManager ATTRIBUTE_MANAGER = PropertyHandlerManager.getInstance(ATTRIBUTES);

    private TagAttribute name = null;

    public DeclareFacetHandler(TagConfig config) {
        super(config);
        name = getRequiredAttribute("name");

    }

    @SuppressWarnings({ "unchecked" })
    @Override
    public void apply(FaceletContext ctx, UIComponent parent) throws IOException {
        // only process if it's been created
        if (null == parent || null == (parent = parent.getParent()) || !ComponentHandler.isNew(parent)) {
            return;
        }

        Map<String, Object> componentAttrs = parent.getAttributes();

        CompositeComponentBeanInfo componentBeanInfo = (CompositeComponentBeanInfo) componentAttrs.get(UIComponent.BEANINFO_KEY);

        // Get the value of required the name propertyDescriptor
        ValueExpression ve = name.getValueExpression(ctx, String.class);
        String strValue = (String) ve.getValue(ctx);
        BeanDescriptor componentBeanDescriptor = componentBeanInfo.getBeanDescriptor();

        Map<String, PropertyDescriptor> facetDescriptors = (Map<String, PropertyDescriptor>) componentBeanDescriptor.getValue(UIComponent.FACETS_KEY);

        if (facetDescriptors == null) {
            facetDescriptors = new HashMap<>();
            componentBeanDescriptor.setValue(UIComponent.FACETS_KEY, facetDescriptors);
        }

        PropertyDescriptor propertyDescriptor;
        try {
            propertyDescriptor = new PropertyDescriptor(strValue, null, null);
        } catch (IntrospectionException ex) {
            throw new TagException(tag, "Unable to create property descriptor for facet" + strValue, ex);
        }
        facetDescriptors.put(strValue, propertyDescriptor);

        for (TagAttribute tagAttribute : tag.getAttributes().getAll()) {
            String attributeName = tagAttribute.getLocalName();
            PropertyHandler handler = ATTRIBUTE_MANAGER.getHandler(ctx, attributeName);
            if (handler != null) {
                handler.apply(ctx, attributeName, propertyDescriptor, tagAttribute);
            }

        }

        nextHandler.apply(ctx, parent);

    }

}
