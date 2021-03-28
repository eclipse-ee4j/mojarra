/*
 * Copyright (c) 2021 Contributors to Eclipse Foundation.
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

package com.sun.faces.util;

import static com.sun.faces.util.Util.coalesce;
import static com.sun.faces.util.Util.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UISelectItem;
import jakarta.faces.component.UISelectItemGroup;
import jakarta.faces.component.UISelectItemGroups;
import jakarta.faces.component.UISelectItems;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;

/**
 * Helper for {@link UISelectItemGroup} and {@link UISelectItemGroups}
 */
public class SelectItemUtils {

    public static List<SelectItem> collectSelectItems(FacesContext context, UIComponent component) {
        List<SelectItem> items = new ArrayList<>();

        for (UIComponent child : component.getChildren()) {
            if (child instanceof UISelectItems) {
                createSelectItems(context, child, ((UISelectItems) child).getValue(), SelectItem::new, items::add);
            }
            else if (child instanceof UISelectItem) {
                items.add(createSelectItem(child, null, SelectItem::new));
            }
        }
        return items;
    }

    public static <S extends SelectItem> void createSelectItems(FacesContext context, UIComponent component, Object values, Supplier<S> supplier, Consumer<S> callback) {
        Map<String, Object> attributes = component.getAttributes();
        String var = coalesce((String) attributes.get("var"), "item");
        stream(values).forEach(value -> new ScopedRunner(context).with(var, value).invoke(() -> callback.accept(createSelectItem(component, getItemValue(attributes, value), supplier))));
    }

    public static <S extends SelectItem> S createSelectItem(UIComponent component, Object value, Supplier<S> supplier) {
        Map<String, Object> attributes = component.getAttributes();
        Object itemValue = getItemValue(attributes, value);
        Object itemLabel = attributes.get("itemLabel");
        Object itemEscaped = coalesce(attributes.get("itemEscaped"), attributes.get("itemLabelEscaped")); // f:selectItem || f:selectItems -- TODO: this should be aligned in their APIs.
        Object itemDisabled = attributes.get("itemDisabled");

        S selectItem = supplier.get();
        selectItem.setValue(itemValue);
        selectItem.setLabel(String.valueOf(itemLabel != null ? itemLabel : selectItem.getValue()));
        selectItem.setEscape(itemEscaped == null || Boolean.parseBoolean(itemEscaped.toString()));
        selectItem.setDisabled(itemDisabled != null && Boolean.parseBoolean(itemDisabled.toString()));
        return selectItem;
    }

    /**
     * Returns item value attribute, taking into account any value expression which actually evaluates to null.
     */
    private static Object getItemValue(Map<String, Object> attributes, Object defaultValue) {
        Object itemValue = attributes.get("itemValue");
        return itemValue != null || attributes.containsKey("itemValue") ? itemValue : defaultValue;
    }

}