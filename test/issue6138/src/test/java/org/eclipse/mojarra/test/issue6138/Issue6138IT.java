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
package org.eclipse.mojarra.test.issue6138;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * A tree visit or an invokeOnComponent which starts inside a row of a nested iterating component leaves the per-row
 * state of the other rows intact: each input validates and updates the model with its own submitted value.
 *
 * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6138">issue 6138</a>
 */
class Issue6138IT extends BaseIT {

    private static final String TWO_LEVELS = "[[a, b], [c, d]]";
    private static final String THREE_LEVELS = "[[[a, b], [c, d]], [[e, f], [g, h]]]";
    private static final String TWO_LEVELS_WIDE = "[[a, b, c], [d, e, f]]";
    private static final String THREE_LEVELS_WIDE = "[[[a, b, c], [d, e, f]], [[g, h, i], [j, k, l]]]";

    /**
     * A validator which visits the form, in a ui:repeat in ui:repeat, keeps the submitted value of each row.
     */
    @Test
    void testVisitRepeatInRepeatKeepsRowState() {
        assertEachRowKeepsItsValue("visit_rr");
    }

    /**
     * A validator which visits the form, in a h:dataTable in ui:repeat, keeps the submitted value of each row.
     */
    @Test
    void testVisitTableInRepeatKeepsRowState() {
        assertEachRowKeepsItsValue("visit_rt");
    }

    /**
     * A validator which visits the form, in a ui:repeat in h:dataTable, keeps the submitted value of each row.
     */
    @Test
    void testVisitRepeatInTableKeepsRowState() {
        assertEachRowKeepsItsValue("visit_tr");
    }

    /**
     * A validator which visits the form, in a h:dataTable in h:dataTable, keeps the submitted value of each row.
     */
    @Test
    void testVisitTableInTableKeepsRowState() {
        assertEachRowKeepsItsValue("visit_tt");
    }

    /**
     * A validator which invokes on the first input, in a ui:repeat in ui:repeat, keeps the submitted value of each row.
     */
    @Test
    void testInvokeRepeatInRepeatKeepsRowState() {
        assertEachRowKeepsItsValue("invoke_rr", TWO_LEVELS_WIDE);
    }

    /**
     * A validator which invokes on the first input, in a h:dataTable in ui:repeat, keeps the submitted value of each row.
     */
    @Test
    void testInvokeTableInRepeatKeepsRowState() {
        assertEachRowKeepsItsValue("invoke_rt");
    }

    /**
     * A validator which invokes on the first input, in a ui:repeat in h:dataTable, keeps the submitted value of each row.
     */
    @Test
    void testInvokeRepeatInTableKeepsRowState() {
        assertEachRowKeepsItsValue("invoke_tr", TWO_LEVELS_WIDE);
    }

    /**
     * A validator which invokes on the first input, in a h:dataTable in h:dataTable, keeps the submitted value of each row.
     */
    @Test
    void testInvokeTableInTableKeepsRowState() {
        assertEachRowKeepsItsValue("invoke_tt");
    }

    /**
     * A validator which visits the form, in a h:dataTable in h:dataTable which both preserve the full row state, keeps the
     * submitted value of each row.
     */
    @Test
    void testVisitTableInTableWithRowStatePreservedKeepsRowState() {
        assertEachRowKeepsItsValue("visit_tt_rsp");
    }

    /**
     * A validator which invokes on the first input, in a h:dataTable in h:dataTable which both preserve the full row
     * state, keeps the submitted value of each row.
     */
    @Test
    void testInvokeTableInTableWithRowStatePreservedKeepsRowState() {
        assertEachRowKeepsItsValue("invoke_tt_rsp");
    }

    /**
     * A validator which visits the form, in a ui:repeat in ui:repeat in ui:repeat, keeps the submitted value of each row.
     */
    @Test
    void testVisitRepeatInRepeatInRepeatKeepsRowState() {
        assertEachRowKeepsItsValue("visit_rrr", THREE_LEVELS);
    }

    /**
     * A validator which invokes on the first input, in a ui:repeat in ui:repeat in ui:repeat, keeps the submitted value
     * of each row.
     */
    @Test
    void testInvokeRepeatInRepeatInRepeatKeepsRowState() {
        assertEachRowKeepsItsValue("invoke_rrr", THREE_LEVELS_WIDE);
    }

    /**
     * A validator which visits the form, in a h:dataTable in a h:panelGroup in ui:repeat, keeps the submitted value of
     * each row.
     */
    @Test
    void testVisitTableInPanelInRepeatKeepsRowState() {
        assertEachRowKeepsItsValue("visit_rt_panel");
    }

    private void assertEachRowKeepsItsValue(String form) {
        assertEachRowKeepsItsValue(form, TWO_LEVELS);
    }

    /**
     * Enters a, b, c, ... in the inputs of the given form in document order, submits it, and asserts that the model got
     * each value in its own row and that each input validated its own value.
     */
    private void assertEachRowKeepsItsValue(String form, String expectedModel) {
        open("issue6138.xhtml");
        List<WebElement> inputs = browser.findElements(By.cssSelector("#" + form + " input[type=text]"));
        Map<String, String> expectedValidated = new TreeMap<>();
        for (int i = 0; i < inputs.size(); i++) {
            String value = String.valueOf((char) ('a' + i));
            inputs.get(i).sendKeys(value);
            expectedValidated.put(inputs.get(i).getAttribute("id"), value);
        }
        guardHttp(browser.findElement(By.id(form + ":submit"))::click);
        assertEquals(expectedModel, browser.findElement(By.id(form + ":output")).getText());
        assertEquals(expectedValidated.toString(), browser.findElement(By.id(form + ":validated")).getText());
    }
}
