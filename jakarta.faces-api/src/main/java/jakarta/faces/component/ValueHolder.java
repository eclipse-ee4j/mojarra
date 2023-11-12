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

package jakarta.faces.component;

import jakarta.el.ValueExpression;
import jakarta.faces.convert.Converter;

/**
 * <p>
 * <strong class="changed_modified_2_0">ValueHolder</strong> is an interface that may be implemented by any concrete
 * {@link UIComponent} that wishes to support a local value, as well as access data in the model tier via a <em>value
 * expression</em>, and support conversion between String and the model tier data's native data type.
 */
public interface ValueHolder {

    // -------------------------------------------------------------- Properties

    /**
     * <p>
     * Return the local value of this {@link UIComponent} (if any), without evaluating any associated
     * {@link ValueExpression}.
     * </p>
     *
     * @return the local value.
     */
    Object getLocalValue();

    /**
     * <p>
     * Gets the value of this {@link UIComponent}. If validation failed, as indicated by
     * {@link jakarta.faces.context.FacesContext#isValidationFailed} returning <code>true</code>, always return the local
     * value. Otherwise, first, consult the local value property of this component. If non-<code>null</code> return it. If
     * <code>null</code>, see if we have a {@link ValueExpression} for the <code>value</code> property. If so, return the
     * result of evaluating the property, otherwise return <code>null</code>.
     * </p>
     *
     * @return the value.
     */
    Object getValue();

    /**
     * <p>
     * Set the value of this {@link UIComponent} (if any).
     * </p>
     *
     * @param value The new local value
     */
    void setValue(Object value);

    /**
     * <p>
     * Return the {@link Converter} (if any) that is registered for this {@link UIComponent}.
     * </p>
     *
     * @return the converter.
     */
    Converter getConverter();

    /**
     * <p>
     * Set the {@link Converter} (if any) that is registered for this {@link UIComponent}.
     * </p>
     *
     * @param converter New {@link Converter} (or <code>null</code>)
     */
    void setConverter(Converter converter);
}
