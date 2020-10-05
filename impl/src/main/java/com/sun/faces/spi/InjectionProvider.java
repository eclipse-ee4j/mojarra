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

/**
 * <p>
 * This interface defines an integration point for Jakarta EE vendors. Each vendor will need to provide an implementation
 * of this interface which will provide the Faces implementation the necessary hooks to perform resource injection.
 * </p>
 *
 * <p>
 * The implementation of this interface *must* be thread-safe and must provider either a no-arg constructor, or a
 * constructor accepting a <code>ServletContext</code> instance.
 * </p>
 */
public interface InjectionProvider {

    /**
     * <p>
     * The implementation of this method must perform the following steps:
     * <ul>
     * <li>Inject the supported resources per the Servlet 2.5 specification into the provided object</li>
     * </ul>
     * </p>
     * <p>
     * This method <em>must not</em> invoke any methods annotated with <code>@PostConstruct</code>
     *
     * @param managedBean the target managed bean
     * @throws InjectionProviderException if an error occurs during resource injection
     */
    void inject(Object managedBean) throws InjectionProviderException;

    /**
     * <p>
     * The implemenation of this method must invoke any method marked with the <code>@PreDestroy</code> annotation (per the
     * Common Annotations Specification).
     *
     * @param managedBean the target managed bean
     * @throws InjectionProviderException if an error occurs when invoking the method annotated by the
     * <code>@PreDestroy</code> annotation
     */
    void invokePreDestroy(Object managedBean) throws InjectionProviderException;

    /**
     * <p>
     * The implemenation of this method must invoke any method marked with the <code>@PostConstruct</code> annotation (per
     * the Common Annotations Specification).
     *
     * @param managedBean the target managed bean
     * @throws InjectionProviderException if an error occurs when invoking the method annotated by the
     * <code>@PostConstruct</code> annotation
     */
    void invokePostConstruct(Object managedBean) throws InjectionProviderException;

}
