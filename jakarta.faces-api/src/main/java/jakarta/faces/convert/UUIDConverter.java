/*
 * Copyright (c) Oracle and/or its affiliates. All rights reserved.
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

import java.util.UUID;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

/**
 * <p>
 * {@link Converter} implementation for <code>java.util.UUID</code> values.
 * </p>
 * @since 4.1
 */
public class UUIDConverter implements Converter<UUID> {

    // ------------------------------------------------------ Manifest Constants

    /**
     * <p>
     * The standard converter id for this converter.
     * </p>
     */
    public static final String CONVERTER_ID = "jakarta.faces.UUID";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if the conversion to <code>UUID</code> fails.
     * The message format string for this message may optionally include the following placeholders:
     * <ul>
     * <li><code>{0}</code> replaced by the unconverted value.</li>
     * <li><code>{1}</code> replaced by an example value.</li>
     * <li><code>{2}</code> replaced by a <code>String</code> whose value is the label of the input component that produced this message.</li>
     * </ul>
     */
    public static final String UUID_ID = "jakarta.faces.converter.UUIDConverter.UUID";

    /**
     * <p>
     * The message identifier of the {@link jakarta.faces.application.FacesMessage} to be created if the conversion of the <code>UUID</code> value to <code>String</code> fails.
     * The message format string for this message may optionally include the following placeholders:
     * <ul>
     * <li><code>{0}</code> relaced by the unconverted value.</li>
     * <li><code>{1}</code> replaced by a <code>String</code> whose value is the label of the input component that produced this message.</li>
     * </ul>
     */
    public static final String STRING_ID = "jakarta.faces.converter.STRING";

    // ------------------------------------------------------- Converter Methods

    /**
     * @throws ConverterException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public UUID getAsObject(FacesContext context, UIComponent component, String value) {

        if (context == null || component == null) {
            throw new NullPointerException();
        }

        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new ConverterException(MessageFactory.getMessage(context, UUID_ID, value, "29573f55-4254-4afa-9ca6-6b5ae6c7ab6e", MessageFactory.getLabel(context, component)), e);
        } catch (Exception e) {
            throw new ConverterException(e);
        }
    }

    /**
     * @throws ConverterException {@inheritDoc}
     * @throws NullPointerException {@inheritDoc}
     */
    @Override
    public String getAsString(FacesContext context, UIComponent component, UUID value) {

        if (context == null || component == null) {
            throw new NullPointerException();
        }

        if (value == null) {
            return "";
        }

        try {
            return value.toString();
        } catch (Exception e) {
            throw new ConverterException(MessageFactory.getMessage(context, STRING_ID, value, MessageFactory.getLabel(context, component)), e);
        }
    }
}
