/*
 * Copyright (c) 2026 Contributors to Eclipse Foundation.
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

package org.glassfish.mojarra.test.impl.issue5958;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import ee.jakarta.tck.faces.util.selenium.BaseITNG;
import ee.jakarta.tck.faces.util.selenium.WebPage;

/**
 * A child added from a {@code postAddToView} listener is produced again by the view build of every request, so the
 * saved state must not carry it: no dynamic action is recorded, and the component is left to the build. This holds
 * wherever the listener is attached, since what makes the add repeatable is the phase it runs in, not its source.
 *
 * @see org.glassfish.mojarra.context.StateContext
 */
class Issue5958IT extends BaseITNG {

    private static final By CHILD = By.className("dynamic-child");
    private static final String SUBMITTED_VALUE = "submitted into a dynamically added input";

    @Test
    void listenerOnViewRoot() {
        assertBuildTimeAddIsNotSaved("dynamic-child-in-view-root.xhtml");
    }

    @Test
    void listenerOnContainer() {
        assertBuildTimeAddIsNotSaved("dynamic-child-in-container.xhtml");
    }

    private void assertBuildTimeAddIsNotSaved(String viewId) {
        WebPage page = getPage(viewId);
        assertEquals(1, page.findElements(CHILD).size(), "one added child on initial render");
        assertEquals("0", page.findElement(By.id("dynamicActionCount")).getText(), "dynamic actions on initial render");

        page.findElement(CHILD).sendKeys(SUBMITTED_VALUE);
        page.guardHttp(page.findElement(By.id("form:submit"))::click);

        assertEquals(1, page.findElements(CHILD).size(), "one added child after postback");
        assertEquals(SUBMITTED_VALUE, page.findElement(By.id("echo")).getText(), "value decoded from the added child");
        assertEquals("0", page.findElement(By.id("dynamicActionCount")).getText(), "dynamic actions after postback");
    }
}
