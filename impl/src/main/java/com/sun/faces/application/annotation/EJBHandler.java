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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;

/**
 * {@link RuntimeAnnotationHandler} responsible for processing EJB annotations.
 */
class EJBHandler extends JndiHandler implements RuntimeAnnotationHandler {

    private static final String JAVA_MODULE = "java:module/";
    private final Field[] fields;
    private final EJB[] fieldAnnotations;
    private final Method[] methods;
    private final EJB[] methodAnnotations;

    public EJBHandler(Field[] fields, EJB[] fieldAnnotations, Method[] methods, EJB[] methodAnnotations) {
        this.fields = fields;
        this.fieldAnnotations = fieldAnnotations;
        this.methods = methods;
        this.methodAnnotations = methodAnnotations;
    }

    @SuppressWarnings({ "UnusedDeclaration" })
    @Override
    public void apply(FacesContext ctx, Object... params) {
        Object object = params[0];
        for (int i = 0; i < fields.length; i++) {
            applyToField(ctx, fields[0], fieldAnnotations[0], object);
        }

        for (int i = 0; i < methods.length; i++) {
            applyToMethod(ctx, methods[i], methodAnnotations[i], object);
        }
    }

    private void applyToField(FacesContext facesContext, Field field, EJB ejb, Object instance) {
        Object value;
        if (ejb.lookup() != null && !"".equals(ejb.lookup().trim())) {
            value = lookup(facesContext, ejb.lookup());
        } else if (ejb.name() != null && !"".equals(ejb.name().trim())) {
            value = lookup(facesContext, JAVA_COMP_ENV + ejb.name());
        } else {
            value = lookup(facesContext, JAVA_MODULE + field.getType().getSimpleName());
        }
        setField(facesContext, field, instance, value);
    }

    private void applyToMethod(FacesContext facesContext, Method method, EJB ejb, Object instance) {
        if (method.getName().startsWith("set")) {
            Object value = null;
            if (ejb.lookup() != null && !"".equals(ejb.lookup().trim())) {
                value = lookup(facesContext, ejb.lookup());
            } else if (ejb.name() != null && !"".equals(ejb.name().trim())) {
                value = lookup(facesContext, JAVA_COMP_ENV + ejb.name());
            }
            invokeMethod(facesContext, method, instance, value);
        }
    }
}
