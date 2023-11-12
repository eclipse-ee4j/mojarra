/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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

package jakarta.faces.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.enterprise.util.Nonbinding;
import jakarta.faces.application.Application;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Qualifier;

/**
 * <p class="changed_added_2_3">
 * The presence of this annotation (along with {@code @Inject}) on a field of any type causes the value returned from
 * evaluating an expression language expression to be injected as the value of that field.
 * <span class="changed_modified_4_0">
 * This expression will be evaluated using {@link Application#evaluateExpressionGet(jakarta.faces.context.FacesContext, String, Class)},
 * which in turn implies that the {@link FacesContext#getCurrentInstance()} must be available at the moment of the evaluation.
 * </span>
 * </p>
 */

@Qualifier
@Target(FIELD)
@Retention(RUNTIME)
public @interface ManagedProperty {


    /**
     * <p class="changed_added_2_3">
     * Taken to be the value that is injected into the field.
     * </p>
     *
     * @return the value.
     */
    @Nonbinding
    String value();

    /**
     * <p class="changed_added_4_0">
     * Supports inline instantiation of the {@link ManagedProperty} qualifier.
     * </p>
     *
     * @since 4.0
     */
    public static final class Literal extends AnnotationLiteral<ManagedProperty> implements ManagedProperty {

        private static final long serialVersionUID = 1L;

        /**
         * Instance of the {@link ManagedProperty} qualifier.
         */
        public static final Literal INSTANCE = of("");

        private final String value;

        public static Literal of(String value) {
            return new Literal(value);
        }

        private Literal(String value) {
            this.value = value;
        }

        @Override
        public String value() {
            return value;
        }
    }

}
