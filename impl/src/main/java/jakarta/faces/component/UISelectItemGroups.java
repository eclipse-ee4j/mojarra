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

package jakarta.faces.component;

import static com.sun.faces.util.Util.coalesce;
import static com.sun.faces.util.Util.stream;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.sun.faces.util.ScopedRunner;

import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;

/**
 * <p class="changed_added_4_0">
 * <strong>UISelectItemGroups</strong> is a component that may be nested inside a {@link UISelectMany} or {@link UISelectOne} component, and causes the addition
 * of one or more {@link SelectItemGroup} of one or more {@link SelectItem} instances to the list of available options in the parent component. This component
 * accepts only children of type {@link UISelectItems} or {@link UISelectItem}. The <code>value</code> attribute of this component, set either directly, or
 * acquired indirectly via a {@link jakarta.el.ValueExpression}, can be an array or {@link Iterable} of items of any type which is acceptable by the
 * <code>value</code> attribute of any nested {@link UISelectItems} or {@link UISelectItem} component.
 * </p>
 *
 * @since 4.0
 */

public class UISelectItemGroups extends UISelectItems {

    // -------------------------------------------------------------------------------------------------------------------------------------- Manifest Constants

    /**
     * <p>
     * The standard component type for this component.
     * </p>
     */
    public static final String COMPONENT_TYPE = "jakarta.faces.SelectItemGroups";

    // ---------------------------------------------------------------------------------------------------------------------------------- ValueHolder Properties

    /**
     * <p>
     * Iterate over the <code>value</code> attribute and wrap each item in a new {@link SelectItemGroup} instance whereby the item is exposed as a request
     * attribute under the key specified by the <code>var</code> property. This must allow any nested {@link UISelectItems} or {@link UISelectItem} component
     * to access the item via their attributes. Finally return these {@link SelectItemGroup} instances as an ordered collection.
     * </p>
     */
    @Override
    public Object getValue() {
        List<SelectItemGroup> groups = new ArrayList<>();

        createSelectItems(this, super.getValue(), SelectItemGroup::new, selectItemGroup -> {
            List<SelectItem> items = new ArrayList<>();

            for (UIComponent child : getChildren()) {
                if (child instanceof UISelectItems) {
                    createSelectItems(child, ((UISelectItems) child).getValue(), SelectItem::new, items::add);
                }
                else if (child instanceof UISelectItem) {
                    items.add(createSelectItem(child, null, SelectItem::new));
                }
            }

            selectItemGroup.setSelectItems(items);
            groups.add(selectItemGroup);
        });

        return groups;
    }

    private <S extends SelectItem> void createSelectItems(UIComponent component, Object values, Supplier<S> supplier, Consumer<S> callback) {
        Map<String, Object> attributes = component.getAttributes();
        String var = coalesce((String) attributes.get("var"), "item");
        stream(values).forEach(value -> new ScopedRunner(getFacesContext()).with(var, value).invoke(() -> callback.accept(createSelectItem(component, getItemValue(attributes, value), supplier))));
    }

    private static <S extends SelectItem> S createSelectItem(UIComponent component, Object value, Supplier<S> supplier) {
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
