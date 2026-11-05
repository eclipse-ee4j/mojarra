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
package org.eclipse.mojarra.test.issue6102;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class Issue6102IT extends BaseIT {

    @FindBy(id = "form:target")
    private WebElement target;

    /**
     * A component added at index 1 of a parent, in a request before another component is added at index 0 of the same
     * parent, moves to index 2 and keeps that position on every later postback.
     *
     * @see https://github.com/eclipse-ee4j/mojarra/issues/6102
     */
    @Test
    void testDynamicChildMovesDownWhenSiblingIsAddedBeforeIt() {
        open("issue6102.xhtml");
        clickAndAssertChildren("addA1", "S0AS1");
        clickAndAssertChildren("addB0", "BS0AS1");
        clickAndAssertChildren("submit", "BS0AS1");
        clickAndAssertChildren("submit", "BS0AS1");
    }

    /**
     * A component added at index 2 of a parent, in a request before a component before it is removed from the same
     * parent, moves to index 1 and keeps that position on every later postback.
     *
     * @see https://github.com/eclipse-ee4j/mojarra/issues/6102
     */
    @Test
    void testDynamicChildMovesUpWhenDynamicSiblingBeforeItIsRemoved() {
        open("issue6102.xhtml");
        clickAndAssertChildren("addA0", "AS0S1");
        clickAndAssertChildren("addB2", "AS0BS1");
        clickAndAssertChildren("removeA", "S0BS1");
        clickAndAssertChildren("submit", "S0BS1");
    }

    /**
     * A component appended to a parent, in a request before a static component before it is removed from the same
     * parent, stays last on every later postback.
     *
     * @see https://github.com/eclipse-ee4j/mojarra/issues/6102
     */
    @Test
    void testAppendedDynamicChildStaysLastWhenStaticSiblingBeforeItIsRemoved() {
        open("issue6102.xhtml");
        clickAndAssertChildren("addA2", "S0S1A");
        clickAndAssertChildren("removeS0", "S1A");
        clickAndAssertChildren("submit", "S1A");
    }

    /**
     * A component added at index 1 of a parent, in a request before a static component before it is removed from the
     * same parent, moves to index 0 and keeps that position on every later postback.
     *
     * @see https://github.com/eclipse-ee4j/mojarra/issues/6102
     */
    @Test
    void testDynamicChildMovesUpWhenStaticSiblingBeforeItIsRemoved() {
        open("issue6102.xhtml");
        clickAndAssertChildren("addA1", "S0AS1");
        clickAndAssertChildren("removeS0", "AS1");
        clickAndAssertChildren("submit", "AS1");
    }

    /**
     * A component added at index 1 of a parent, in the same request as a static component before it is removed from the
     * same parent, moves to index 0 and keeps that position on every later postback.
     *
     * @see https://github.com/eclipse-ee4j/mojarra/issues/6102
     */
    @Test
    void testDynamicChildMovesUpWhenStaticSiblingBeforeItIsRemovedInTheSameRequest() {
        open("issue6102.xhtml");
        clickAndAssertChildren("addA1RemoveS0", "AS1");
        clickAndAssertChildren("submit", "AS1");
        clickAndAssertChildren("submit", "AS1");
    }

    /**
     * A component added at index 2 of a parent, in a request after another component was added at index 0 of the same
     * parent, keeps its position on every later postback.
     *
     * @see https://github.com/eclipse-ee4j/mojarra/issues/6102
     */
    @Test
    void testDynamicChildAddedAfterDynamicSiblingKeepsItsPosition() {
        open("issue6102.xhtml");
        clickAndAssertChildren("addA0", "AS0S1");
        clickAndAssertChildren("addB2", "AS0BS1");
        clickAndAssertChildren("submit", "AS0BS1");
    }

    private void clickAndAssertChildren(String button, String expected) {
        WebElement element = browser.findElement(By.id("form:" + button));
        guardHttp(element::click);
        assertEquals(expected, target.getText().replaceAll("\\s", ""), "rendered children of form:target after " + button);
    }
}
