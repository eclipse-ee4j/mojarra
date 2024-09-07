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
package org.eclipse.mojarra.test.issue5460;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class Issue5460IT extends BaseIT {

    @FindBy(id = "form:cc1:required")
    private WebElement cc1required;

    @FindBy(id = "form:cc1:valid")
    private WebElement cc1valid;

    @FindBy(id = "form:cc1:message")
    private WebElement cc1message;

    @FindBy(id = "form:cc1:attributeResults")
    private WebElement cc1attributeResults;

    @FindBy(id = "form:cc2:required")
    private WebElement cc2required;

    @FindBy(id = "form:cc2:valid")
    private WebElement cc2valid;

    @FindBy(id = "form:cc2:message")
    private WebElement cc2message;

    @FindBy(id = "form:cc2:attributeResults")
    private WebElement cc2attributeResults;

    @FindBy(id = "form:cc3:required")
    private WebElement cc3required;

    @FindBy(id = "form:cc3:valid")
    private WebElement cc3valid;

    @FindBy(id = "form:cc3:message")
    private WebElement cc3message;

    @FindBy(id = "form:cc3:attributeResults")
    private WebElement cc3attributeResults;

    @FindBy(id = "form:cc4:required")
    private WebElement cc4required;

    @FindBy(id = "form:cc4:valid")
    private WebElement cc4valid;

    @FindBy(id = "form:cc4:message")
    private WebElement cc4message;

    @FindBy(id = "form:cc4:attributeResults")
    private WebElement cc4attributeResults;

    @FindBy(id = "form:cc5:required")
    private WebElement cc5required;

    @FindBy(id = "form:cc5:valid")
    private WebElement cc5valid;

    @FindBy(id = "form:cc5:message")
    private WebElement cc5message;

    @FindBy(id = "form:cc5:attributeResults")
    private WebElement cc5attributeResults;

    @FindBy(id = "form:cc6:required")
    private WebElement cc6required;

    @FindBy(id = "form:cc6:valid")
    private WebElement cc6valid;

    @FindBy(id = "form:cc6:message")
    private WebElement cc6message;

    @FindBy(id = "form:cc6:attributeResults")
    private WebElement cc6attributeResults;

    @FindBy(id = "form:submit")
    private WebElement submit;

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5460
     * https://github.com/eclipse-ee4j/mojarra/issues/5417
     */
    @Test
    void test() {
        open("issue5460.xhtml");

        assertAll(
            () -> assertEquals("false", cc1required.getText()), () -> assertEquals("true", cc1valid.getText()),
            () -> assertEquals("false defaultClass", cc1attributeResults.getText()),
            () -> assertEquals("", cc1message.getText()), () -> assertEquals("false", cc2required.getText()),
            () -> assertEquals("true", cc2valid.getText()),
            () -> assertEquals("false randomClass", cc2attributeResults.getText()),
            () -> assertEquals("", cc2message.getText()), () -> assertEquals("true", cc3required.getText()),
            () -> assertEquals("true", cc3valid.getText()),
            () -> assertEquals("true defaultClass", cc3attributeResults.getText()),
            () -> assertEquals("", cc3message.getText()), () -> assertEquals("true", cc4required.getText()),
            () -> assertEquals("true", cc4valid.getText()),
            () -> assertEquals("true randomClass", cc4attributeResults.getText()),
            () -> assertEquals("", cc4message.getText()), () -> assertEquals("true", cc5required.getText()),
            () -> assertEquals("true", cc5valid.getText()),
            () -> assertEquals("true defaultClass", cc5attributeResults.getText()),
            () -> assertEquals("", cc5message.getText()), () -> assertEquals("true", cc6required.getText()),
            () -> assertEquals("true", cc6valid.getText()),
            () -> assertEquals("true randomClass", cc6attributeResults.getText()),
            () -> assertEquals("", cc6message.getText())
        );

        guardAjax(submit::click);

        assertAll(
            () -> assertEquals("false", cc1required.getText()), () -> assertEquals("true", cc1valid.getText()),
            () -> assertEquals("false defaultClass", cc1attributeResults.getText()),
            () -> assertEquals("", cc1message.getText()), () -> assertEquals("false", cc2required.getText()),
            () -> assertEquals("true", cc2valid.getText()),
            () -> assertEquals("false randomClass", cc2attributeResults.getText()),
            () -> assertEquals("", cc2message.getText()), () -> assertEquals("true", cc3required.getText()),
            () -> assertEquals("false", cc3valid.getText()),
            () -> assertEquals("true defaultClass", cc3attributeResults.getText()),
            () -> assertEquals("form:cc3: Validation Error: Value is required.", cc3message.getText()),
            () -> assertEquals("true", cc4required.getText()), () -> assertEquals("false", cc4valid.getText()),
            () -> assertEquals("true randomClass", cc4attributeResults.getText()),
            () -> assertEquals("form:cc4: Validation Error: Value is required.", cc4message.getText()),
            () -> assertEquals("true", cc5required.getText()), () -> assertEquals("false", cc5valid.getText()),
            () -> assertEquals("true defaultClass", cc5attributeResults.getText()),
            () -> assertEquals("form:cc5: Validation Error: Value is required.", cc5message.getText()),
            () -> assertEquals("true", cc6required.getText()), () -> assertEquals("false", cc6valid.getText()),
            () -> assertEquals("true randomClass", cc6attributeResults.getText()),
            () -> assertEquals("form:cc6: Validation Error: Value is required.", cc6message.getText())
        );
    }
}
