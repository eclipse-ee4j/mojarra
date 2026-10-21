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

package org.eclipse.mojarra.test.issue6078;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class Issue6078IT extends BaseIT {

    @FindBy(id = "pageTitle")
    private WebElement pageTitle;

    @FindBy(id = "boxTitle")
    private WebElement boxTitle;

    @FindBy(id = "footerTitle")
    private WebElement footerTitle;

    /**
     * A variable which the template of a {@code ui:decorate} sets must not replace the variable of the same name which
     * the page set before, neither inside the decoration nor after it.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6078">issue 6078</a>
     */
    @Test
    void testDecorateKeepsTheVariablesOfItsTemplate() {
        assertTitles("issue6078.xhtml");
    }

    /**
     * As {@link #testDecorateKeepsTheVariablesOfItsTemplate()}, with a {@code ui:param} on the {@code ui:decorate}. The
     * variables of a decoration are kept within it with and without a {@code ui:param}.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6078">issue 6078</a>
     */
    @Test
    void testDecorateWithParamKeepsTheVariablesOfItsTemplate() {
        assertTitles("issue6078Param.xhtml");
    }

    /**
     * A variable which the {@code ui:define} content of a {@code ui:decorate} sets must not replace the variable of the
     * same name which the page set before, neither inside the decoration nor after it.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6078">issue 6078</a>
     */
    @Test
    void testDecorateKeepsTheVariablesOfItsDefine() {
        assertTitles("issue6078Define.xhtml");
    }

    private void assertTitles(String page) {
        open(page);
        assertEquals("My page", pageTitle.getText(), "page title before the decoration");
        assertEquals("Box", boxTitle.getText(), "title inside the decoration");
        assertEquals("My page", footerTitle.getText(), "page title after the decoration");
    }
}
