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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.util.FacesLogger;

import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.FacesConverter;

/**
 * <p>
 * <code>ConfigAnnotationHandler</code> for {@link FacesConverter} annotated classes.
 * </p>
 */
public class ConverterConfigHandler implements ConfigAnnotationHandler {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    private static final Collection<Class<? extends Annotation>> HANDLES;

    static {
        Collection<Class<? extends Annotation>> handles = new ArrayList<>(1);
        handles.add(FacesConverter.class);
        HANDLES = Collections.unmodifiableCollection(handles);
    }

    private Map<Object, String> converters;

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

        if (converters == null) {
            converters = new HashMap<>();
        }
        Object key;
        FacesConverter converterAnnotation = (FacesConverter) annotation;

        if (converterAnnotation.value().length() > 0 && converterAnnotation.forClass() != null && converterAnnotation.forClass() != Object.class) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, "@FacesConverter is using both value and forClass, only value will be applied.");
            }
        }

        if (0 == converterAnnotation.value().length()) {
            key = converterAnnotation.forClass();
        } else {
            key = converterAnnotation.value();
        }
        converters.put(key, target.getName());

    }

    /**
     * @see com.sun.faces.application.annotation.ConfigAnnotationHandler#push(jakarta.faces.context.FacesContext)
     */
    @Override
    public void push(FacesContext ctx) {

        if (converters != null) {
            Application app = ctx.getApplication();
            for (Map.Entry<Object, String> entry : converters.entrySet()) {
                if (entry.getKey() instanceof Class) {
                    app.addConverter((Class<?>) entry.getKey(), entry.getValue());
                } else {
                    app.addConverter((String) entry.getKey(), entry.getValue());
                }
            }
        }

    }

}
