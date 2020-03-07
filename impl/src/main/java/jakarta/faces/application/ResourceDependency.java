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

package jakarta.faces.application;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * <p class="changed_added_2_0">
 * Instances of {@link jakarta.faces.component.UIComponent} or {@link jakarta.faces.render.Renderer} that have this
 * annotation (or {@link ResourceDependencies} attached at the class level will automatically have a resource dependency
 * added so that the named resource will be present in user agent's view of the <code>UIViewRoot</code> in which this
 * component or renderer is used.
 * </p>
 *
 * <div class="changed_added_2_0">
 *
 * <p>
 * The default implementation must support attaching this annotation to {@link jakarta.faces.component.UIComponent} or
 * {@link jakarta.faces.render.Renderer} classes. In both cases, the event that precipitates the processing of this
 * annotation is the insertion of a <code>UIComponent</code> instance into the view hierarchy on an initial request for
 * a view. When that event happens, the following action must be taken.
 * </p>
 *
 * <ol>
 * <li>
 * <p>
 * If this annotation is not present on the class in question, no action must be taken.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * Create a {@link jakarta.faces.component.UIOutput} instance by passing <code>jakarta.faces.Output</code>. to
 * {@link Application#createComponent(java.lang.String)}.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * Get the annotation instance from the class and obtain the values of the <em>name</em>, <em>library</em>, and
 * <em>target</em> attributes.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * If <em>library</em> is the empty string, let <em>library</em> be <code>null</code>.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * If <em>target</em> is the empty string, let <em>target</em> be <code>null</code>.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * Obtain the <em>renderer-type</em> for the resource <em>name</em> by passing <em>name</em> to
 * {@link ResourceHandler#getRendererTypeForResourceName}.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * Call <code>setRendererType</code> on the <code>UIOutput</code> instance, passing the <em>renderer-type</em>.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * Obtain the <code>Map</code> of attributes from the <code>UIOutput</code> component by calling
 * {@link jakarta.faces.component.UIComponent#getAttributes}.
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * Store the <em>name</em> into the attributes <code>Map</code> under the key "name".
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * If <em>library</em> is non-<code>null</code>, store it under the key "library".
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * If <em>target</em> is non-<code>null</code>, store it under the key "target".
 * </p>
 * </li>
 *
 * <li>
 * <p>
 * Otherwise, if <em>target</em> is <code>null</code>, call
 * {@link jakarta.faces.component.UIViewRoot#addComponentResource(jakarta.faces.context.FacesContext, jakarta.faces.component.UIComponent)},
 * passing the <code>UIOutput</code> instance as the second argument.
 * </p>
 * </li>
 * </ol>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * <code>
  &#0064;ResourceDependency(library="corporate", name="colorAndMedia.css"),
</code>
 * </pre>
 *
 *
 * </div>
 *
 * @since 2.0
 */
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
@Repeatable(ResourceDependencies.class)
public @interface ResourceDependency {

    /**
     * <p class="changed_added_2_0">
     * The <em>resourceName</em> of the resource pointed to by this <code>ResourceDependency</code>. It is valid to have
     * Jakarta Expression Language Expressions in the value of this attribute, as long as the expression resolves to an
     * instance of the expected type.
     * </p>
     *
     * @return the name.
     */
    public String name();

    /**
     * <p class="changed_added_2_0">
     * The <em>libraryName</em> in which the resource pointed to by this <code>ResourceDependency</code> resides. If not
     * specified, defaults to the empty string. It is valid to have Jakarta Expression Language Expressions in the value of
     * this attribute, as long as the expression resolves to an instance of the expected type.
     * </p>
     *
     * @return the library.
     */
    public String library() default "";

    /**
     * <p class="changed_added_2_0">
     * The value given for this attribute will be passed as the "target" argument to
     * {@link jakarta.faces.component.UIViewRoot#addComponentResource(jakarta.faces.context.FacesContext, jakarta.faces.component.UIComponent, java.lang.String)}.
     * If this attribute is specified,
     * {@link jakarta.faces.component.UIViewRoot#addComponentResource(jakarta.faces.context.FacesContext,jakarta.faces.component.UIComponent)}
     * must be called instead, as described above. It is valid to have Jakarta Expression Language Expressions in the value
     * of this attribute, as long as the expression resolves to an instance of the expected type.
     * </p>
     *
     * @return the target.
     */
    public String target() default "";

}
