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

package jakarta.faces.application;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p class="changed_added_2_0">
 * Container annotation to specify multiple {@link ResourceDependency} annotations on a single class. Example:
 * </p>
 *
 * <pre>
 * <code>
&#0064;ResourceDependencies( {
  &#0064;ResourceDependency(library="corporate", name="css_master.css"),
  &#0064;ResourceDependency(library="client01", name="layout.css"),
  &#0064;ResourceDependency(library="corporate", name="typography.css"),
  &#0064;ResourceDependency(library="client01", name="colorAndMedia.css"),
  &#0064;ResourceDependency(library="corporate", name="table2.css"),
  &#0064;ResourceDependency(library="fancy", name="commontaskssection.css"),
  &#0064;ResourceDependency(library="fancy", name="progressBar.css"),
  &#0064;ResourceDependency(library="fancy", name="css_ns6up.css")
                       })
</code>
 * </pre>
 *
 *
 * <div class="changed_added_2_0">
 *
 * <p>
 * The action described in {@link ResourceDependency} must be taken for each <code>&#0064;ResourceDependency</code>
 * present in the container annotation.
 * </p>
 *
 * </div>
 */
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
public @interface ResourceDependencies {

    /**
     * The individual resource dependencies
     * @return The individual resource dependencies
     */
    ResourceDependency[] value();
}
