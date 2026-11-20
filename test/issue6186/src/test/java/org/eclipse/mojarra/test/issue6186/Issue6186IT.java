/*
 * Copyright (c) Contributors to Eclipse Foundation.
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
package org.eclipse.mojarra.test.issue6186;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * The pass-through attributes of a group go on its outer-most element: the optgroup, or the nested table or list.
 * Each option or input in a group of f:selectItemGroups is started with the nested f:selectItem or f:selectItems
 * which produced its item, so that it carries the pass-through attributes of that component. While these are
 * evaluated, the <code>var</code> of f:selectItemGroups stays bound to the member which produced the group, and the
 * <code>var</code> of the nested f:selectItems refers to its current element. Each option or input in a group of
 * f:selectItem or f:selectItems carries none of the pass-through attributes of that component.
 *
 * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6186">issue 6186</a>
 */
class Issue6186IT extends BaseIT {

    private static final List<String> SKUS = List.of("all", "a1", "p1", "all", "l1");

    private static final List<String> CATEGORIES = List.of("fruit", "fruit", "fruit", "veg", "veg");

    private static final List<String> GROUPS = List.of("fruit", "veg");

    /**
     * Each option and input of f:selectItemGroups carries the pass-through attributes of its nested component, with
     * both <code>var</code> bound. The optgroup and the nested table carry those of f:selectItemGroups only. Each input
     * carries those of the select component as well, as the Faces TCK requires.
     */
    @Test
    void testItemsOfSelectItemGroupsCarryPassThroughAttributesOfTheirNestedComponent() {
        open("groups.xhtml");

        assertEquals(SKUS, valuesOf("menu", "option", "data-sku"));
        assertEquals(CATEGORIES, valuesOf("menu", "option", "data-category"));
        assertEquals(GROUPS, valuesOf("menu", "optgroup", "data-group"));
        assertEquals(List.of("option", "option", "option", "option", "option"), tagsWith("menu", "data-sku"));

        for (String id : List.of("checks", "radios")) {
            assertEquals(SKUS, valuesOf(id, "input", "data-sku"), id);
            assertEquals(CATEGORIES, valuesOf(id, "input", "data-category"), id);
            assertEquals(List.of("s", "s", "s", "s", "s"), valuesOf(id, "input", "data-select"), id);
            assertEquals(GROUPS, valuesOf(id, "table", "data-group"), id);
            assertEquals(List.of("input", "input", "input", "input", "input"), tagsWith(id, "data-sku"), id);
        }
    }

    /**
     * The <code>var</code> of f:selectItemGroups is bound for the pass-through attributes of its nested components
     * also when f:selectItemGroups has none of its own.
     */
    @Test
    void testItemsOfSelectItemGroupsWithoutOwnPassThroughAttributesSeeItsVar() {
        open("nestedOnly.xhtml");

        for (String id : List.of("menu", "checks", "radios")) {
            String selector = "menu".equals(id) ? "option" : "input";
            assertEquals(SKUS, valuesOf(id, selector, "data-sku"), id);
            assertEquals(CATEGORIES, valuesOf(id, selector, "data-category"), id);
        }
    }

    /**
     * The options and inputs inside a group of f:selectItems carry none of its pass-through attributes, and the inputs
     * carry those of the select component only. The items after the group carry those of f:selectItems again, next to
     * those of the select component, which they take precedence over for the same name.
     */
    @Test
    void testItemsAroundGroupOfSelectItemsCarryPassThroughAttributesOfSelectItems() {
        open("itemsAroundGroup.xhtml");

        assertEquals(Arrays.asList("one", "group", null, null, "four"), valuesOf(inside("menu", "optgroup") + ", " + inside("menu", "option"), "data-item"));
        assertEquals(Arrays.asList("one", null, null, "four"), valuesOf("checks", "input", "data-item"));
        assertEquals(List.of("item", "select", "select", "item"), valuesOf("checks", "input", "data-both"));
        assertEquals(List.of("input", "ul", "input"), tagsWith("checks", "data-item"));
    }

    /**
     * Returns the given attribute of each element matching the given selector inside the component with the given id.
     */
    private List<String> valuesOf(String id, String selector, String attribute) {
        return valuesOf(inside(id, selector), attribute);
    }

    private List<String> valuesOf(String selector, String attribute) {
        return browser.findElements(By.cssSelector(selector)).stream().map(element -> element.getAttribute(attribute)).collect(toList());
    }

    /**
     * Returns the tag name of each element with the given attribute inside the component with the given id.
     */
    private List<String> tagsWith(String id, String attribute) {
        return browser.findElements(By.cssSelector(inside(id, "[" + attribute + "]"))).stream().map(WebElement::getTagName).collect(toList());
    }

    private static String inside(String id, String selector) {
        return "#form\\:" + id + " " + selector;
    }
}
