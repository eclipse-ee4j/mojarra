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

package com.sun.faces.spi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.faces.config.manager.spi.FilterClassesFromFacesInitializerAnnotationProvider;
import com.sun.faces.util.FacesLogger;

import jakarta.faces.FacesException;
import jakarta.servlet.ServletContext;

/**
 *
 */
public class AnnotationProviderFactory {

    private static final Logger LOGGER = FacesLogger.APPLICATION.getLogger();

    private static final Class<? extends AnnotationProvider> DEFAULT_ANNOTATION_PROVIDER = FilterClassesFromFacesInitializerAnnotationProvider.class;

    private static final String ANNOTATION_PROVIDER_SERVICE_KEY = "com.sun.faces.spi.annotationprovider";

    // ---------------------------------------------------------- Public Methods

    public static AnnotationProvider createAnnotationProvider(ServletContext sc) {
        AnnotationProvider annotationProvider = createDefaultProvider(sc);

        String[] services = ServiceFactoryUtils.getServiceEntries(ANNOTATION_PROVIDER_SERVICE_KEY);
        if (services.length > 0) {
            // only use the first entry...
            Object provider = null;
            try {
                // try two arguments constructor
                provider = ServiceFactoryUtils.getProviderFromEntry(services[0], new Class<?>[] { ServletContext.class, AnnotationProvider.class },
                        new Object[] { sc, annotationProvider });
            } catch (FacesException e) {
                if (!NoSuchMethodException.class.isInstance(e.getCause())) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, e.toString(), e);
                    }
                }
            }

            if (provider == null) {
                try {
                    // try one argument constructor
                    provider = ServiceFactoryUtils.getProviderFromEntry(services[0], new Class<?>[] { ServletContext.class }, new Object[] { sc });
                } catch (FacesException e) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, e.toString(), e);
                    }
                }
            }
            if (provider != null) {
                if (!(provider instanceof AnnotationProvider)) {
                    throw new FacesException("Class " + provider.getClass().getName() + " is not an instance of com.sun.faces.spi.AnnotationProvider");
                }
                annotationProvider = (AnnotationProvider) provider;
            }
        } else {

            ServiceLoader<AnnotationProvider> serviceLoader = ServiceLoader.load(AnnotationProvider.class);
            Iterator iterator = serviceLoader.iterator();

            if (iterator.hasNext()) {

                AnnotationProvider defaultAnnotationProvider = annotationProvider;
                annotationProvider = (AnnotationProvider) iterator.next();
                annotationProvider.initialize(sc, defaultAnnotationProvider);
            }
        }

        return annotationProvider;
    }

    // --------------------------------------------------------- Private Methods

    private static AnnotationProvider createDefaultProvider(ServletContext sc) {
        AnnotationProvider result = null;
        Constructor c;

        try {
            c = DEFAULT_ANNOTATION_PROVIDER.getDeclaredConstructor(ServletContext.class);
            result = (AnnotationProvider) c.newInstance(sc);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e2) {
            throw new FacesException(e2);
        }
        return result;
    }
}
