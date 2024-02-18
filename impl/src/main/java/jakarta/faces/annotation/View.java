/*
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
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.faces.component.ActionSource;
import jakarta.inject.Qualifier;

/**
 * <p class="changed_added_4_0">
 * The presence of this annotation on a target (type, method, parameter or field) within an application is used to indicate that
 * this target is somehow handling a Faces View Id or Ids.
 * </p>
 *
 * <p>
 * The exact way in which such view is handled depends on the annotated element in question.
 *
 * @since 4.0
 *
 */

@Target({ TYPE, METHOD, PARAMETER, FIELD })
@Retention(RUNTIME)
@Qualifier
@Documented
public @interface View {

    /**
     * <p>
     * Set the Faces View Id pattern.
     * </p>
     *
     * The Faces View Id pattern can represent a single view, such as "/index.xhtml",
     * or a pattern like "/foo/bar/*". Though the exact interpretation of the Faces View Id
     * for a single view is ultimately defined by the annotated element, in general it should
     * align with the return value from an action expression
     * (see {@link ActionSource#setActionExpression(jakarta.el.MethodExpression)}
     *
     * @return the Faces View Id pattern
     */
    String value() default "";

    /**
     * Supports inline instantiation of the {@link View} annotation.
     *
     */
    public final static class Literal extends AnnotationLiteral<View> implements View {

        /**
         * Instance of the {@link View} qualifier.
         */
        public static final Literal INSTANCE = of("");

        private static final long serialVersionUID = 1L;

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
