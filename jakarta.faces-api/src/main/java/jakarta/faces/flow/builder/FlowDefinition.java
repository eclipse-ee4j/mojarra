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

package jakarta.faces.flow.builder;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

/**
 * <p class="changed_added_2_2">
 * The presence of this annotation on a CDI producer method indicates that the method will produce a flow. See
 * {@link FlowBuilder} for a usage example.
 * </p>
 *
 * @since 2.2
 */

@Retention(RUNTIME)
@Target({ METHOD })
@Qualifier
public @interface FlowDefinition {

    /**
     * <p class="changed_added_4_0">
     * Supports inline instantiation of the {@link FlowDefinition} qualifier.
     * </p>
     *
     * @since 4.0
     */
    public static final class Literal extends AnnotationLiteral<FlowDefinition> implements FlowDefinition {

        private static final long serialVersionUID = 1L;

        /**
         * Instance of the {@link FlowDefinition} qualifier.
         */
        public static final Literal INSTANCE = new Literal();
    }
}
