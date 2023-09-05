/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.el.ValueExpression;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import static com.sun.faces.util.Util.EMPTY_STRING;
import static com.sun.faces.util.Util.notNullArgs;

/**
 * <p>
 * <span class="changed_modified_2_0 changed_modified_2_0_rev_a">{@link Converter}</span> implementation for
 * <code>java.lang.Enum</code> (and enum primitive) values.
 * </p>
 *
 * @since 1.2
 */
public class EnumConverter implements Converter<Enum> {

    /**
     * <p>
     * The standard converter id for this converter.
     * </p>
     */
    public static final String CONVERTER_ID = "jakarta.faces.Enum";

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if the conversion to
     * <code>Enum</code> fails. The message format string for this message may optionally include the following
     * placeholders:
     * <ul>
     * <li><code>{0}</code> replaced by the unconverted value.</li>
     * <li><code>{1}</code> replaced by one of the enum constants or the empty string if none can be found.</li>
     * <li><code>{2}</code> replaced by a <code>String</code> whose value is the label of the input component that produced
     * this message.</li>
     * </ul>
     */
    public static final String ENUM_ID = "jakarta.faces.converter.EnumConverter.ENUM";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if the conversion to
     * <code>Enum</code> fails and no target class has been provided. The message format string for this message may
     * optionally include the following placeholders:
     * <ul>
     * <li><code>{0}</code> replaced by the unconverted value.</li>
     * <li><code>{1}</code> replaced by a <code>String</code> whose value is the label of the input component that produced
     * this message.</li>
     * </ul>
     */
    public static final String ENUM_NO_CLASS_ID = "jakarta.faces.converter.EnumConverter.ENUM_NO_CLASS";

    // ----------------------------------------------------- Converter Methods

    /**
     * <p>
     * Convert the <code>value</code> argument to one of the enum constants of the class provided in our constructor. If no
     * target class argument has been provided to the constructor of this instance, throw a <code>ConverterException</code>
     * containing the {@link #ENUM_NO_CLASS_ID} message with proper parameters. If the <code>value</code> argument is
     * <code>null</code> or it has a length of zero, return <code>null</code>. Otherwise, perform the equivalent of
     * <code>Enum.valueOf</code> using target class and <code>value</code> and return the <code>Object</code>. If the
     * conversion fails, throw a <code>ConverterException</code> containing the {@link #ENUM_ID} message with proper
     * parameters.
     * </p>
     *
     * @param context the <code>FacesContext</code> for this request.
     * @param component the <code>UIComponent</code> to which this value will be applied.
     * @param value the String <code>value</code> to be converted to <code>Object</code>.
     * @throws ConverterException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public Enum getAsObject(FacesContext context, UIComponent component, String value) {
        notNullArgs( context , component );

        // If the specified value is null or zero-length, return null
        if ( value == null || value.isBlank() ) {
            return null;
        }

        // detect the return type
        final Class<? extends Enum> enumClass = getExpectedValueType(context,component);

        if (enumClass == null) {
            throw new ConverterException(MessageFactory.getMessage(context, ENUM_NO_CLASS_ID, value, MessageFactory.getLabel(context, component)));
        }

        try {
            return Enum.valueOf(enumClass, value.trim());
        } catch (IllegalArgumentException iae) {
            throw new ConverterException(MessageFactory.getMessage(context, ENUM_ID, value, value, MessageFactory.getLabel(context, component)), iae);
        }
    }

    /**
     * <p class="changed_modified_2_3">
     * Convert the enum constant given by the <code>value</code> argument into a String. If no target class argument has
     * been provided to the constructor of this instance, throw a <code>ConverterException</code> containing the
     * {@link #ENUM_NO_CLASS_ID} message with proper parameters. If the <code>value</code> argument is <code>null</code>,
     * return the empty String. If the value is an instance of the provided target class, return its string value by
     * <span class="changed_added_2_0">casting it to a <code>java.lang.Enum</code> and returning the result of calling the
     * <code>name()</code> method.</span> Otherwise, throw a {@link ConverterException} containing the {@link #ENUM_ID}
     * message with proper parameters.
     * </p>
     *
     * @throws ConverterException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, Enum value) {
        notNullArgs( context , component );

        // If the specified value is null, return the empty string.
        if (value == null) {
            return EMPTY_STRING;
        }

        try {
            return value.name();
        } catch (Exception e) {
            throw new ConverterException(MessageFactory.getMessage(context, ENUM_ID, value, value, MessageFactory.getLabel(context, component)));
        }
    }

    // ----------------------------------------------------------------------------- private methods

    private static <T> Class<T> getExpectedValueType(FacesContext context,UIComponent component) {
        ValueExpression valueExpression = component.getValueExpression("value");
        if (valueExpression != null) {
            return getExpectedType(context,valueExpression);
        } else {
            Object value = component.getAttributes().get("value");
            return value != null ? (Class<T>) value.getClass() : null;
        }
    }

    private static <T> Class<T> getExpectedType(FacesContext context,ValueExpression valueExpression) {
        Class<?> expectedType = valueExpression.getExpectedType();
        if (expectedType == Object.class) {
            expectedType = valueExpression.getType(context.getELContext());
        }

        return (Class<T>) expectedType;
    }

}
