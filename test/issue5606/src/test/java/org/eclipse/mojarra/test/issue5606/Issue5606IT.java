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
package org.eclipse.mojarra.test.issue5606;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class Issue5606IT extends BaseIT {

    @FindBy(id = "form:link")
    private WebElement link;

    @FindBy(id = "form:ajaxButton")
    private WebElement ajaxButton;

    @FindBy(id = "form:executed")
    private WebElement executed;

    @FindBy(id = "form:ajaxExecuted")
    private WebElement ajaxExecuted;

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5606
     */
    @Test
    void testCommandLinkRendersInlineOnclickWithoutCsp() {
        open("issue5606.xhtml");

        String onclick = link.getDomAttribute("onclick");
        assertNotNull(onclick);
        assertTrue(onclick.contains("mojarra.cljs"), onclick);

        String responseBody = getResponseBody("issue5606.xhtml");
        assertTrue(responseBody.contains("onclick="), responseBody);
        assertTrue(!responseBody.contains("mojarra.ael('form:link','click'"), responseBody);
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5606
     */
    @Test
    void testCommandLinkClickStillWorksWithoutCsp() {
        open("issue5606.xhtml");
        guardHttp(link::click);
        assertEquals("true", executed.getText());
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5606
     */
    @Test
    void testCommandButtonAjaxRendersInlineOnclickWithoutCsp() {
        open("issue5606.xhtml");

        String onclick = ajaxButton.getDomAttribute("onclick");
        assertNotNull(onclick);
        assertTrue(onclick.contains("mojarra.ab"), onclick);

        String responseBody = getResponseBody("issue5606.xhtml");
        assertTrue(responseBody.contains("id=\"form:ajaxButton\""), responseBody);
        assertTrue(responseBody.contains("onclick="), responseBody);
        assertTrue(!responseBody.contains("mojarra.ael('form:ajaxButton','click'"), responseBody);
    }

    /**
     * https://github.com/eclipse-ee4j/mojarra/issues/5606
     */
    @Test
    void testCommandButtonAjaxClickStillWorksWithoutCsp() {
        open("issue5606.xhtml");
        guardAjax(ajaxButton::click);
        assertEquals("true", ajaxExecuted.getText());
    }
}
