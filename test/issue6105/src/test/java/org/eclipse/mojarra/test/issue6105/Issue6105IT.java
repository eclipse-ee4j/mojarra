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
package org.eclipse.mojarra.test.issue6105;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.Select;

class Issue6105IT extends BaseIT {

    private static final List<String> ITEMS = List.of("one", "two", "three");

    @FindBy(id = "form:ajaxSubmit")
    private WebElement ajaxSubmit;

    @FindBy(id = "form:nonAjaxSubmit")
    private WebElement nonAjaxSubmit;

    @FindBy(id = "form:selected")
    private WebElement selected;

    /**
     * When the items of a composite component which relocates its children via cc:insertChildren are created by a
     * c:forEach, then an ajax postback must still render all of them, and a value selected afterwards must still be
     * submitted.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6105">issue 6105</a>
     */
    @Test
    void testAjaxPostbackKeepsRelocatedForEachChildren() {
        open("issue6105.xhtml");
        assertEquals(ITEMS, getOptions());

        guardAjax(ajaxSubmit::click);
        assertEquals(ITEMS, getOptions());

        getMenu().selectByValue("two");
        guardAjax(ajaxSubmit::click);
        assertEquals(ITEMS, getOptions());
        assertEquals("two", selected.getText());
    }

    /**
     * When the items of a composite component which relocates its children via cc:insertChildren are created by a
     * c:forEach, then a non-ajax postback must still render all of them, and a value selected afterwards must still be
     * submitted.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6105">issue 6105</a>
     */
    @Test
    void testNonAjaxPostbackKeepsRelocatedForEachChildren() {
        open("issue6105.xhtml");
        assertEquals(ITEMS, getOptions());

        guardHttp(nonAjaxSubmit::click);
        assertEquals(ITEMS, getOptions());

        getMenu().selectByValue("three");
        guardHttp(nonAjaxSubmit::click);
        assertEquals(ITEMS, getOptions());
        assertEquals("three", selected.getText());
    }

    private Select getMenu() {
        return new Select(browser.findElement(By.id("form:composite:menu")));
    }

    private List<String> getOptions() {
        return getMenu().getOptions().stream().map(option -> option.getAttribute("value")).collect(toList());
    }
}
