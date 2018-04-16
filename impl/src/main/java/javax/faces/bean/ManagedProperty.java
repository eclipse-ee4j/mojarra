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

/**
 * <p class="changed_added_2_0">The presence of this annotation on a field of a
 * class annotated with {@link ManagedBean} instructs the system to inject a
 * value into this property as described in section JSF.5.3 of the spec prose
 * document in the <code>&lt;managed-property&gt;</code> subsection. The time of
 * instantiation is dictated by the value of the attributes on the usage of
 * <code>ManagedBean</code> and by the application logic itself. The value of
 * the {@link #value} attribute may be a literal <code>String</code> or a
 * <code>ValueExpression</code>. If the latter, the expression must not be
 * evaluated until the bean is instantiated. The value of the name attribute is
 * taken to be the
 * <em>managed-property-name</em> for this property. If not specified, the
 * <em>managed-property-name</em> is taken to be the name of the field to which
 * this is attribute is attached.</p>
 *
 * <p class="changed_added_2_0">If this annotation is present on a class that
 * does not have the <code>ManagedBean</code> annotation, the implementation
 * must take no action on this annotation.</p>
 * 
 * @deprecated This has been replaced by {@code javax.faces.annotation.ManagedProperty}, 
 * which is a CDI build-in bean with similar semantics
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Deprecated
public @interface ManagedProperty {

    /**
     * <p class="changed_added_2_0">Taken to be the
     * <code>managed-property-name</code>. See class documentation for
     * details.</p>
     *
     * @return the managed property name.
     */
    String name() default "";

    /**
     * <p class="changed_added_2_0">Taken to be the value that is injected into
     * the field. See class documentation for details.</p>
     *
     * @return the value.
     */
    String value();
}
