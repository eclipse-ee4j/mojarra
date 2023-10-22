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

package jakarta.faces.flow;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.enterprise.context.NormalScope;

/**
 * <p class="changed_added_2_2">
 * <strong class="changed_modified_2_3"> FlowScoped</strong> is a CDI scope that causes the runtime to consider classes
 * with this annotation to be in the scope of the specified {@link Flow}. The implementation must provide an
 * implementation of {@code jakarta.enterprise.inject.spi.Extension} that implements the semantics such that beans with
 * this annotation are created <span class="changed_added_2_3">lazily, when referenced, after</span> the user enters
 * into the specified {@code Flow}, and <span class="changed_added_2_3">made available for garbage collection</span>
 * when the user exits the specified {@code Flow}. See {@link FlowHandler#transition} for the specification of flow
 * entry and exit.
 * </p>
 *
 * <p class="changed_added_2_3">
 * When replacing (rather than decorating) the flow implementation with a custom {@link FlowHandler} implementation, it
 * is necessary to also replace the CDI extension that implements the specified behavior regarding
 * <code>FlowScoped</code> beans.
 * </p>
 * 
 * <p class="changed_added_4_1">
 * Events with qualifiers  {@code @Initialized}, {@code @BeforeDestroyed}, and {@code @Destroyed} as defined by the CDI specification  must fire for this built-in scope. 
 * </p>
 *
 * @since 2.2
 */

@NormalScope(passivating = true)
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface FlowScoped {

    /**
     * <p class="changed_added_2_2">
     * Must be equivalent to the {@link Flow#getId} of a defined flow for this application.
     * </p>
     *
     * @since 2.2
     *
     * @return the id of this flow
     */
    String value();

    /**
     * <p class="changed_added_2_2">
     * If not empty, declare the defining document id within which the {@link Flow} referenced by {@link #value} is unique.
     * If empty the, the runtime assumes that all flow ids are unique within the scope of the application.
     * </p>
     *
     * @since 2.2
     *
     * @return the defining document id of this flow
     */

    String definingDocumentId() default "";

}
