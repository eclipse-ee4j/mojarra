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
package org.eclipse.mojarra.test.issue6176;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

/**
 * The <code>var</code> of f:selectItems and of f:selectItemGroups exposes the current member of the collection
 * referenced by <code>value</code>, as the VDL documentation of their <code>var</code> attributes requires. This holds
 * too for their pass-through attributes. Those of f:selectItems and of f:selectItemGroups go on every option and
 * optgroup they produce, as their outer-most elements, and not on the options inside such an optgroup.
 *
 * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6176">issue 6176</a>
 */
class Issue6176IT extends BaseIT {

    private static final List<String> FLAGS = List.of("on", "off");

    /**
     * When <code>itemValue</code> points to a property of the member, <code>var</code> is still the member, not the
     * item value.
     */
    @Test
    void testVarIsMemberWhenItemValueIsPropertyOfMember() {
        open("itemValue.xhtml");
        assertEquals(FLAGS, flagsOf("menuItemValue"));
        assertEquals(FLAGS, flagsOf("listboxItemValue"));
    }

    /**
     * Without <code>itemValue</code>, <code>var</code> is the member, which is also the item value.
     */
    @Test
    void testVarIsMemberWithoutItemValue() {
        open("element.xhtml");
        assertEquals(List.of("a", "b"), flagsOf("menuElement"));
    }

    /**
     * When the member is a <code>SelectItem</code>, <code>var</code> is that <code>SelectItem</code>. When the member is
     * a <code>SelectItemGroup</code>, <code>var</code> is that group for its optgroup, and the options of the group
     * carry none of the pass-through attributes of f:selectItems.
     */
    @Test
    void testVarIsSelectItemMember() {
        open("selectItems.xhtml");
        assertEquals(Arrays.asList("one", "group", null, null), flagsOf("menuSelectItems"));
    }

    /**
     * The <code>var</code> of f:selectItemGroups is the member which produced the group, also when it has an
     * <code>itemValue</code>, which f:selectItemGroups does not define. The pass-through attributes
     * of f:selectItemGroups go on the optgroup of each group only, not on its options.
     */
    @Test
    void testSelectItemGroupsVarIsMemberOnOptgroupOnly() {
        open("groups.xhtml");
        assertEquals(Arrays.asList("x", null, null, "y", null), flagsOf("menuGroups"));
    }

    /**
     * Returns the data-flag of each optgroup and option of the given select, in document order.
     */
    private List<String> flagsOf(String id) {
        String select = "#form\\:" + id;
        return browser.findElements(By.cssSelector(select + " optgroup, " + select + " option")).stream().map(element -> element.getAttribute("data-flag")).collect(toList());
    }
}
