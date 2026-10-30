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
package org.eclipse.mojarra.test.issue6087;

import static org.eclipse.mojarra.test.issue6087.Issue6087SessionServlet.ACTIVE_VIEW_MAPS_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class Issue6087IT extends BaseIT {

    /** Must match com.sun.faces.numberOfActiveViewMaps in web.xml. */
    private static final int NUMBER_OF_ACTIVE_VIEW_MAPS = 3;

    @FindBy(id = "created")
    private WebElement created;

    @FindBy(id = "destroyed")
    private WebElement destroyed;

    /**
     * When the max active view maps of the session is raised after the first view map was created, then each evicted
     * view map must still have its @ViewScoped beans destroyed.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6087">issue 6087</a>
     */
    @Test
    void testRaisedActiveViewMapsSizeDestroysAllViewScopedBeans() {
        assertAllViewScopedBeansDestroyed(1, NUMBER_OF_ACTIVE_VIEW_MAPS * 2, NUMBER_OF_ACTIVE_VIEW_MAPS * 3);
    }

    /**
     * When the max active view maps of the session is lowered below the current number of view maps, then each evicted
     * view map must still have its @ViewScoped beans destroyed.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6087">issue 6087</a>
     */
    @Test
    void testLoweredActiveViewMapsSizeDestroysAllViewScopedBeans() {
        assertAllViewScopedBeansDestroyed(NUMBER_OF_ACTIVE_VIEW_MAPS, 1, NUMBER_OF_ACTIVE_VIEW_MAPS);
    }

    private void assertAllViewScopedBeansDestroyed(int viewsBefore, int activeViewMapsSize, int viewsAfter) {
        open("session");
        open("counter.xhtml");
        int createdBefore = Integer.parseInt(created.getText());
        int destroyedBefore = Integer.parseInt(destroyed.getText());

        openViews(viewsBefore);
        open("session?" + ACTIVE_VIEW_MAPS_SIZE + "=" + activeViewMapsSize);
        openViews(viewsAfter);
        open("session");

        open("counter.xhtml");
        assertEquals(viewsBefore + viewsAfter, Integer.parseInt(created.getText()) - createdBefore, "created");
        assertEquals(viewsBefore + viewsAfter, Integer.parseInt(destroyed.getText()) - destroyedBefore, "destroyed");
    }

    private void openViews(int count) {
        for (int i = 0; i < count; i++) {
            open("issue6087.xhtml");
        }
    }
}
