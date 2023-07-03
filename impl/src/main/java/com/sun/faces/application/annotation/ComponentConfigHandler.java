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

package com.sun.faces.application.annotation;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.faces.application.ApplicationAssociate;

import jakarta.faces.application.Application;
import jakarta.faces.component.FacesComponent;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * <code>ConfigAnnotationHandler</code> for {@link FacesComponent} annotated classes.
 * </p>
 */
public class ComponentConfigHandler implements ConfigAnnotationHandler {

    private static final Collection<Class<? extends Annotation>> HANDLES = List.of(FacesComponent.class);

    // key: componentId
    private Map<String, FacesComponentUsage> components;

    // ------------------------------------- Methods from ComponentConfigHandler

    /**
     * @see com.sun.faces.application.annotation.ConfigAnnotationHandler#getHandledAnnotations()
     */
    @Override
    public Collection<Class<? extends Annotation>> getHandledAnnotations() {

        return HANDLES;

    }

    /**
     * @see com.sun.faces.application.annotation.ConfigAnnotationHandler#collect(Class, java.lang.annotation.Annotation)
     */
    @Override
    public void collect(Class<?> target, Annotation annotation) {

        if (components == null) {
            components = new HashMap<>();
        }
        String value = ((FacesComponent) annotation).value();
        if (null == value || 0 == value.length()) {
            value = target.getSimpleName();
            value = Character.toLowerCase(value.charAt(0)) + value.substring(1);
        }
        components.put(value, new FacesComponentUsage(target, (FacesComponent) annotation));

    }

    /**
     * @see com.sun.faces.application.annotation.ConfigAnnotationHandler#push(jakarta.faces.context.FacesContext)
     */
    @Override
    public void push(FacesContext ctx) {

        if (components != null) {
            Application app = ctx.getApplication();
            ApplicationAssociate appAss = ApplicationAssociate.getCurrentInstance();
            for (Map.Entry<String, FacesComponentUsage> entry : components.entrySet()) {
                if (entry.getValue().getAnnotation().createTag()) {
                    appAss.addFacesComponent(entry.getValue());
                }
                app.addComponent(entry.getKey(), entry.getValue().getTarget().getName());
            }
        }

    }

}
