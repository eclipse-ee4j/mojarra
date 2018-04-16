/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.vendor;

import com.sun.faces.spi.DiscoverableInjectionProvider;
import com.sun.faces.spi.InjectionProviderException;

import javax.servlet.ServletContext;

import org.apache.AnnotationProcessor;

/**
 *
 */
public class Tomcat6InjectionProvider extends DiscoverableInjectionProvider {

    private ServletContext servletContext;
    

    // ------------------------------------------------------------ Constructors


    public Tomcat6InjectionProvider(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
    

    // ------------------------------------------ Methods from InjectionProvider


    /**
     * <p>The implementation of this method must perform the following
     * steps:
     * <ul>
     * <li>Inject the supported resources per the Servlet 2.5
     * specification into the provided object</li>
     * </ul>
     * </p>
     * <p>This method <em>must not</em> invoke any methods
     * annotated with <code>@PostConstruct</code>
     *
     * @param managedBean the target managed bean
     * @throws com.sun.faces.spi.InjectionProviderException
     *          if an error occurs during
     *          resource injection
     */
    public void inject(Object managedBean) throws InjectionProviderException {
        try {
            getProcessor().processAnnotations(managedBean);
        } catch (Exception e) {
            throw new InjectionProviderException(e);
        }
    }

    /**
     * <p>The implemenation of this method must invoke any
     * method marked with the <code>@PreDestroy</code> annotation
     * (per the Common Annotations Specification).
     *
     * @param managedBean the target managed bean
     * @throws com.sun.faces.spi.InjectionProviderException
     *          if an error occurs when invoking
     *          the method annotated by the <code>@PreDestroy</code> annotation
     */
    public void invokePreDestroy(Object managedBean) throws InjectionProviderException {
        try {
            getProcessor().preDestroy(managedBean);
        } catch (Exception e) {
            throw new InjectionProviderException(e);
        }
    }

    /**
     * <p>The implemenation of this method must invoke any
     * method marked with the <code>@PostConstruct</code> annotation
     * (per the Common Annotations Specification).
     *
     * @param managedBean the target managed bean
     * @throws com.sun.faces.spi.InjectionProviderException
     *          if an error occurs when invoking
     *          the method annotated by the <code>@PostConstruct</code> annotation
     */
    public void invokePostConstruct(Object managedBean) throws InjectionProviderException {
        try {
            getProcessor().postConstruct(managedBean);
        } catch (Exception e) {
            throw new InjectionProviderException(e);
        }
    }


    // --------------------------------------------------------- Private Methods


    private AnnotationProcessor getProcessor() {
        return ((AnnotationProcessor) servletContext.getAttribute(AnnotationProcessor.class.getName()));
    }
}
