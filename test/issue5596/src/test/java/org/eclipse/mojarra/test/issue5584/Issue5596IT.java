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
package org.eclipse.mojarra.test.issue5584;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

class Issue5596IT extends BaseIT {

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5596
     */
    @Test
    void testImplicitConversion() {
        test("formWithImplicitConversion");
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5596
     */
    @Test
    void testDefaultNumberConverter() {
        test("formWithDefaultNumberConverter");
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5596
     */
    @Test
    void testSpecificNumberConverter() {
        test("formWithSpecificNumberConverter");
    }

    private void test(String formId) {
        open("issue5596.xhtml");
        assertEquals("", browser.findElement(By.id(formId + ":messages")).getText());
        browser.findElement(By.id(formId + ":byte")).sendKeys("123");
        browser.findElement(By.id(formId + ":short")).sendKeys("123");
        browser.findElement(By.id(formId + ":integer")).sendKeys("123");
        browser.findElement(By.id(formId + ":long")).sendKeys("123");
        browser.findElement(By.id(formId + ":float")).sendKeys("123.45");
        browser.findElement(By.id(formId + ":double")).sendKeys("123.45");
        browser.findElement(By.id(formId + ":bigInteger")).sendKeys("123");
        browser.findElement(By.id(formId + ":bigDecimal")).sendKeys("123.45");
        guardHttp(browser.findElement(By.id(formId + ":submit"))::click);
        assertEquals("", browser.findElement(By.id(formId + ":messages")).getText());

        for (var id : Set.of("formWithImplicitConversion", "formWithDefaultNumberConverter", "formWithSpecificNumberConverter")) {
            assertAll(
                () -> assertEquals("123", browser.findElement(By.id(id + ":byte")).getDomProperty("value")),
                () -> assertEquals("123", browser.findElement(By.id(id + ":short")).getDomProperty("value")),
                () -> assertEquals("123", browser.findElement(By.id(id + ":integer")).getDomProperty("value")),
                () -> assertEquals("123", browser.findElement(By.id(id + ":long")).getDomProperty("value")),
                () -> assertEquals("123.45", browser.findElement(By.id(id + ":float")).getDomProperty("value")),
                () -> assertEquals("123.45", browser.findElement(By.id(id + ":double")).getDomProperty("value")),
                () -> assertEquals("123", browser.findElement(By.id(id + ":bigInteger")).getDomProperty("value")),
                () -> assertEquals("123.45", browser.findElement(By.id(id + ":bigDecimal")).getDomProperty("value"))
            );
        }
    }
}
