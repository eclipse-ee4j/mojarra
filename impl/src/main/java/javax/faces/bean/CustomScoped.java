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

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;

/**
 * <p class="changed_added_2_0">When this annotation, along with {@link
 * ManagedBean} is found on a class, the runtime must act as if a
 * <code>&lt;managed-bean-scope&gt;VALUE&lt;managed-bean-scope&gt;</code>
 * element was declared for the corresponding managed bean, where VALUE is the
 * value of the {@link #value} attribute, which must be a Jakarta Expression Language expression that
 * evaluates to a <code>Map</code>.</p>
 *
 * <p class="changed_added_2_0">Developers must take care when using custom
 * scopes to ensure that any object references made to or from a custom scoped
 * bean consider the necessary scope lifetimes. The runtime is not required to
 * perform any validations for such considerations.</p>
 *
 *
 * @since 2.0
 * @deprecated This has been replaced by CDI custom scopes and 
 * {@code javax.enterprise.context.spi.Context}. See 2.4.2 and 6.2 of the CDI
 * specification for further details.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Deprecated
public @interface CustomScoped {

    /**
     * Get the value.
     * 
     * @return the value.
     */
    public String value();
}
