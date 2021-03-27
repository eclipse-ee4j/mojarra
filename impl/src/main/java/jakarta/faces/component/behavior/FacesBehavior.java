/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.faces.component.behavior;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.inject.Qualifier;

/**
 * <p class="changed_added_2_0 changed_modified_2_3">
 * The presence of this annotation on a class automatically registers the class with the runtime as a {@link Behavior}.
 * The value of this annotation attribute is taken to be the <em>behavior-id</em> with which instances of this class of
 * behavior can be instantiated by calling
 * {@link jakarta.faces.application.Application#createBehavior(java.lang.String)}
 * </p>
 *
 * @since 2.0
 */
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
@Qualifier
public @interface FacesBehavior {
    
    /**
     * The value of this annotation attribute is taken to be the <em>behavior-id</em> with which instances of this class of
     * behavior can be instantiated. 
     * @return the <em>behavior-id</em>
     */
    String value();

    /**
     * <p class="changed_added_2_3">
     * The value of this annotation attribute is taken to be an indicator that flags whether or not the given converter is a
     * CDI managed converter.
     * </p>
     *
     * @return true if CDI managed, false otherwise.
     */
    boolean managed() default false;
}
