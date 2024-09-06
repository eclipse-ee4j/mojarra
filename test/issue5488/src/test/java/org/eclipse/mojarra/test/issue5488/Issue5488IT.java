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
package org.eclipse.mojarra.test.issue5488;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class Issue5488IT extends BaseIT {

    @FindBy(id = "form1:input")
    private WebElement form1input;

    @FindBy(id = "form1:button")
    private WebElement form1button;

    @FindBy(id = "form1:messages")
    private WebElement form1messages;

    @FindBy(id = "form2:input")
    private WebElement form2input;

    @FindBy(id = "form2:link")
    private WebElement form2link;

    @FindBy(id = "form2:messages")
    private WebElement form2messages;

    @FindBy(id = "form3:button1")
    private WebElement form3button1;

    @FindBy(id = "form3:button2")
    private WebElement form3button2;

    @FindBy(id = "form3:messages")
    private WebElement form3messages;

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5488
     */
    @Test
    void testCommandButtonBlurred() {
        open("issue5488.xhtml");
        form1input.sendKeys(Keys.TAB);
        guardAjax(() -> form1button.sendKeys(Keys.TAB));

        var messages = form1messages.getText();
        assertEquals("listener invoked on form1:button", messages); // and thus not action invoked as well
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5488
     */
    @Test
    void testCommandButtonClicked() {
        open("issue5488.xhtml");
        guardAjax(form1button::click);
        assertEquals("action invoked on form1:button", form1messages.getText()); // and thus not listener invoked as well
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5488
     */
    @Test
    void testCommandLinkBlurred() {
        open("issue5488.xhtml");
        form2input.sendKeys(Keys.TAB);
        guardAjax(() -> form2link.sendKeys(Keys.TAB));
        assertEquals("listener invoked on form2:link", form2messages.getText()); // and thus not action invoked as well
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5488
     */
    @Test
    void testCommandLinkClicked() {
        open("issue5488.xhtml");
        guardAjax(form2link::click);
        assertEquals("action invoked on form2:link", form2messages.getText()); // and thus not listener invoked as well
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/3355
     */
    @Test
    void testPlainButton1() {
        open("issue5488.xhtml");
        guardAjax(form3button1::click);
        assertEquals("listener invoked on form3:button1", form3messages.getText()); // and thus not on form3:button2 as well
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/3355
     */
    @Test
    void testPlainButton2() {
        open("issue5488.xhtml");
        guardAjax(form3button2::click);
        assertEquals("listener invoked on form3:button2", form3messages.getText()); // and thus not on form3:button1 as well
    }
}
