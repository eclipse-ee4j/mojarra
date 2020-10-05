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

import jakarta.faces.context.FacesContext;

/**
 * Implementations of this class provide basic caching and processing of of {@link java.lang.annotation.Annotation}
 * instances.
 */
interface RuntimeAnnotationHandler {

    /**
     * <p>
     * Apply the {@link java.lang.annotation.Annotation}(s). The act of doing so should affect the Faces runtime in some
     * fashion (see the spec for the specific annotation types).
     * </p>
     *
     * <p>
     * <em>NOTE</em>: when adding new types of components that can be annotated, the fact that we expose varargs here should
     * be hidden. Type-safe methods should be added to {@link AnnotationManager} to clarify the contract.
     * </p>
     *
     * @param ctx the {@link jakarta.faces.context.FacesContext} for the current request
     * @param params one or more arguments to the handler instance. The type and number may vary.
     */
    void apply(FacesContext ctx, Object... params);

}
