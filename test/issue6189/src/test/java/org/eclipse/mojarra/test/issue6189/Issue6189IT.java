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
package org.eclipse.mojarra.test.issue6189;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

/**
 * The view rendered by a postback reflects the build time decisions as they stand at render time, also when the
 * invoked action builds another view in the same request.
 *
 * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6189">issue 6189</a>
 */
class Issue6189IT extends BaseIT {

    /**
     * The include follows the step after each ajax postback which advances it.
     */
    @Test
    void testIncludeFollowsStep() {
        assertIncludeFollowsStep("form:next");
    }

    /**
     * The include follows the step after each ajax postback which advances it and builds another view.
     */
    @Test
    void testIncludeFollowsStepWhenActionBuildsOtherView() {
        assertIncludeFollowsStep("form:nextAndBuildOtherView");
    }

    private void assertIncludeFollowsStep(String button) {
        open("include.xhtml");
        assertStep("step 1");

        guardAjax(browser.findElement(By.id(button))::click);
        assertStep("step 2");

        guardAjax(browser.findElement(By.id(button))::click);
        assertStep("step 3");
    }

    private void assertStep(String expected) {
        assertEquals(expected, browser.findElement(By.id("form:step")).getText(), "EL");
        assertEquals(expected, browser.findElement(By.id("form:include")).getText(), "include");
    }
}
