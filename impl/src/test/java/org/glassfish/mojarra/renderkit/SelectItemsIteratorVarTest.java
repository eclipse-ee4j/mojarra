/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.mojarra.renderkit;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jakarta.el.ValueExpression;
import jakarta.faces.component.UISelectItem;
import jakarta.faces.component.UISelectItemGroups;
import jakarta.faces.component.UISelectItems;
import jakarta.faces.component.UISelectMany;
import jakarta.faces.model.SelectItem;
import jakarta.faces.model.SelectItemGroup;

import org.glassfish.mojarra.junit.JUnitFacesTestCaseBase;
import org.junit.jupiter.api.Test;

/**
 * The <code>var</code> of a {@link UISelectItems} in the {@link SelectItemsIterator} of the renderers. The iterator which validation uses is tested for the
 * same contract in the API.
 */
class SelectItemsIteratorVarTest extends JUnitFacesTestCaseBase {

    /**
     * The current element of the iterator is the member of the value of a UISelectItems which produced the item last returned: the member of an array or a
     * collection, whether it is a SelectItem or not, the member of a UISelectItemGroups which produced the group, also when it has an itemValue, and the
     * SelectItem of a single-valued UISelectItems or of a UISelectItem, and the Map.Entry of a Map value.
     */
    @Test
    void currentElementIsTheMemberWhichProducedTheItem() {
        SelectItem selectItem = new SelectItem("one");
        Object plain = Integer.valueOf(2);
        SelectItem single = new SelectItem("three");
        SelectItem child = new SelectItem("four");
        Object groupMember = "six";

        UISelectMany selectMany = new UISelectMany();
        selectMany.getChildren().add(selectItems(new Object[] { selectItem, plain }));
        selectMany.getChildren().add(selectItems(List.of(selectItem, plain)));
        selectMany.getChildren().add(selectItems(single));
        UISelectItem uiSelectItem = new UISelectItem();
        uiSelectItem.setValue(child);
        selectMany.getChildren().add(uiSelectItem);
        selectMany.getChildren().add(selectItems(Map.of("five", "5")));
        UISelectItemGroups uiSelectItemGroups = new UISelectItemGroups();
        uiSelectItemGroups.setValue(List.of(groupMember));
        uiSelectItemGroups.getAttributes().put("itemValue", "eight");
        uiSelectItemGroups.getChildren().add(selectItems(List.of("seven")));
        selectMany.getChildren().add(uiSelectItemGroups);

        SelectItemsIterator<SelectItem> iter = new SelectItemsIterator<>(facesContext, selectMany);
        List<Object> elements = new ArrayList<>();
        while (iter.hasNext()) {
            iter.next();
            elements.add(iter.currentElement());
        }

        assertEquals(Arrays.asList(selectItem, plain, selectItem, plain, single, child, Map.entry("five", "5"), groupMember), elements);
    }

    /**
     * With <code>var</code>, the item attributes of a UISelectItems apply also to a member which is a SelectItem, to a single-valued SelectItem and to a
     * Map.Entry of a Map value. Each attribute which is not set keeps the value of the SelectItem, or the value and key of the Map.Entry. A SelectItemGroup
     * member is kept as is.
     */
    @Test
    void varAppliesToSelectItemAndMapEntry() {
        SelectItem member = new SelectItem("1", "one", "first", true, false, true);
        SelectItemGroup group = new SelectItemGroup("group");

        UISelectMany selectMany = new UISelectMany();
        UISelectItems labeled = selectItems(List.of(member, group));
        labeled.getAttributes().put("var", "i");
        labeled.setValueExpression("itemLabel", expression("#{i.label}-label"));
        selectMany.getChildren().add(labeled);
        UISelectItems plain = selectItems(member);
        plain.getAttributes().put("var", "i");
        selectMany.getChildren().add(plain);
        UISelectItems keyed = selectItems(Map.of("k", "v"));
        keyed.getAttributes().put("var", "e");
        keyed.setValueExpression("itemValue", expression("#{e.key}"));
        selectMany.getChildren().add(keyed);
        UISelectItems entries = selectItems(Map.of("k", "v"));
        entries.getAttributes().put("var", "e");
        selectMany.getChildren().add(entries);

        List<String> expected = List.of(
            "1|one-label|first|true|false|true", "group", "1|one|first|true|false|true", "k|k|null|false|true|false", "v|k|null|false|true|false"
        );
        assertEquals(expected, describe(new SelectItemsIterator<>(facesContext, selectMany)));
    }

    /**
     * With <code>var</code>, each item gets the no selection option of its own member: that of a SelectItem member, and none for a plain member after it.
     * <code>noSelectionValue</code> marks the item with that value, and a SelectItem member without value is no match for it.
     */
    @Test
    void varKeepsNoSelectionOptionPerMember() {
        UISelectMany selectMany = new UISelectMany();
        UISelectItems mixed = selectItems(List.of(new SelectItem("", "Select", null, false, true, true), "a", "b"));
        mixed.getAttributes().put("var", "i");
        selectMany.getChildren().add(mixed);

        assertEquals(
            List.of("|Select|null|false|true|true", "a|a|null|false|true|false", "b|b|null|false|true|false"),
            describe(new SelectItemsIterator<>(facesContext, selectMany))
        );

        UISelectMany withValue = new UISelectMany();
        UISelectItems marked = selectItems(Arrays.asList(new SelectItem(null, "Select"), "x", "y"));
        marked.getAttributes().put("var", "i");
        marked.getAttributes().put("noSelectionValue", "x");
        withValue.getChildren().add(marked);

        assertEquals(
            List.of("null|Select|null|false|true|false", "x|x|null|false|true|true", "y|y|null|false|true|false"),
            describe(new SelectItemsIterator<>(facesContext, withValue))
        );
    }

    private ValueExpression expression(String expression) {
        return application.getExpressionFactory().createValueExpression(facesContext.getELContext(), expression, Object.class);
    }

    private static List<String> describe(Iterator<SelectItem> items) {
        List<String> descriptions = new ArrayList<>();
        while (items.hasNext()) {
            SelectItem item = items.next();
            descriptions.add(
                item instanceof SelectItemGroup
                    ? item.getLabel()
                    : item.getValue() + "|" + item.getLabel() + "|" + item.getDescription() + "|" + item.isDisabled() + "|" + item.isEscape() + "|"
                        + item.isNoSelectionOption()
            );
        }
        return descriptions;
    }

    private static UISelectItems selectItems(Object value) {
        UISelectItems items = new UISelectItems();
        items.setValue(value);
        return items;
    }

}
