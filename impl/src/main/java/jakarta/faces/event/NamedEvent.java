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

package jakarta.faces.event;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <p class="changed_added_2_0">
 * The presence of this annotation on a class automatically registers the class with the runtime as a
 * {@link ComponentSystemEvent} for use with the <code>&lt;f:event /&gt;</code> tag in a page. The value of the
 * {@link #shortName} attribute is taken to be the short name for the {@link jakarta.faces.event.ComponentSystemEvent}.
 * If the <em>shortName</em> has already been registered, the current class must be added to a List of of duplicate
 * events for that name. If the event name is then referenced by an application, a <code>FacesException</code> must be
 * thrown listing the <em>shortName</em> and the offending classes.
 * </p>
 *
 * @since 2.0
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
public @interface NamedEvent {

    /**
     * <p class="changed_added_2_0">
     * The value of this annotation attribute is taken to be the short name for the
     * {@link jakarta.faces.event.ComponentSystemEvent}. If the value of this attribute is ommitted, the following algorithm
     * must be used by the code that processes this annotation to determine its value.
     * </p>
     *
     * <div class="changed_added_2_0">
     *
     * <ol>
     *
     * <li>
     * <p>
     * Get the unqualified class name (e.g., <code>UserLoginEvent</code>)
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Strip off the trailing "Event", if present (e.g., <code>UserLogin</code>)
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Convert the first character to lower-case (e.g., <code>userLogin</code>)
     * </p>
     * </li>
     *
     * <li>
     * <p>
     * Prepend the package name to the lower-cased name.
     * </p>
     * </li>
     *
     * </ol>
     *
     *
     * </div>
     *
     * @return the short name
     */
    String shortName() default "";
}
