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
package org.eclipse.mojarra.test.issue6166;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * At the render-time re-apply, the refresh of a parent removes every component it finds and adds it back at the end
 * of its children, while a c:forEach over a list whose iteration is unchanged since the restore-time build retains
 * the children it created instead of re-applying its body. The retained children must still end up behind whatever
 * precedes the c:forEach in the facelet.
 */
class Issue6166IT extends BaseIT {

    private static final String COMPONENTS = "[before][one][two][between][three][four][after]";

    private static final String TEXT = "[before][one][two][after]";

    private static final String ONLY = "[one][two]";

    private static final String FIRST = "[one][two][after]";

    @FindBy(id = "form:components")
    private WebElement components;

    @FindBy(id = "form:text")
    private WebElement text;

    @FindBy(id = "form:only")
    private WebElement only;

    @FindBy(id = "form:first")
    private WebElement first;

    @FindBy(id = "form:ajaxSubmit")
    private WebElement ajaxSubmit;

    @FindBy(id = "form:nonAjaxSubmit")
    private WebElement nonAjaxSubmit;

    /**
     * When a c:forEach over a list shares its parent with components or template text, then an ajax postback must
     * render them all in document order, as the initial request did.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6166">issue 6166</a>
     */
    @Test
    void testAjaxPostbackKeepsDocumentOrder() {
        open("issue6166.xhtml");
        assertDocumentOrder();

        guardAjax(ajaxSubmit::click);
        assertDocumentOrder();

        guardAjax(ajaxSubmit::click);
        assertDocumentOrder();
    }

    /**
     * When a c:forEach over a list shares its parent with components or template text, then a non-ajax postback must
     * render them all in document order, as the initial request did.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6166">issue 6166</a>
     */
    @Test
    void testNonAjaxPostbackKeepsDocumentOrder() {
        open("issue6166.xhtml");
        assertDocumentOrder();

        guardHttp(nonAjaxSubmit::click);
        assertDocumentOrder();

        guardHttp(nonAjaxSubmit::click);
        assertDocumentOrder();
    }

    private void assertDocumentOrder() {
        assertEquals(COMPONENTS, withoutWhitespace(components.getText()));
        assertEquals(TEXT, withoutWhitespace(text.getText()));
        assertEquals(ONLY, withoutWhitespace(only.getText()));
        assertEquals(FIRST, withoutWhitespace(first.getText()));
    }

    private static String withoutWhitespace(String string) {
        return string.replaceAll("\\s+", "");
    }
}
