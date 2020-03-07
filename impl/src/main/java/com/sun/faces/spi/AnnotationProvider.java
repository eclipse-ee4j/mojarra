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

import java.lang.annotation.Annotation;
import java.net.URI;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import jakarta.servlet.ServletContext;

/**
 * <p>
 * An integration point for integrators to provide custom annotation scanning.
 * </p>
 *
 * <p>
 * <em>All</em> <code>AnnotationProvider</code> implementations <em>must</em> scan for the following annotations:
 * </p>
 * <ul>
 * <li>FacesComponent</li>
 * <li>FacesConverter</li>
 * <li>FacesRenderer</li>
 * <li>FacesValidator</li>
 * <li>ManagedBean</li>
 * <li>NamedEvent</li>
 * <li>FacesBehavior</li>
 * <li>FacesBehaviorRenderer</li>
 * </ul>
 *
 * <p>
 * The <code>AnnotationProvider</code> instance will be wrapped as a {@link java.util.concurrent.Future} and executed
 * during the environment initialization. The result of the future can be obtained by calling
 * {@link com.sun.faces.config.ConfigManager#getAnnotatedClasses(jakarta.faces.context.FacesContext)}.
 * </p>
 *
 * <p>
 * The {@link java.util.concurrent.Future} itself can be obtained from the application map using the key
 * <code>com.sun.faces.config.ConfigManager__ANNOTATION_SCAN_TASK</code>.
 * </p>
 *
 * <p>
 * It's important to note that the value returned by either method described above is only available while the
 * application is being initialized and will be removed before the application is put into service.
 * </p>
 *
 * <p>
 * To register a custom AnnotationProvider with the runtime, place a file named com.sun.faces.spi.annotationprovider
 * within META-INF/services of a JAR file, with a single line referencing the fully qualified class name of the
 * AnnotationProvider implementation. Custom AnnotationProviders can be used to decorate the default AnnotationProvider
 * by providing a constructor that takes an AnnotationProvider as the second parameter:
 * </p>
 *
 * <p>
 * <code>public AnnotationProvider(ServletContext sc, AnnotationProvider parent)</code>
 * </p>
 *
 * <p>
 * If decoration is not desired, then the custom provider must have a constructor that takes one paramer, a
 * <code>ServletContext</code>:
 *
 * <p>
 * <code>public AnnotationProvider(ServletContext sc)</code>
 * </p>
 *
 * <p>
 * All customer providers must extend this class.
 * </p>
 *
 */
public abstract class AnnotationProvider {

    protected ServletContext servletContext;

    /**
     * The wrapped annotation provider. May be null if this class is not loaded via {@link ServiceLoader} (from a file named
     * com.sun.faces.spi.AnnotationProvider) and is instead loaded via Mojarra's {@link ServiceFactory} (from a file named
     * com.sun.faces.spi.annotationprovider).
     */
    protected AnnotationProvider wrappedAnnotationProvider;

    // ------------------------------------------------------------ Constructors

    public AnnotationProvider(ServletContext servletContext) {
        initialize(servletContext, null);
    }

    public AnnotationProvider() {
    }

    // ---------------------------------------------------------- Package Methods

    final void initialize(ServletContext servletContext, AnnotationProvider wrappedAnnotationProvider) {

        if (this.servletContext == null) {
            this.servletContext = servletContext;
        }

        if (this.wrappedAnnotationProvider == null) {
            this.wrappedAnnotationProvider = wrappedAnnotationProvider;
        }
    }

    // ---------------------------------------------------------- Public Methods

    /**
     * @param urls a <code>Set</code> of URLs that refer to specific faces-config.xml documents on the classpath. The
     * information returned by the map may return annotation information from sources outside of those defined by the urls.
     * @return a <code>Map</code> of classes mapped to a specific annotation type. If no annotations are present, this
     * method returns an empty <code>Map</code>.
     */
    public abstract Map<Class<? extends Annotation>, Set<Class<?>>> getAnnotatedClasses(Set<URI> urls);

}
