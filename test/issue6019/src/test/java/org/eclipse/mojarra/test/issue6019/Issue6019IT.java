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
package org.eclipse.mojarra.test.issue6019;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

class Issue6019IT extends BaseIT {

    @FindBy(id = "form:rendered")
    private WebElement rendered;

    @FindBy(id = "form:increment")
    private WebElement increment;

    @FindBy(id = "form:editable")
    private WebElement editable;

    @FindBy(id = "form:readonly")
    private WebElement readonly;

    /**
     * An input which is validated but whose parent turns <code>rendered=false</code> in the same Update Model Values
     * phase keeps its local value in the saved view state. The <code>localValueSet</code> property must be saved along
     * with it, so that the next Update Model Values phase still recognizes the local value, pushes it into the model and
     * resets the input. Save the value without the flag and the input renders that stale local value on every further
     * request, while a readonly twin bound to the same property keeps following the model.
     *
     * @see https://github.com/eclipse-ee4j/mojarra/issues/6019
     */
    @Test
    void testLocalValueSetSurvivesUnrenderedUpdateModel() {
        open("issue6019.xhtml");

        var toggle = new Select(rendered);
        guardAjax(() -> toggle.selectByVisibleText("Show"));
        assertValues("0");

        guardAjax(increment::click);
        assertValues("1");

        guardAjax(increment::click);
        assertValues("2");

        guardAjax(() -> toggle.selectByVisibleText("Hide"));
        guardAjax(() -> toggle.selectByVisibleText("Show"));
        assertValues("2");

        guardAjax(increment::click);
        assertValues("3");
    }

    private void assertValues(String expected) {
        assertEquals(expected, editable.getAttribute("value"), "editable input");
        assertEquals(expected, readonly.getAttribute("value"), "readonly input");
    }
}
