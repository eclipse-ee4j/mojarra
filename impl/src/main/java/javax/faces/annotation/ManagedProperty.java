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

package javax.faces.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.Nonbinding;
import javax.inject.Qualifier;

/**
 * <p class="changed_added_2_3">The presence of this annotation (along
 * with {@code @Inject}) on a field of any type causes the value returned from
 * evaluating an expression language expression to be
 * injected as the value of that field. </p>
 */

@Qualifier
@Target(FIELD)
@Retention(RUNTIME)
public @interface ManagedProperty {
    
    /**
     * <p class="changed_added_2_3">Taken to be the value that is injected into
     * the field. </p>
     *
     * @return the value.
     */
    @Nonbinding
    String value();
    
}
