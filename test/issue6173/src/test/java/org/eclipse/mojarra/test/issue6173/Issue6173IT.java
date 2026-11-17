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
package org.eclipse.mojarra.test.issue6173;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * The pass-through attributes of a component must be rendered on the outer-most markup element for the component, as
 * the overview of the Standard HTML RenderKit requires in "Rendering Pass Through Attributes". The inner elements which
 * a renderer starts for its component, and the wrappers which it starts around a facet, must not repeat them.
 *
 * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6173">issue 6173</a>
 */
class Issue6173IT extends BaseIT {

    private static final List<String> TABLE = List.of("table");

    private static final List<String> SPAN = List.of("span");

    private static final List<String> UL = List.of("ul");

    private static final List<String> INPUT = List.of("input");

    private static final List<String> TABLE_AND_INPUTS = List.of("table", "input", "input", "input");

    private static final List<String> UL_AND_INPUTS = List.of("ul", "input", "input", "input");

    @BeforeEach
    void openPage() {
        open("issue6173.xhtml");
    }

    /**
     * The pass-through attributes of h:panelGrid go on its table only, and those of a header or footer facet on the
     * facet's own markup only.
     */
    @Test
    void testPanelGridRendersPassThroughAttributesOnOuterMostElementOnly() {
        assertEquals(TABLE, elementsWith("data-grid"));
        assertEquals(SPAN, elementsWith("data-grid-header"));
        assertEquals(SPAN, elementsWith("data-grid-footer"));
        assertEquals(TABLE, elementsWith("data-empty-grid"));
    }

    /**
     * The pass-through attributes of h:dataTable go on its table only, and those of a header or footer facet on the
     * facet's own markup only. An h:column has no element of its own, so its pass-through attributes go on each of its
     * cells: the header cell, the footer cell and the cell of every row.
     */
    @Test
    void testDataTableRendersPassThroughAttributesOnOuterMostElementOnly() {
        assertEquals(TABLE, elementsWith("data-table"));
        assertEquals(SPAN, elementsWith("data-table-header"));
        assertEquals(SPAN, elementsWith("data-table-footer"));
        assertEquals(List.of("th", "td", "td", "td"), elementsWith("data-column"));
        assertEquals(TABLE, elementsWith("data-empty-table"));
    }

    /**
     * The pass-through attributes of h:selectManyCheckbox and h:selectOneRadio go on their outer table or list and on
     * the input of every item, as the Faces TCK requires, but not on the rows, cells, list items and labels around
     * them, nor on the nested table or list of a group of items. In group mode an h:selectOneRadio has no table, and
     * its pass-through attributes go on its input only.
     */
    @Test
    void testSelectListsRenderPassThroughAttributesOnOuterMostElementOnly() {
        assertEquals(TABLE_AND_INPUTS, elementsWith("data-checks"));
        assertEquals(TABLE_AND_INPUTS, elementsWith("data-checks-single"));
        assertEquals(UL_AND_INPUTS, elementsWith("data-checks-list"));
        assertEquals(TABLE_AND_INPUTS, elementsWith("data-checks-page"));
        assertEquals(TABLE_AND_INPUTS, elementsWith("data-radios"));
        assertEquals(UL_AND_INPUTS, elementsWith("data-radios-list"));
        assertEquals(INPUT, elementsWith("data-radio-one"));
        assertEquals(INPUT, elementsWith("data-radio-two"));
    }

    /**
     * The pass-through attributes of h:messages go on its list or table only, not on the item of each message nor on
     * its tooltip.
     */
    @Test
    void testMessagesRenderPassThroughAttributesOnOuterMostElementOnly() {
        assertEquals(UL, elementsWith("data-messages"));
        assertEquals(TABLE, elementsWith("data-messages-table"));
    }

    private List<String> elementsWith(String attribute) {
        return browser.findElements(By.cssSelector("[" + attribute + "]")).stream().map(WebElement::getTagName).collect(toList());
    }
}
