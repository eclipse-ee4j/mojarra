/*
 * Copyright (c) Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GPL-2.0 with Classpath-exception-2.0 which
 * is available at https://openjdk.java.net/legal/gplv2+ce.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 or Apache-2.0
 */
package org.eclipse.mojarra.test.issue5464;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class Issue5464IT extends BaseIT {

    @FindBy(id = "form:input")
    private WebElement input;

    @FindBy(id = "form:submit")
    private WebElement submit;

    @FindBy(id = "form:output")
    private WebElement output;

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5464
     */
    @Test
    void testFFFE() {
        open("issue5464.xhtml");
        input.sendKeys("f\uFFFEoo");
        guardAjax(submit::click);
        assertEquals("Result: foo", output.getText());
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/4516
     */
    @Test
    void test000C() {
        open("issue5464.xhtml");
        input.sendKeys("f\u000Coo");
        guardAjax(submit::click);
        assertEquals("Result: foo", output.getText());
    }
}
