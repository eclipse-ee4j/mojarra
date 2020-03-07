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

/**
 * <p>
 * Instances of this interface are responsible for scanning a <code>Class</code> for a specific annotation types.
 * </p>
 *
 * <p>
 * Scanner implementations <em>must</em> be thread safe.
 * </p>
 */
interface Scanner {

    /**
     * @return the {@link java.lang.annotation.Annotation} this <code>Scanner</code> is responsible for handling.
     * <em>NOTE</em>: while a particular <code>Scanner</code> instance may handle a plural version of an
     * <code>Annotation</code> in additional to a singular, this method must return the singular version only.
     */
    Class<? extends Annotation> getAnnotation();

    /**
     * Scan the target class for the {@link java.lang.annotation.Annotation}s this scanner handles.
     *
     * @param clazz the target class
     * @return a new {@link RuntimeAnnotationHandler} instance capable of processing the annotations defined on this class.
     * If no relevant {@link java.lang.annotation.Annotation}s are found, return <code>null</code>.
     */
    RuntimeAnnotationHandler scan(Class<?> clazz);

}
