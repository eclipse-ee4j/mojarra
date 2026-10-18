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
package org.eclipse.mojarra.test.issue6070;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * An expression in {@code <f:view encoding>} which evaluates to {@code null} or an empty string counts as an absent
 * encoding. With {@code jakarta.faces.INTERPRET_EMPTY_STRING_SUBMITTED_VALUES_AS_NULL} enabled, an expression without
 * value evaluates to {@code null}.
 */
class Issue6070IT extends BaseIT {

    @FindBy(id = "form:redirect")
    private WebElement redirect;

    /**
     * Opened without {@code enc}, the view is rendered in the default UTF-8, so the redirect from its action must encode
     * "ä" as UTF-8.
     *
     * @see https://github.com/eclipse-ee4j/mojarra/issues/6070
     */
    @Test
    void testRedirectFromViewWithEncodingExpressionWithoutValue() {
        open("issue6070Expression.xhtml");
        guardHttp(redirect::click);
        assertQueryString("q=%C3%A4");
    }

    /**
     * Opened with an empty {@code enc}, the view is rendered in the default UTF-8, so the redirect from its action must
     * encode "ä" as UTF-8.
     *
     * @see https://github.com/eclipse-ee4j/mojarra/issues/6070
     */
    @Test
    void testRedirectFromViewWithEncodingExpressionWithEmptyValue() {
        open("issue6070Expression.xhtml?enc=");
        guardHttp(redirect::click);
        assertQueryString("q=%C3%A4");
    }

    /**
     * Opened with {@code ?enc=ISO-8859-1}, the view is rendered in ISO-8859-1. Its form posts back without that request
     * parameter, so the expression has no value on the postback. The redirect from its action must still encode "ä" as
     * ISO-8859-1, the encoding of the submitted page.
     *
     * @see https://github.com/eclipse-ee4j/mojarra/issues/6070
     */
    @Test
    void testRedirectFromViewWithEncodingExpressionWithoutValueOnPostback() {
        open("issue6070Expression.xhtml?enc=ISO-8859-1");
        guardHttp(redirect::click);
        assertQueryString("q=%E4");
    }

    private void assertQueryString(String expected) {
        String url = browser.getCurrentUrl();
        assertTrue(url.endsWith("?" + expected), "redirect URL should end with ?" + expected + ", was: " + url);
    }
}
