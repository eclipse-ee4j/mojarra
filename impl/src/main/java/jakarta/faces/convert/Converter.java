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

package jakarta.faces.convert;

import jakarta.faces.component.StateHolder;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * <strong class="changed_modified_2_0 changed_modified_2_2 changed_modified_2_3">Converter</strong> is an interface
 * describing a Java class that can perform Object-to-String and String-to-Object conversions between model data objects
 * and a String representation of those objects that is suitable for rendering.
 * </p>
 *
 * <p>
 * {@link Converter} implementations must have a zero-arguments public constructor. In addition, if the
 * {@link Converter} class wishes to have configuration property values saved and restored with the component tree, the
 * implementation must also implement {@link StateHolder}.
 * </p>
 *
 * <p>
 * Starting with version 1.2 of the specification, an exception to the above zero-arguments constructor requirement has
 * been introduced. If a converter has a single argument constructor that takes a <code>Class</code> instance and the
 * <code>Class</code> of the data to be converted is known at converter instantiation time, this constructor must be
 * used to instantiate the converter instead of the zero-argument version. This enables the per-class conversion of Java
 * enumerated types.
 * </p>
 *
 * <p>
 * If any <code>Converter</code> implementation requires a <code>java.util.Locale</code> to perform its job, it must
 * obtain that <code>Locale</code> from the {@link jakarta.faces.component.UIViewRoot} of the current
 * {@link FacesContext}, unless the <code>Converter</code> maintains its own <code>Locale</code> as part of its state.
 * </p>
 *
 * <p class="changed_added_2_0">
 * If the class implementing <code>Converter</code> has a {@link jakarta.faces.application.ResourceDependency}
 * annotation, the action described in <code>ResourceDependency</code> must be taken when
 * {@link jakarta.faces.component.ValueHolder#setConverter} is called. If the class implementing <code>Converter</code>
 * has a {@link jakarta.faces.application.ResourceDependencies} annotation, the action described in
 * <code>ResourceDependencies</code> must be taken when {@link jakarta.faces.component.ValueHolder#setConverter} is
 * called.
 * </p>
 *
 * @param <T> The generic type of object value to convert.
 */

public interface Converter<T> {

    /**
     * <p>
     * <span class="changed_modified_2_3">Convert</span> the specified string value, which is associated with the specified
     * {@link UIComponent}, into a model data object that is appropriate for being stored during the
     * <em class="changed_modified_2_3">Process Validations</em> phase of the request processing lifecycle.
     * </p>
     *
     * @param context {@link FacesContext} for the request being processed
     * @param component {@link UIComponent} with which this model object value is associated
     * @param value String value to be converted (may be <code>null</code>)
     * @return <code>null</code> if the value to convert is <code>null</code>, otherwise the result of the conversion
     * @throws ConverterException if conversion cannot be successfully performed
     * @throws NullPointerException if <code>context</code> or <code>component</code> is <code>null</code>
     */
    T getAsObject(FacesContext context, UIComponent component, String value);

    /**
     * <p>
     * Convert the specified model object value, which is associated with the specified {@link UIComponent}, into a String
     * that is suitable for being included in the response generated during the <em>Render Response</em> phase of the
     * request processing lifeycle.
     * </p>
     *
     * @param context {@link FacesContext} for the request being processed
     * @param component {@link UIComponent} with which this model object value is associated
     * @param value Model object value to be converted (may be <code>null</code>)
     * @return a zero-length String if value is <code>null</code>, otherwise the result of the conversion
     * @throws ConverterException if conversion cannot be successfully performed
     * @throws NullPointerException if <code>context</code> or <code>component</code> is <code>null</code>
     */
    String getAsString(FacesContext context, UIComponent component, T value);

    /**
     * <p class="changed_added_2_2">
     * If this param is set, and calling toLowerCase().equals("true") on a String representation of its value returns true,
     * Application.createConverter() must guarantee that the default for the timezone of all
     * jakarta.faces.convert.DateTimeConverter instances must be equal to TimeZone.getDefault() instead of "GMT".
     * </p>
     *
     * @since 2.0
     */
    String DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE_PARAM_NAME = "jakarta.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE";

}
