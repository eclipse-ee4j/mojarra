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

import com.sun.faces.util.Util;

import jakarta.faces.application.ResourceDependencies;
import jakarta.faces.application.ResourceDependency;

/**
 * <code>Scanner</code> implementation responsible for {@link ResourceDependency} annotations.
 */
class ResourceDependencyScanner implements Scanner {

    // ---------------------------------------------------- Methods from Scanner

    @Override
    public Class<? extends Annotation> getAnnotation() {

        return ResourceDependency.class;

    }

    @Override
    public RuntimeAnnotationHandler scan(Class<?> clazz) {

        Util.notNull("clazz", clazz);

        ResourceDependencyHandler handler = null;
        ResourceDependency dep = clazz.getAnnotation(ResourceDependency.class);
        if (dep != null) {
            handler = new ResourceDependencyHandler(new ResourceDependency[] { dep });
        } else {
            ResourceDependencies deps = clazz.getAnnotation(ResourceDependencies.class);
            if (deps != null) {
                handler = new ResourceDependencyHandler(deps.value());
            }
        }

        return handler;

    }

}
