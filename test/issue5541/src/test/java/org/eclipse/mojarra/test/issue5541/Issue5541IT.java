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
package org.eclipse.mojarra.test.issue5541;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class Issue5541IT extends BaseIT {

    @FindBy(id = "form:inputDate")
    private WebElement inputDate;

    @FindBy(id = "form:inputDateTime")
    private WebElement inputDateTime;

    @FindBy(id = "form:inputLocalDate")
    private WebElement inputLocalDate;

    @FindBy(id = "form:inputLocalTime")
    private WebElement inputLocalTime;

    @FindBy(id = "form:inputLocalDateTime")
    private WebElement inputLocalDateTime;

    @FindBy(id = "form:inputZonedDateTime")
    private WebElement inputZonedDateTime;

    @FindBy(id = "form:submit")
    private WebElement submit;

    @FindBy(id = "form:outputDate")
    private WebElement outputDate;

    @FindBy(id = "form:outputDateTime")
    private WebElement outputDateTime;

    @FindBy(id = "form:outputLocalDate")
    private WebElement outputLocalDate;

    @FindBy(id = "form:outputLocalTime")
    private WebElement outputLocalTime;

    @FindBy(id = "form:outputLocalDateTime")
    private WebElement outputLocalDateTime;

    @FindBy(id = "form:outputZonedDateTime")
    private WebElement outputZonedDateTime;

    @FindBy(id = "form:messages")
    private WebElement messages;

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testValidDate() {
        open("issue5541.xhtml");
        inputDate.sendKeys("2024-06-30");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("2024-06-30", outputDate.getText()),
            () -> assertEquals("", messages.getText())
        );
    }
    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testInvalidDate() {
        open("issue5541.xhtml");
        inputDate.sendKeys("2024-06-31");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("", outputDate.getText()),
            () -> assertEquals("form:inputDate: '2024-06-31' could not be understood as a date.", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testValidDateTime() {
        open("issue5541.xhtml");
        inputDateTime.sendKeys("2024-06-30 12:34:56");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("2024-06-30 12:34:56", outputDateTime.getText()),
            () -> assertEquals("", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testInvalidDateTime() {
        open("issue5541.xhtml");
        inputDateTime.sendKeys("2024-06-30 23:45:67");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("", outputDateTime.getText()),
            () -> assertEquals("form:inputDateTime: '2024-06-30 23:45:67' could not be understood as a date.", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testValidLocalDate() {
        open("issue5541.xhtml");
        inputLocalDate.sendKeys("2024-06-30");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("2024-06-30", outputLocalDate.getText()),
            () -> assertEquals("", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testValidLocalDateBce() {
        open("issue5541.xhtml?acceptBce=true");
        inputLocalDate.sendKeys("2024-06-30");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("2024-06-30", outputLocalDate.getText()),
            () -> assertEquals("", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testValidLocalDateBceNegative() {
        open("issue5541.xhtml?acceptBce=true");
        inputLocalDate.sendKeys("-2024-06-30");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("-2024-06-30", outputLocalDate.getText()),
            () -> assertEquals("", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testInvalidLocalDate() {
        open("issue5541.xhtml");
        inputLocalDate.sendKeys("2024-06-31");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("", outputLocalDate.getText()),
            () -> assertEquals("form:inputLocalDate: '2024-06-31' could not be understood as a date.", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testInvalidLocalDateBce() {
        open("issue5541.xhtml?acceptBce=true");
        inputLocalDate.sendKeys("2024-06-31");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("", outputLocalDate.getText()),
            () -> assertEquals("form:inputLocalDate: '2024-06-31' could not be understood as a date.", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testInvalidLocalDateBceNegative() {
        open("issue5541.xhtml?acceptBce=true");
        inputLocalDate.sendKeys("-2024-06-31");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("", outputLocalDate.getText()),
            () -> assertEquals("form:inputLocalDate: '-2024-06-31' could not be understood as a date.", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testValidLocalTime() {
        open("issue5541.xhtml");
        inputLocalTime.sendKeys("12:34:56");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("12:34:56", outputLocalTime.getText()),
            () -> assertEquals("", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testInvalidLocalTime() {
        open("issue5541.xhtml");
        inputLocalTime.sendKeys("23:45:67");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("", outputLocalTime.getText()),
            () -> assertEquals("form:inputLocalTime: '23:45:67' could not be understood as a time.", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testValidLocalDateTime() {
        open("issue5541.xhtml");
        inputLocalDateTime.sendKeys("2024-06-30 12:34:56");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("2024-06-30 12:34:56", outputLocalDateTime.getText()),
            () -> assertEquals("", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testValidLocalDateTimeBce() {
        open("issue5541.xhtml?acceptBce=true");
        inputLocalDateTime.sendKeys("2024-06-30 12:34:56");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("2024-06-30 12:34:56", outputLocalDateTime.getText()),
            () -> assertEquals("", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testValidLocalDateTimeBceNegative() {
        open("issue5541.xhtml?acceptBce=true");
        inputLocalDateTime.sendKeys("-2024-06-30 12:34:56");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("-2024-06-30 12:34:56", outputLocalDateTime.getText()),
            () -> assertEquals("", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testInvalidLocalDateTime() {
        open("issue5541.xhtml");
        inputLocalDateTime.sendKeys("2024-06-30 23:45:67");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("", outputLocalDateTime.getText()),
            () -> assertEquals("form:inputLocalDateTime: '2024-06-30 23:45:67' could not be understood as a date and time.", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testInvalidLocalDateTimeBce() {
        open("issue5541.xhtml?acceptBce=true");
        inputLocalDateTime.sendKeys("2024-06-30 23:45:67");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("", outputLocalDateTime.getText()),
            () -> assertEquals("form:inputLocalDateTime: '2024-06-30 23:45:67' could not be understood as a date and time.", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testInvalidLocalDateTimeBceNegative() {
        open("issue5541.xhtml?acceptBce=true");
        inputLocalDateTime.sendKeys("-2024-06-30 23:45:67");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("", outputLocalDateTime.getText()),
            () -> assertEquals("form:inputLocalDateTime: '-2024-06-30 23:45:67' could not be understood as a date and time.", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testValidZonedDateTime() {
        open("issue5541.xhtml");
        inputZonedDateTime.sendKeys("2024-06-30 12 uur 34 min +0130");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("2024-06-30 12 uur 34 min +0130", outputZonedDateTime.getText()),
            () -> assertEquals("", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testValidZonedDateTimeBce() {
        open("issue5541.xhtml?acceptBce=true");
        inputZonedDateTime.sendKeys("2024-06-30 12 uur 34 min +0130");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("2024-06-30 12 uur 34 min +0130", outputZonedDateTime.getText()),
            () -> assertEquals("", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testValidZonedDateTimeBceNegative() {
        open("issue5541.xhtml?acceptBce=true");
        inputZonedDateTime.sendKeys("-2024-06-30 12 uur 34 min +0130");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("-2024-06-30 12 uur 34 min +0130", outputZonedDateTime.getText()),
            () -> assertEquals("", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testInvalidZonedDateTime() {
        open("issue5541.xhtml");
        inputZonedDateTime.sendKeys("2024-06-31 12 uur 34 min +0130");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("", outputZonedDateTime.getText()),
            () -> assertEquals("form:inputZonedDateTime: '2024-06-31 12 uur 34 min +0130' could not be understood as a date and time.", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testInvalidZonedDateTimeBce() {
        open("issue5541.xhtml?acceptBce=true");
        inputZonedDateTime.sendKeys("2024-06-31 12 uur 34 min +0130");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("", outputZonedDateTime.getText()),
            () -> assertEquals("form:inputZonedDateTime: '2024-06-31 12 uur 34 min +0130' could not be understood as a date and time.", messages.getText())
        );
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5541
     */
    @Test
    void testInvalidZonedDateTimeBceNegative() {
        open("issue5541.xhtml?acceptBce=true");
        inputZonedDateTime.sendKeys("-2024-06-31 12 uur 34 min +0130");
        guardHttp(submit::click);
        assertAll(
            () -> assertEquals("", outputZonedDateTime.getText()),
            () -> assertEquals("form:inputZonedDateTime: '-2024-06-31 12 uur 34 min +0130' could not be understood as a date and time.", messages.getText())
        );
    }
}
