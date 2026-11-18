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
package org.eclipse.mojarra.test.issue6177;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * Each input of h:selectManyCheckbox and h:selectOneRadio is started with the f:selectItem or f:selectItems of its
 * item, as the "Rendering the option elements" section of the Listbox renderer requires, which the Checkbox and Radio
 * renderers refer to. So the pass-through attributes of the item component go on its input, with the <code>var</code>
 * of f:selectItems exposing the current member. The Faces TCK requires the pass-through attributes of the select
 * component on each input as well, so each input carries both, those of the item component taking precedence for the
 * same name. The pass-through attributes of a component which produces a group go on the nested table or list of the
 * group.
 *
 * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6177">issue 6177</a>
 */
class Issue6177IT extends BaseIT {

    private static final List<String> SELECT = List.of("s", "s", "s");

    private static final List<String> ITEM = List.of("i1", "on", "off");

    private static final List<String> BOTH = List.of("item", "select", "select");

    private static final List<String> NONE = Arrays.asList(null, null, null);

    /**
     * The input of each item carries the pass-through attributes of the select component and of its item component.
     * The outer table or list carries those of the select component only, and the rows, cells, list items and labels
     * carry none.
     */
    @Test
    void testInputsCarryPassThroughAttributesOfSelectAndItem() {
        open("items.xhtml");

        for (String id : List.of("checks", "checksList", "checksPage", "radios", "radiosList", "radiosPage")) {
            WebElement select = browser.findElement(By.id("form:" + id));
            assertEquals("s", select.getAttribute("data-select"), id);
            assertEquals("select", select.getAttribute("data-both"), id);
            assertEquals(SELECT, valuesOf(id, "input", "data-select"), id);
            assertEquals(ITEM, valuesOf(id, "input", "data-item"), id);
            assertEquals(BOTH, valuesOf(id, "input", "data-both"), id);
            assertEquals(List.of("input", "input", "input"), tagsWith(id, "data-select"), id);
            assertEquals(List.of("input", "input", "input"), tagsWith(id, "data-item"), id);
        }
    }

    /**
     * The pass-through attributes of f:selectItemGroups go on the nested table of each group, with its
     * <code>var</code> exposing the member which produced the group, and not on the inputs. The nested components of
     * f:selectItemGroups contribute none. Those of an f:selectItems whose member is a group go on the nested list of
     * the group and on each input, next to those of the select component.
     */
    @Test
    void testGroupsCarryPassThroughAttributesOfTheirComponent() {
        open("groups.xhtml");

        for (String id : List.of("checksGroups", "radiosGroups")) {
            assertEquals(List.of("x", "y"), valuesOf(id, "table", "data-group"), id);
            assertEquals(NONE, valuesOf(id, "input", "data-group"), id);
            assertEquals(SELECT, valuesOf(id, "input", "data-select"), id);
            assertEquals(List.of(), tagsWith(id, "data-inner"), id);
        }

        assertEquals(List.of("input", "ul", "input", "input"), tagsWith("checksSelectItems", "data-group"));
        assertEquals(List.of("one", "group", "group", "group"), valuesOf("checksSelectItems", "[data-group]", "data-group"));
        assertEquals(SELECT, valuesOf("checksSelectItems", "input", "data-select"));
        assertEquals(List.of("item", "item", "item"), valuesOf("checksSelectItems", "input", "data-both"));
    }

    /**
     * In group mode, the input of each h:selectOneRadio carries its own pass-through attributes and those of the item
     * component which produced its item, also when the item comes from the first radio of the group. Those of the
     * item component take precedence for the same name. An item from a Map value exposes its Map.Entry as
     * <code>var</code>.
     */
    @Test
    void testRadioGroupInputsCarryPassThroughAttributesOfRadioAndItem() {
        open("radioGroup.xhtml");

        assertEquals(List.of("one", "two"), valuesOf("#form input[name='form:group']", "data-select"));
        assertEquals(List.of("on", "off"), valuesOf("#form input[name='form:group']", "data-item"));
        assertEquals(List.of("item", "item"), valuesOf("#form input[name='form:group']", "data-both"));
        assertEquals(List.of("one", "two"), valuesOf("#form input[name='form:mapGroup']", "data-map"));
        assertEquals(List.of("m1", "m2"), valuesOf("#form input[name='form:mapGroup']", "data-key"));
    }

    /**
     * The options and inputs of a Map value are started with the f:selectItems, so that they carry its pass-through
     * attributes, with its <code>var</code> exposing the Map.Entry of the current item. The options do not carry those
     * of the select component, whose outer-most element is the select. The inputs carry both.
     */
    @Test
    void testMapValueItemsCarryPassThroughAttributesOfSelectItems() {
        open("map.xhtml");

        assertEquals(List.of("m1", "m2"), valuesOf("menuMap", "option", "data-key"));
        assertEquals(Arrays.asList(null, null), valuesOf("menuMap", "option", "data-select"));
        assertEquals("s", browser.findElement(By.id("form:menuMap")).getAttribute("data-select"));
        assertEquals(List.of("m1", "m2"), valuesOf("checksMap", "input", "data-key"));
        assertEquals(List.of("s", "s"), valuesOf("checksMap", "input", "data-select"));
    }

    /**
     * With <code>var</code>, the item attributes of f:selectItems apply also to members which are SelectItem instances
     * and to the Map.Entry of each item of a Map value. Validation sees the same values as the rendered options, so a
     * postback with them passes, also for the inputs of h:selectManyCheckbox. An item attribute which is not set keeps
     * the value of the SelectItem, also when <code>var</code> is set for the pass-through attributes only.
     */
    @Test
    void testVarAppliesToSelectItemMembersAndMapEntries() {
        open("varItems.xhtml");

        assertEquals(List.of("one", "two"), valuesOf("menuSelectItems", "option", "value"));
        assertEquals(List.of("one", "two"), textsOf("menuSelectItems"));
        assertEquals(List.of("m1", "m2"), valuesOf("menuMapEntries", "option", "value"));
        assertEquals(List.of("1", "2"), textsOf("menuMapEntries"));
        assertEquals(List.of("1", "2"), valuesOf("menuPassThroughOnly", "option", "value"));
        assertEquals(List.of("one", "two"), textsOf("menuPassThroughOnly"));
        assertEquals(List.of("one", "two"), valuesOf("menuPassThroughOnly", "option", "data-label"));

        new Select(browser.findElement(By.id("form:menuSelectItems"))).selectByValue("two");
        new Select(browser.findElement(By.id("form:menuMapEntries"))).selectByValue("m2");
        assertEquals(List.of("m1", "m2"), valuesOf("checksMapEntries", "input", "value"));
        browser.findElement(By.cssSelector(inside("checksMapEntries", "input[value=m1]"))).click();
        guardHttp(browser.findElement(By.id("form:submit"))::click);

        assertEquals("", browser.findElement(By.id("form:messages")).getText());
        assertEquals("two m2 [m1]", browser.findElement(By.id("form:result")).getText());
    }

    private List<String> textsOf(String id) {
        return browser.findElements(By.cssSelector(inside(id, "option"))).stream().map(WebElement::getText).collect(toList());
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
