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
package org.eclipse.mojarra.test.issue6184;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

/**
 * The client id of a component is derived from its current parent chain: the container client id of its closest
 * NamingContainer ancestor, followed by its own id. Content which composite:insertFacet relocates into a nested
 * composite component therefore carries the id of that nested composite component, on the initial request and after
 * any postback, also when its client id was resolved before the relocation. The head of the view gets a component
 * resource after the view is built on the initial request, so that postbacks restore a view whose head differs from the
 * one built from the Facelet.
 *
 * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6184">issue 6184</a>
 */
class Issue6184IT extends BaseIT {

    private static final String LINK = "form:navigator:toolbar:export:exportLink:download";

    private static final List<String> CELLS = List.of("form:navigator:toolbar:table:0:cell", "form:navigator:toolbar:table:1:cell");

    /**
     * The relocated command link carries the id of the composite component it is relocated into, on the initial request
     * and after an ajax postback, and invokes its action listener.
     */
    @Test
    void testRelocatedCommandLinkKeepsIdAndInvokesActionListenerAfterAjaxPostback() {
        open("relocated.xhtml");
        assertEquals(LINK, linkId());

        guardAjax(browser.findElement(By.id("form:refresh"))::click);
        assertEquals(LINK, linkId());

        guardHttp(browser.findElement(By.id(LINK))::click);
        assertEquals("true", browser.findElement(By.id("form:downloaded")).getText());
    }

    /**
     * The rows of a relocated data table carry the id of the composite component it is relocated into, also when the
     * client id of the data table was resolved before the relocation.
     */
    @Test
    void testRelocatedDataTableRowsCarryNewParentChain() {
        open("relocated.xhtml");
        assertEquals(CELLS, cellIds());

        guardAjax(browser.findElement(By.id("form:refresh"))::click);
        assertEquals(CELLS, cellIds());
    }

    private String linkId() {
        return browser.findElement(By.className("download")).getAttribute("id");
    }

    private List<String> cellIds() {
        return browser.findElements(By.className("cell")).stream().map(cell -> cell.getAttribute("id")).collect(toList());
    }
}
