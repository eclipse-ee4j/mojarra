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
package org.eclipse.mojarra.test.issue6067;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class Issue6067IT extends BaseIT {

    @FindBy(id = "form:field")
    private WebElement field;

    @FindBy(id = "form:submit")
    private WebElement submit;

    @FindBy(id = "form:value")
    private WebElement value;

    /**
     * With client side state saving and client state encryption disabled in Development stage, the state is written as
     * plain Base64 and must be read back, so that a postback succeeds and processes the submitted value.
     *
     * @see https://github.com/eclipse-ee4j/mojarra/issues/6067
     */
    @Test
    void testPostbackWithUnencryptedClientState() {
        open("issue6067.xhtml");
        field.sendKeys("submitted");
        guardHttp(submit::click);
        assertEquals("submitted", value.getText(), "model value");
    }
}
