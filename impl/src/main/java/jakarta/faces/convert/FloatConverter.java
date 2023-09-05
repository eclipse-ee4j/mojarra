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

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import static com.sun.faces.util.Util.EMPTY_STRING;
import static com.sun.faces.util.Util.notNullArgs;

/**
 * <p>
 * {@link Converter} implementation for <code>java.lang.Float</code> (and float primitive) values.
 * </p>
 */

public class FloatConverter implements Converter<Float> {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard converter id for this converter.
     * </p>
     */
    public static final String CONVERTER_ID = "jakarta.faces.Float";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if the conversion to
     * <code>Float</code> fails. The message format string for this message may optionally include the following
     * placeholders:
     * <ul>
     * <li><code>{0}</code> replaced by the unconverted value.</li>
     * <li><code>{1}</code> replaced by an example value.</li>
     * <li><code>{2}</code> replaced by a <code>String</code> whose value is the label of the input component that produced
     * this message.</li>
     * </ul>
     */
    public static final String FLOAT_ID = "jakarta.faces.converter.FloatConverter.FLOAT";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if the conversion of the
     * <code>Float</code> value to <code>String</code> fails. The message format string for this message may optionally
     * include the following placeholders:
     * <ul>
     * <li><code>{0}</code> relaced by the unconverted value.</li>
     * <li><code>{1}</code> replaced by a <code>String</code> whose value is the label of the input component that produced
     * this message.</li>
     * </ul>
     */
    public static final String STRING_ID = "jakarta.faces.converter.STRING";

    // ------------------------------------------------------- Converter Methods

    /**
     * @throws ConverterException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public Float getAsObject(FacesContext context, UIComponent component, String value) {
        notNullArgs( context , component );

        // If the specified value is null or blank, return null
        if ( value == null || value.isBlank() ) {
            return null;
        }

        try {
            return Float.valueOf(value.trim());
        } catch (NumberFormatException nfe) {
            throw new ConverterException(MessageFactory.getMessage(context, FLOAT_ID, value, "2000000000", MessageFactory.getLabel(context, component)), nfe);
        } catch (Exception e) {
            throw new ConverterException(e);
        }
    }

    /**
     * @throws ConverterException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, Float value) {
        notNullArgs( context , component );

        // If the specified value is null, return a zero-length String
        if (value == null) {
            return EMPTY_STRING;
        }

        try {
            return Float.toString(value);
        } catch (Exception e) {
            throw new ConverterException(MessageFactory.getMessage(context, STRING_ID, value, MessageFactory.getLabel(context, component)), e);
        }
    }

}
