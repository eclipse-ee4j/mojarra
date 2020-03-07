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

import jakarta.faces.context.FacesContext;

/**
 * Implementations of the interface will be called during application initialization to process any configuration
 * annotations within the web application.
 */
public interface ConfigAnnotationHandler {

    /**
     * @return a <code>Collection</code> of annotations handled by this ConfigAnnotationHandler implementation
     */
    Collection<Class<? extends Annotation>> getHandledAnnotations();

    /**
     * <p>
     * Collect metadata based on the provided <code>Class</code> and <code>Annotation</code> to be processed later by
     * {@link #push(jakarta.faces.context.FacesContext)}.
     * </p>
     *
     * <p>
     * NOTE: This method may be called more than once.
     * </p>
     *
     *
     * @param target annotated class
     * @param annotation <code>Annotation</code> to process
     */
    void collect(Class<?> target, Annotation annotation);

    /**
     * <code>Push<code> the configuration based on the collected metadata to the current application.
     */
    void push(FacesContext ctx);

}
