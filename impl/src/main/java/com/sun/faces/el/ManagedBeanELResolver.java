/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.faces.application.ApplicationAssociate;
import com.sun.faces.mgbean.BeanBuilder;
import com.sun.faces.mgbean.BeanManager;
import com.sun.faces.util.MessageUtils;
import com.sun.faces.util.Util;

import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.PropertyNotFoundException;

import java.beans.FeatureDescriptor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ManagedBeanELResolver extends ELResolver {

    public ManagedBeanELResolver() {
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) throws ELException {
        if (base != null) {
            return null;
        }
        if (property == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "property");
            throw new PropertyNotFoundException(message);
        }

        return resolveBean(context, property, true);

    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) throws ELException {

        if (base == null && property == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "property");
            throw new PropertyNotFoundException(message);
        }

        return null;

    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object val) throws ELException {

        if (base == null && property == null) {
            String message = MessageUtils.getExceptionMessageString(MessageUtils.NULL_PARAMETERS_ERROR_MESSAGE_ID, "base and property");
            throw new PropertyNotFoundException(message);
        }

        if (base == null) {
            // create the bean if it doesn't exist. We won't mark the property
            // as resolved in this case, since the spec requires us to actually
            // wait to set the value until the ScopeAttributeELResolved so that
            // implicit scopes with higher priority than this one have a crack
            // at resolving the bean first
            resolveBean(context, property, false);
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

        return false;
    }

    @Override
    public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {

        if (base != null) {
            return null;
        }

        FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);
        BeanManager beanManager = getBeanManager();
        if (beanManager == null || beanManager.getRegisteredBeans().isEmpty()) {
            List<FeatureDescriptor> l = Collections.emptyList();
            return l.iterator();
        }

        Map<String, BeanBuilder> beans = beanManager.getRegisteredBeans();
        List<FeatureDescriptor> list = new ArrayList<>(beans.size());
        // iterate over the list of managed beans
        for (Map.Entry<String, BeanBuilder> bean : beans.entrySet()) {
            String beanName = bean.getKey();
            BeanBuilder builder = bean.getValue();
            String loc = Util.getLocaleFromContextOrSystem(facesContext).toString();
            Map<String, String> descriptions = builder.getDescriptions();

            String description = null;
            if (descriptions != null) {
                description = descriptions.get(loc);
                if (description == null) {
                    description = descriptions.get("DEFAULT");
                }
            }
            list.add(Util.getFeatureDescriptor(beanName, beanName, (description == null) ? "" : description, false, false, true, builder.getBeanClass(),
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

    // --------------------------------------------------------- Private Methods

    private static BeanManager getBeanManager() {

        ApplicationAssociate associate = ApplicationAssociate.getCurrentInstance();
        return ((associate != null) ? associate.getBeanManager() : null);

    }

    private Object resolveBean(ELContext context, Object property, boolean markAsResolvedIfCreated) {
        Object result = null;
        BeanManager manager = getBeanManager();
        if (manager != null) {
            String beanName = property.toString();
            BeanBuilder builder = manager.getBuilder(beanName);
            if (builder != null) {
                FacesContext facesContext = (FacesContext) context.getContext(FacesContext.class);

                // JAVASERVERFACES-2989: Make sure to check request, session, and application.
                ExternalContext extContext = facesContext.getExternalContext();
                if (extContext.getRequestMap().containsKey(beanName)) {
                    return null;
                } else if (null != extContext.getSession(false) && extContext.getSessionMap().containsKey(beanName)) {
                    return null;
                } else if (extContext.getApplicationMap().containsKey(beanName)) {
                    return null;
                }

                result = manager.getBeanFromScope(beanName, builder, facesContext);
                if (result == null) {
                    result = manager.create(beanName, builder, facesContext);
                }
                context.setPropertyResolved(markAsResolvedIfCreated && (result != null));
            }
        }

        return result;
    }

}
