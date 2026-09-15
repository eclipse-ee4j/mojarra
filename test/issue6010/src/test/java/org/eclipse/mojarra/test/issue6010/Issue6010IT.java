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
package org.eclipse.mojarra.test.issue6010;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * A composite component may aim a {@code cc:clientBehavior} at another composite component, which aims its own
 * {@code cc:clientBehavior} at a {@code ClientBehaviorHolder} of its implementation. The behavior the page attaches to
 * the outermost composite component must travel down that chain and end up on that holder, under whichever event name
 * each step along the way declares.
 */
class Issue6010IT extends BaseIT {

    @FindBy(id = "form:outer:inner:label")
    private WebElement label;

    @FindBy(id = "form:count")
    private WebElement count;

    /**
     * The view must build, which it only does when every step of the chain finds a holder to hand the behavior to, and
     * clicking the holder of the innermost composite component must run the behavior, on the first request as well as
     * on the postbacks rebuilding the view.
     */
    @Test
    void testNestedCompositeComponentClientBehavior() {
        open("index.xhtml");
        assertEquals("index", getPageTitle(), getPageSource());
        assertEquals("0", count.getText());

        guardAjax(label::click);
        assertEquals("1", count.getText(), getPageSource());

        guardAjax(label::click);
        assertEquals("2", count.getText(), getPageSource());
    }
}
