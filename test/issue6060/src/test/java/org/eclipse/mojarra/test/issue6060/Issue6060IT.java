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
package org.eclipse.mojarra.test.issue6060;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class Issue6060IT extends BaseIT {

    @FindBy(id = "form:rebuild")
    private WebElement rebuild;

    @FindBy(id = "form:submit")
    private WebElement submit;

    @FindBy(id = "form:field")
    private WebElement field;

    @FindBy(id = "form:value")
    private WebElement value;

    /**
     * A dynamically added panel holding an input is replaced in an action by a new dynamically added panel holding a new
     * input with the same id. On the next postback the new input must be restored under the new panel, so that it
     * processes its submitted value and is rendered again.
     *
     * @see https://github.com/eclipse-ee4j/mojarra/issues/6060
     */
    @Test
    void testDynamicPanelReplacedByDynamicPanelWithSameChildId() {
        open("issue6060Dynamic.xhtml");
        assertFieldIn("form:panel1");
        assertReplacedFieldSubmits("form:panel2");
    }

    /**
     * A panel holding an input, both created by the view declaration, is replaced in an action by a dynamically added
     * panel holding a new input with the same id. On the next postback the new input must be restored under the new
     * panel, so that it processes its submitted value and is rendered again.
     *
     * @see https://github.com/eclipse-ee4j/mojarra/issues/6060
     */
    @Test
    void testStaticPanelReplacedByDynamicPanelWithSameChildId() {
        open("issue6060Static.xhtml");
        assertFieldIn("form:panel0");
        assertReplacedFieldSubmits("form:panel1");
    }

    private void assertReplacedFieldSubmits(String panelId) {
        guardHttp(rebuild::click);
        assertFieldIn(panelId);

        assertFieldSubmits(panelId, "first");
        assertFieldSubmits(panelId, "second");
    }

    private void assertFieldSubmits(String panelId, String text) {
        field.clear();
        field.sendKeys(text);
        guardHttp(submit::click);
        assertFieldIn(panelId);
        assertEquals(text, field.getAttribute("value"), "rendered field value");
        assertEquals(text, value.getText(), "model value");
    }

    private void assertFieldIn(String panelId) {
        assertEquals(1, browser.findElements(By.cssSelector("[id='" + panelId + "'] [id='form:field']")).size(), "form:field inside " + panelId);
    }
}
