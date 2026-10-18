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
package org.eclipse.mojarra.test.issue6069;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * A redirect from an action percent-encodes its query parameters with the encoding the view was rendered with. With
 * client side state saving, only the view itself carries that encoding to the postback. The views without a declared
 * encoding and with a literal one derive it again while building the view on the postback, and guard that the encoding
 * need not be saved in the view state for them. The view with an encoding from an expression needs the saved one.
 */
class Issue6069IT extends BaseIT {

    @FindBy(id = "form:redirect")
    private WebElement redirect;

    /**
     * A view without a declared encoding is rendered in UTF-8, so the redirect must encode "ä" as UTF-8.
     *
     * @see https://github.com/eclipse-ee4j/mojarra/issues/6069
     */
    @Test
    void testRedirectFromViewWithDefaultEncoding() {
        open("issue6069Default.xhtml");
        guardHttp(redirect::click);
        assertQueryString("q=%C3%A4");
    }

    /**
     * A view with {@code <f:view encoding="ISO-8859-1">} is rendered in ISO-8859-1, so the redirect from its action
     * must encode "ä" as ISO-8859-1.
     *
     * @see https://github.com/eclipse-ee4j/mojarra/issues/6069
     */
    @Test
    void testRedirectFromViewWithDeclaredEncoding() {
        open("issue6069Declared.xhtml");
        guardHttp(redirect::click);
        assertQueryString("q=%E4");
    }

    /**
     * A view with {@code <f:view encoding="#{param.enc}">} opened with {@code ?enc=ISO-8859-1} is rendered in
     * ISO-8859-1. Its form posts back without that request parameter, so the expression evaluates to something else on
     * the postback. The redirect from its action must still encode "ä" as ISO-8859-1, the encoding of the submitted
     * page.
     *
     * @see https://github.com/eclipse-ee4j/mojarra/issues/6069
     */
    @Test
    void testRedirectFromViewWithEncodingFromExpression() {
        open("issue6069Expression.xhtml?enc=ISO-8859-1");
        guardHttp(redirect::click);
        assertQueryString("q=%E4");
    }

    private void assertQueryString(String expected) {
        String url = browser.getCurrentUrl();
        assertTrue(url.endsWith("?" + expected), "redirect URL should end with ?" + expected + ", was: " + url);
    }
}
