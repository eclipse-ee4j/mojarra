/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package jakarta.faces.validator;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.inject.Qualifier;

/**
 * <p class="changed_added_2_0">
 * <span class="changed_modified_2_2">The</span> presence of this annotation on a class automatically registers the
 * class with the runtime as a {@link Validator}. The value of the {@link #value} attribute is taken to be the
 * <em>validator-id</em> and the fully qualified class name of the class to which this annotation is attached is taken
 * to be the <em>validator-class</em>. 
 * 
 * The implementation must guarantee that for each class annotated with  * <code>FacesValidator</code>, found with the 
 * algorithm in section 11.5 of the spec prose document,
 * {@link jakarta.faces.application.Application#addValidator(java.lang.String,java.lang.String)} is called, passing the
 * derived <em>validator-id</em> as the first argument and the derived <em>validator-class</em> as the second argument.
 * The implementation must guarantee that all such calls to <code>addValidator()</code> happen during application
 * startup time and before any requests are serviced.
 * </p>
 */
@Retention(RUNTIME)
@Target({ TYPE, FIELD, METHOD, PARAMETER })
@Inherited
@Qualifier
public @interface FacesValidator {

    /**
     * <p class="changed_added_2_0">
     * <span class="changed_modified_2_2">The</span> value of this annotation attribute is taken to be the
     * <em>validator-id</em> with which instances of this class of component can be instantiated by calling
     * {@link jakarta.faces.application.Application#createValidator(java.lang.String)}. <span class="changed_added_2_2">If
     * no value is specified, or the value is <code>null</code>, the value is taken to be the return of calling
     * <code>getSimpleName</code> on the class to which this annotation is attached and lowercasing the first character. If
     * more than one validator with this derived name is found, the results are undefined.</span>
     * </p>
     *
     * @return the validator-id
     */

    String value() default "";

    /**
     * <p class="changed_added_2_0">
     * If <code>true</code>, the validator id for this annotation is added to the list of default validators by a call to
     * {@link jakarta.faces.application.Application#addDefaultValidatorId}.
     * </p>
     *
     * @return whether or not this is a default validator
     */

    boolean isDefault() default false;

    /**
     * <p class="changed_added_2_3">
     * The value of this annotation attribute is taken to be an indicator that flags whether or not the given converter is a
     * CDI managed converter.
     * </p>
     *
     * @return true if CDI managed, false otherwise.
     */

    boolean managed() default false;
}
