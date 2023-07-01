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

import jakarta.faces.context.FacesContext;
import jakarta.persistence.PersistenceUnit;

/**
 * {@link RuntimeAnnotationHandler} responsible for processing {@link PersistenceUnit} annotations.
 */
class PersistenceUnitHandler extends JndiHandler implements RuntimeAnnotationHandler {

    private final Method[] methods;
    private final PersistenceUnit[] methodAnnotations;
    private final Field[] fields;
    private final PersistenceUnit[] fieldAnnotations;

    public PersistenceUnitHandler(Method[] methods, PersistenceUnit[] methodAnnotations, Field[] fields, PersistenceUnit[] fieldAnnotations) {
        this.methods = methods;
        this.methodAnnotations = methodAnnotations;
        this.fields = fields;
        this.fieldAnnotations = fieldAnnotations;
    }

    @SuppressWarnings({ "UnusedDeclaration" })
    @Override
    public void apply(FacesContext ctx, Object... params) {
        Object object = params[0];
        for (int i = 0; i < fields.length; i++) {
            applyToField(ctx, fields[i], fieldAnnotations[i], object);
        }

        for (int i = 0; i < methods.length; i++) {
            applyToMethod(ctx, methods[i], methodAnnotations[i], object);
        }
    }

    private void applyToMethod(FacesContext facesContext, Method method, PersistenceUnit unit, Object instance) {
        if (method.getName().startsWith("set")) {
            Object value = null;
            if (unit.name() != null && !"".equals(unit.name().trim())) {
                value = lookup(facesContext, JAVA_COMP_ENV + unit.name());
            }
            invokeMethod(facesContext, method, instance, value);
        }
    }

    private void applyToField(FacesContext facesContext, Field field, PersistenceUnit unit, Object instance) {
        Object value;
        if (unit.name() != null && !"".equals(unit.name().trim())) {
            value = lookup(facesContext, JAVA_COMP_ENV + unit.name());
        } else {
            value = lookup(facesContext, field.getType().getSimpleName());
        }
        setField(facesContext, field, instance, value);
    }
}
