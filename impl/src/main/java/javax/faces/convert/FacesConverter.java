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

package javax.faces.convert;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Inherited;
import javax.inject.Qualifier;

/**
 * <p class="changed_added_2_0"><span
 * class="changed_modified_2_2">The</span> presence of this annotation
 * on a class automatically registers the class with the runtime as a
 * {@link Converter}.  The value of the {@link #value} attribute is
 * taken to be <em>converter-id</em>, the value of the {@link #forClass}
 * attribute is taken to be <em>converter-for-class</em> and the fully
 * qualified class name of the class to which this annotation is
 * attached is taken to be the <em>converter-class</em>.  The
 * implementation must guarantee that for each class annotated with
 * <code>FacesConverter</code>, found with the algorithm in section
 * JSF.11.5, the proper variant of
 * <code>Application.addConverter()</code> is called.  If
 * <em>converter-id</em> is not the empty string, {@link
 * javax.faces.application.Application#addConverter(java.lang.String,java.lang.String)}
 * is called, passing the derived <em>converter-id</em> as the first
 * argument and the derived <em>converter-class</em> as the second
 * argument.  If <em>converter-id</em> is the empty string, {@link
 * javax.faces.application.Application#addConverter(java.lang.Class,java.lang.String)}
 * is called, passing the <em>converter-for-class</em> as the first
 * argument and the derived <em>converter-class</em> as the second
 * argument.  The implementation must guarantee that all such calls to
 * <code>addConverter()</code> happen during application startup time
 * and before any requests are serviced.</p>

 * <div class="changed_added_2_2">

 * <p>The preceding text contains an important
 * subtlety which application users should understand.  It is not
 * possible to use a single {@code @FacesConverter} annotation to
 * register a single {@code Converter} implementation both in the {@code
 * by-class} and the {@code by-converter-id} data structures.  One way
 * to achieve this result is to put the actual converter logic in an
 * abstract base class, without a {@code @FacesConverter} annotation,
 * and derive two sub-classes, each with a {@code @FacesConverter}
 * annotation.  One sub-class has a {@code value} attribute but no
 * {@code forClass} attribute, and the other sub-class has the converse.</p>

 * <p>Please see the ViewDeclarationLanguage documentation for {@code
 * <h:selectManyListBox>} for another important subtlety regarding
 * converters and collections.</p>

 * </div>

 *
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Inherited
@Qualifier
public @interface FacesConverter {

    /**
     * <p class="changed_added_2_0">The value of this annotation
     * attribute is taken to be the <em>converter-id</em> with which
     * instances of this class of converter can be instantiated by
     * calling {@link
     * javax.faces.application.Application#createConverter(java.lang.String)}.</p>
     *
     * @return the converter-id
     */ 

    String value() default "";

    /**
     * <p class="changed_added_2_0">The value of this annotation
     * attribute is taken to be the <em>converter-for-class</em> with
     * which instances of this class of converter can be instantiated by
     * calling {@link
     * javax.faces.application.Application#createConverter(java.lang.Class)}.</p>
     *
     * @return the class
     */ 

    Class forClass() default Object.class;


    /**
     * <p class="changed_added_2_3">The value of this annotation attribute is
     * taken to be an indicator that flags whether or not the given converter
     * is a CDI managed converter. </p>
     *
     * @return whether or not this converter is managed by CDI
     */
    
    boolean managed() default false;
}
