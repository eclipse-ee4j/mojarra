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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.Inherited;

/**
 * <p class="changed_added_2_0">The presence of this annotation on a class
 * automatically registers the class with the runtime as a managed bean class.
 * Classes must be scanned for the presence of this annotation at application
 * startup, before any requests have been serviced.</p>
 *
 * <div class="changed_added_2_0">
 *
 * <p>
 * The value of the {@link #name} attribute is taken to be the
 * <em>managed-bean-name</em>. If the value of the <em>name</em>
 * attribute is unspecified or is the empty <code>String</code>, the
 * <em>managed-bean-name</em> is derived from taking the unqualified class name
 * portion of the fully qualified class name and converting the first character
 * to lower case. For example, if the <code>ManagedBean</code> annotation is on
 * a class with the fully qualified class name <code>com.foo.Bean</code>, and
 * there is no
 * <em>name</em> attribute on the annotation, the
 * <em>managed-bean-name</em> is taken to be <code>bean</code>. The fully
 * qualified class name of the class to which this annotation is attached is
 * taken to be the <em>managed-bean-class</em>.</p>
 *
 * <p>
 * The scope of the managed bean is declared using one of {@link
 * NoneScoped}, {@link RequestScoped}, {@link ViewScoped}, {@link
 * SessionScoped}, {@link ApplicationScoped}, or {@link CustomScoped}
 * annotations. If the scope annotations are omitted, the bean must be handled
 * as if the {@link RequestScoped} annotation is present.</p>
 *
 * <p>
 * If the value of the {@link #eager} attribute is <code>true</code>, and the
 * <code>managed-bean-scope</code> value is "application", the runtime must
 * instantiate this class when the application starts. This instantiation and
 * storing of the instance must happen before any requests are serviced. If
 * <em>eager</em> is unspecified or <code>false</code>, or the
 * <code>managed-bean-scope</code> is something other than "application", the
 * default "lazy" instantiation and scoped storage of the managed bean
 * happens.</p>
 *
 * <p>
 * When the runtime processes this annotation, if a managed bean exists whose
 * name is equal to the derived <em>managed-bean-name</em>, a
 * <code>FacesException</code> must be thrown and the application must not be
 * placed in service.</p>
 *
 * <p>
 * A class tagged with this annotation must have a public zero-argument
 * constructor. If such a constructor is not defined on the class, a
 * <code>FacesException</code> must be thrown and the application must not be
 * placed in service.</p>
 *
 * </div>
 *
 * @since 2.0
 * @deprecated This has been replaced by the Managed Beans specification in
 * general and specifically the dependency injection, scopes and naming
 * from the CDI specification. Note that the <em>eager</em> attribute
 * for application scoped beans is replaced specifically by observing
 * the {@code javax.enterprise.context.Initialized} event for
 * {@code javax.enterprise.context.ApplicationScoped}. See 6.7.3 of the CDI
 * spec for further details.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Deprecated
public @interface ManagedBean {

    /**
     * <p class="changed_added_2_0">Taken to be the
     * <code>managed-bean-name</code>. See class documentation for details.</p>
     *
     * @return the managed bean name.
     */
    String name() default "";

    /**
     * <p class="changed_added_2_0">Taken to be the value of the
     * <code>eager</code> attribute of the <code>managed-bean</code>. See class
     * documentation for details.</p>
     *
     * @return the eager attribute of the managed bean.
     */
    boolean eager() default false;
}
