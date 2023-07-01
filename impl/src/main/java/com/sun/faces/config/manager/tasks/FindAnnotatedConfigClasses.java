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

package com.sun.faces.config.manager.tasks;

import static com.sun.faces.RIConstants.ANNOTATED_CLASSES;
import static java.util.Collections.emptySet;

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.sun.faces.config.InitFacesContext;
import com.sun.faces.config.manager.spi.FilterClassesFromFacesInitializerAnnotationProvider;
import com.sun.faces.spi.AnnotationProvider;
import com.sun.faces.spi.AnnotationProviderFactory;
import com.sun.faces.util.Timer;

import jakarta.servlet.ServletContext;

/**
 * Scans the class files within a web application returning a <code>Set</code> of classes that have been annotated with
 * a standard Faces annotation (possibly limited to the annotations that denote configurable elements)
 */
public class FindAnnotatedConfigClasses implements Callable<Map<Class<? extends Annotation>, Set<Class<?>>>> {

    private final InitFacesContext facesContext;
    private final AnnotationProvider provider;
    private final ProvideMetadataToAnnotationScanTask metadataGetter;
    private final Set<Class<?>> annotatedSet;

    // -------------------------------------------------------- Constructors

    @SuppressWarnings("unchecked")
    public FindAnnotatedConfigClasses(ServletContext servletContext, InitFacesContext facesContext, ProvideMetadataToAnnotationScanTask metadataGetter) {
        this.facesContext = facesContext;
        provider = AnnotationProviderFactory.createAnnotationProvider(servletContext);
        this.metadataGetter = metadataGetter;
        annotatedSet = (Set<Class<?>>) servletContext.getAttribute(ANNOTATED_CLASSES);
    }

    // ----------------------------------------------- Methods from Callable

    @Override
    public Map<Class<? extends Annotation>, Set<Class<?>>> call() throws Exception {

        Timer t = Timer.getInstance();
        if (t != null) {
            t.startTiming();
        }

        // We are executing on a different thread.
        facesContext.addInitContextEntryForCurrentThread();
        Set<URI> scanUris = null;
        com.sun.faces.spi.AnnotationScanner annotationScanner = metadataGetter.getAnnotationScanner();

        // This is where we discover what kind of InjectionProvider we have.
        if (provider instanceof FilterClassesFromFacesInitializerAnnotationProvider && annotationScanner != null) {
            // This InjectionProvider is capable of annotation scanning *and* injection.

            // Note that DelegatingAnnotationProvider itself doesn't use the provided scanner, but just uses the classes
            // that were already scanned by the ServletContainerInitializer and stored there.

            ((FilterClassesFromFacesInitializerAnnotationProvider) provider).setAnnotationScanner(annotationScanner, metadataGetter.getJarNames(annotatedSet));
            scanUris = emptySet();
        } else {
            // This InjectionProvider is capable of annotation scanning only
            scanUris = metadataGetter.getAnnotationScanURIs(annotatedSet);
        }

        // Note that DelegatingAnnotationProvider itself ignores the scanUris and directly gets the classes from the
        // ServletContext where they were stored by the ServletContainerInitializer

        Map<Class<? extends Annotation>, Set<Class<?>>> annotatedClasses = provider.getAnnotatedClasses(scanUris);

        if (t != null) {
            t.stopTiming();
            t.logResult("Configuration annotation scan complete.");
        }

        return annotatedClasses;
    }

} // END AnnotationScanTask
