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
package org.eclipse.mojarra.test.issue5576;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

class Issue5576IT extends BaseIT {

    @FindBy(id = "form:messages")
    private WebElement messages;

    @FindBy(id = "form:group")
    private WebElement group;

    @FindBy(id = "form:subgroup11")
    private WebElement subgroup11;

    @FindBy(id = "form:subgroup12")
    private WebElement subgroup12;

    @FindBy(id = "form:save")
    private WebElement save;

    @FindBy(id = "form:clean")
    private WebElement clean;

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5576
     */
    @Test
    void test() {
        open("issue5576.xhtml");

        var selectGroup = new Select(group);
        assertEquals("- Select group -", selectGroup.getFirstSelectedOption().getText());

        guardAjax(() -> selectGroup.selectByValue("Group1"));
        assertEquals("Group1", selectGroup.getFirstSelectedOption().getText());

        var selectSubgroup11 = new Select(subgroup11);
        var selectSubgroup12 = new Select(subgroup12);
        assertEquals("- Select value -", selectSubgroup11.getFirstSelectedOption().getText());
        assertEquals("- Select value -", selectSubgroup12.getFirstSelectedOption().getText());

        guardAjax(() -> selectSubgroup11.selectByValue("Item1"));
        assertEquals("Item1", selectSubgroup11.getFirstSelectedOption().getText());

        selectSubgroup12.selectByValue("Item2");
        assertEquals("Item2", selectSubgroup12.getFirstSelectedOption().getText());
        assertEquals("", messages.getText());

        guardAjax(save::click);
        assertEquals("Text: Validation Error: Value is required.", messages.getText());

        guardAjax(clean::click);
        assertEquals("", messages.getText());
        assertEquals("- Select group -", selectGroup.getFirstSelectedOption().getText());

        guardAjax(() -> selectGroup.selectByValue("Group1"));
        assertEquals("Group1", selectGroup.getFirstSelectedOption().getText());
        assertEquals("- Select value -", selectSubgroup11.getFirstSelectedOption().getText());
        assertEquals("- Select value -", selectSubgroup12.getFirstSelectedOption().getText());
    }
}
