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
 * <code>&lt;managed-bean-scope&gt;session&lt;managed-bean-scope&gt;</code>
 * element was declared for the corresponding managed bean.</p>
 *
 * @since 2.0
 * @deprecated This has been replaced by {@code javax.enterprise.context.SessionScoped}, 
 * which is a CDI build-in scope with similar semantics.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Deprecated
public @interface SessionScoped {
}
