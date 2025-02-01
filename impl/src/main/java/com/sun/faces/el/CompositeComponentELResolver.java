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

import static com.sun.faces.util.Util.getFeatureDescriptor;
import static java.util.Arrays.asList;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.PropertyNotWritableException;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import com.sun.faces.component.CompositeComponentStackManager;

/**
 * <p>
 * This {@link ELResolver} will handle the resolution of <code>cc</code> when processing a composite component
 * instance.
 * </p>
 */
public class CompositeComponentELResolver extends ELResolver {

    private static final String COMPOSITE_COMPONENT_NAME = "cc";

    @Override
    public Object getValue(ELContext context, Object base, Object property) throws ELException {
        if (base == null && COMPOSITE_COMPONENT_NAME.equals(property)) {
            context.setPropertyResolved(true);
            FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
            CompositeComponentStackManager manager = CompositeComponentStackManager.getManager(facesContext);
            UIComponent currentCompositeComponent = manager.peek();

            if (currentCompositeComponent == null) {
                currentCompositeComponent = UIComponent.getCurrentCompositeComponent(facesContext);
            }

            return currentCompositeComponent;
        }
        
        return null;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object val) throws ELException {
        if (base == null && COMPOSITE_COMPONENT_NAME.equals(property)) {
            throw new PropertyNotWritableException((String) property);
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) throws ELException {
        if (base == null && COMPOSITE_COMPONENT_NAME.equals(property)) {
            context.setPropertyResolved(true);
            return true;
        }

        return false;
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) throws ELException {
        if (base == null && COMPOSITE_COMPONENT_NAME.equals(property)) {
            context.setPropertyResolved(true);
            return UIComponent.class;
        }

        return null;
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        if (base != null) {
            return null;
        }

        return String.class;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
        if (base != null) {
            return null;
        }

        return asList(getFeatureDescriptor("cc", "cc", "cc", false, false, true, UIComponent.class, Boolean.TRUE)).iterator();
    }
}
