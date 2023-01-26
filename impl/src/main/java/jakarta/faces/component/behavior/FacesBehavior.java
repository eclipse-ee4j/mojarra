/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021-2021 Contributors to the Eclipse Foundation
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

package jakarta.faces.component.behavior;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Qualifier;

/**
 * <p class="changed_added_2_0 changed_modified_2_3">
 * The presence of this annotation on a class automatically registers the class with the runtime as a {@link Behavior}.
 * The value of this annotation attribute is taken to be the <em>behavior-id</em> with which instances of this class of
 * behavior can be instantiated by calling
 * {@link jakarta.faces.application.Application#createBehavior(java.lang.String)}
 * </p>
 *
 * @since 2.0
 */
@Retention(RUNTIME)
@Target({ TYPE, FIELD, METHOD, PARAMETER })
@Inherited
@Qualifier
public @interface FacesBehavior {
    
    /**
     * The value of this annotation attribute is taken to be the <em>behavior-id</em> with which instances of this class of
     * behavior can be instantiated. 
     * @return the <em>behavior-id</em>
     */
    String value();

    /**
     * <p class="changed_added_2_3">
     * The value of this annotation attribute is taken to be an indicator that flags whether or not the given behavior is a
     * CDI managed behavior.
     * </p>
     *
     * @return true if CDI managed, false otherwise.
     */
    boolean managed() default false;

    /**
     * <p class="changed_added_4_0">
     * Supports inline instantiation of the {@link FacesBehavior} qualifier.
     * </p>
     *
     * @since 4.0
     */
    public static final class Literal extends AnnotationLiteral<FacesBehavior> implements FacesBehavior {

        private static final long serialVersionUID = 1L;

        /**
         * Instance of the {@link FacesBehavior} qualifier.
         */
        public static final Literal INSTANCE = of("", false);

        private final String value;
        private final boolean managed;

        public static Literal of(String value, boolean managed) {
            return new Literal(value, managed);
        }

        private Literal(String value, boolean managed) {
            this.value = value;
            this.managed = managed;
        }

        @Override
        public String value() {
            return value;
        }
        
        @Override
        public boolean managed() {
            return managed;
        }
    }
}
