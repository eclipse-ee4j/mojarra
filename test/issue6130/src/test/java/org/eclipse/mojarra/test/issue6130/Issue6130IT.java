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
package org.eclipse.mojarra.test.issue6130;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class Issue6130IT extends BaseIT {

    @FindBy(id = "form:target")
    private WebElement target;

    @FindBy(id = "form:other")
    private WebElement other;

    /**
     * A removed component which the view still builds stays removed on every later postback.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6130">issue 6130</a>
     */
    @Test
    void testRemovedComponentStaysRemovedWhileViewBuildsIt() {
        open("issue6130.xhtml");
        clickAndAssertChildren("removeS", "C", "O");
        clickAndAssertChildren("submit", "C", "O");
        clickAndAssertChildren("submit", "C", "O");
    }

    /**
     * A component which is removed in the same request as the view stops building it is shown again when the view
     * builds it again on the next postback.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6130">issue 6130</a>
     */
    @Test
    void testRemovedAndHiddenComponentIsShownWhenViewBuildsItAgainOnNextPostback() {
        open("issue6130.xhtml");
        clickAndAssertChildren("removeAndHideC", "S", "O");
        clickAndAssertChildren("showC", "SC", "O");
        clickAndAssertChildren("submit", "SC", "O");
    }

    /**
     * A component which is removed in the same request as the view stops building it is shown again when the view
     * builds it again on a later postback.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6130">issue 6130</a>
     */
    @Test
    void testRemovedAndHiddenComponentIsShownWhenViewBuildsItAgainOnLaterPostback() {
        open("issue6130.xhtml");
        clickAndAssertChildren("removeAndHideC", "S", "O");
        clickAndAssertChildren("submit", "S", "O");
        clickAndAssertChildren("showC", "SC", "O");
        clickAndAssertChildren("submit", "SC", "O");
    }

    /**
     * A component which is removed one postback before the view stops building it is shown again when the view builds
     * it again.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6130">issue 6130</a>
     */
    @Test
    void testRemovedComponentIsShownWhenViewStopsAndThenBuildsItAgain() {
        open("issue6130.xhtml");
        clickAndAssertChildren("removeC", "S", "O");
        clickAndAssertChildren("hideC", "S", "O");
        clickAndAssertChildren("showC", "SC", "O");
        clickAndAssertChildren("submit", "SC", "O");
    }

    /**
     * A component which is moved to another parent stays there only, also when the view stops and then builds it
     * again in its original parent.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6130">issue 6130</a>
     */
    @Test
    void testMovedComponentStaysMovedWhenViewStopsAndThenBuildsItAgain() {
        open("issue6130.xhtml");
        clickAndAssertChildren("moveC", "S", "OC");
        clickAndAssertChildren("hideC", "S", "OC");
        clickAndAssertChildren("showC", "S", "OC");
        clickAndAssertChildren("submit", "S", "OC");
    }

    private void clickAndAssertChildren(String button, String expectedTarget, String expectedOther) {
        WebElement element = browser.findElement(By.id("form:" + button));
        guardHttp(element::click);
        assertEquals(expectedTarget, target.getText().replaceAll("\\s", ""), "rendered children of form:target after " + button);
        assertEquals(expectedOther, other.getText().replaceAll("\\s", ""), "rendered children of form:other after " + button);
    }
}
