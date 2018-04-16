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

package javax.faces.bean;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Inherited;

/**
 * <p class="changed_added_2_0">The presence of this annotation on a class is
 * equivalent to the <em>referenced-bean</em> element in the application
 * configuration resources.</p>
 *
 * @since 2.0
 * @deprecated The referenced-bean concept was used for a design time promise
 * which however did not achieve widespread adoption. There is no direct 
 * replacement for this other than using the XML variant in faces-config.xml.
 */
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
@Inherited
@Deprecated
public @interface ReferencedBean {

    /**
     * <p class="changed_added_2_0">Taken to be the
     * <code>referenced-bean-name</code>. See class documentation for
     * {@link ManagedBean} for details.</p>
     *
     * @return the referenced bean name.
     */
    String name() default "";
}
