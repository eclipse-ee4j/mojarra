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
import jakarta.xml.ws.WebServiceRef;

/**
 * {@link RuntimeAnnotationHandler} responsible for processing {@link WebServiceRef} annotations.
 */
class WebServiceRefHandler extends JndiHandler implements RuntimeAnnotationHandler {

    private final Field[] fields;
    private final WebServiceRef[] fieldAnnotations;
    private final Method[] methods;
    private final WebServiceRef[] methodAnnotations;

    public WebServiceRefHandler(Field[] fields, WebServiceRef[] fieldAnnotations, Method[] methods, WebServiceRef[] methodAnnotations) {
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

    private void applyToField(FacesContext facesContext, Field field, WebServiceRef ref, Object instance) {
        Object value = null;
        /*
         * if (ref.lookup() != null && !"".equals(ref.lookup().trim())) { value = lookup(facesContext, ref.lookup()); } else
         */
        if (ref.name() != null && !"".equals(ref.name().trim())) {
            value = lookup(facesContext, JAVA_COMP_ENV + ref.name());
        } else {
            value = lookup(facesContext, field.getName());
        }
        setField(facesContext, field, instance, value);
    }

    private void applyToMethod(FacesContext facesContext, Method method, WebServiceRef ref, Object instance) {
        if (method.getName().startsWith("set")) {
            Object value = null;
            /*
             * if (ref.lookup() != null && !"".equals(ref.lookup().trim())) { value = lookup(facesContext, ref.lookup()); } else
             */
            if (ref.name() != null && !"".equals(ref.name().trim())) {
                value = lookup(facesContext, JAVA_COMP_ENV + ref.name());
            }
            invokeMethod(facesContext, method, instance, value);
        }
    }
}
