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

import java.util.Iterator;

import jakarta.el.ELException;
import jakarta.el.ExpressionFactory;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;

import static com.sun.faces.util.CollectionsUtils.asIterator;

/**
 * Contains common utility methods used by both {@link UISelectOne} and {@link UISelectMany}.
 */
class SelectUtils {

    // ------------------------------------------------------------ Constructors

    private SelectUtils() {
    }

    // ------------------------------------------------- Package Private Methods

    /**
     * <p>
     * Return <code>true</code> if the specified value matches one of the available options, performing a recursive search
     * if if a {@link jakarta.faces.model.SelectItemGroup} instance is detected.
     * </p>
     *
     * @param ctx {@link FacesContext} for the current request
     * @param value {@link UIComponent} value to be tested
     * @param items Iterator over the {@link jakarta.faces.model.SelectItem}s to be checked
     * @param converter the {@link Converter} associated with this component
     */
    static boolean matchValue(FacesContext ctx, UIComponent component, Object value, Iterator<SelectItem> items, Converter converter) {

        while (items.hasNext()) {
            SelectItem item = items.next();
            if (item instanceof SelectItemGroup) {
                SelectItem[] subitems = ((SelectItemGroup) item).getSelectItems();
                if (subitems != null && subitems.length > 0) {
                    if (matchValue(ctx, component, value, asIterator(subitems), converter)) {
                        return true;
                    }
                }
            } else {
                Object compareValue = null;

                try {
                    compareValue = doConversion(ctx, component, item, value, converter);
                } catch (IllegalStateException ise) {
                    continue;
                }

                if (null == compareValue && null == value) {
                    return true;
                }

                if (value.equals(compareValue)) {
                    return true;
                }
            }
        }
        return false;

    }

    /**
     * Returns true iff component has a {@link UISelectItem} child whose itemValue exactly matches the argument value
     *
     * @param ctx
     * @param component
     * @param value
     * @param items
     * @return
     */

    static boolean valueIsNoSelectionOption(FacesContext ctx, UIComponent component, Object value, Iterator<SelectItem> items, Converter converter) {
        boolean result = false;

        while (items.hasNext()) {
            SelectItem item = items.next();
            if (item instanceof SelectItemGroup) {
                SelectItem[] subitems = ((SelectItemGroup) item).getSelectItems();
                if (subitems != null && subitems.length > 0) {
                    if (valueIsNoSelectionOption(ctx, component, value, asIterator(subitems), converter)) {
                        result = true;
                        break;
                    }
                }
            } else {
                Object compareValue = null;

                try {
                    compareValue = doConversion(ctx, component, item, value, converter);
                } catch (IllegalStateException ise) {
                    continue;
                }

                if (null == compareValue && null == value && item.isNoSelectionOption()) {
                    result = true;
                    break;
                } else if (value.equals(compareValue) && item.isNoSelectionOption()) {
                    result = true;
                    break;
                }
            }

        }

        return result;
    }

    private static Object doConversion(FacesContext ctx, UIComponent component, SelectItem item, Object value, Converter converter)
            throws IllegalStateException {
        Object itemValue = item.getValue();
        if (itemValue == null && value == null) {
            return null;
        }
        if (value == null ^ itemValue == null) {
            throw new IllegalStateException("Either value was null, or itemValue was null, but not both.");
        }
        Object compareValue;
        if (converter == null) {
            compareValue = coerceToModelType(ctx, itemValue, value.getClass());
        } else {
            compareValue = itemValue;
            if (compareValue instanceof String && !(value instanceof String)) {
                // type mismatch between the time and the value we're
                // comparing. Invoke the Converter.
                compareValue = converter.getAsObject(ctx, component, (String) compareValue);
            }
        }

        return compareValue;
    }

    /**
     * Coerce the provided value to the specified type using Jakarta Expression Language coercion.
     *
     * @param ctx the {@link FacesContext} for the current request
     * @param value the value to coerce
     * @param toType the type <code>value</code> should be coerced to
     *
     * @return the result of the Jakarta Expression Language coersion
     *
     * @see ExpressionFactory#coerceToType(Object, Class)
     */
    private static Object coerceToModelType(FacesContext ctx, Object value, Class<?> toType) {

        Object newValue;
        try {
            ExpressionFactory ef = ctx.getApplication().getExpressionFactory();
            newValue = ef.coerceToType(value, toType);
        } catch (ELException | IllegalArgumentException ele) {
            // If coerceToType fails, per the docs it should throw
            // an ELException, however, GF 9.0 and 9.0u1 will throw
            // an IllegalArgumentException instead (see GF issue 1527).
            newValue = value;
        }

        return newValue;

    }

}
