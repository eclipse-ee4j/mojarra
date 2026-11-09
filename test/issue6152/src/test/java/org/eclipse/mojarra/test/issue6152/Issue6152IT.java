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
package org.eclipse.mojarra.test.issue6152;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

class Issue6152IT extends BaseIT {

    /** Must match jakarta.faces.NUMBER_OF_CLIENT_WINDOWS in web.xml. */
    private static final int NUMBER_OF_CLIENT_WINDOWS = 3;

    @FindBy(id = "created")
    private WebElement created;

    @FindBy(id = "destroyed")
    private WebElement destroyed;

    @FindBy(id = "dependencyDestroyed")
    private WebElement dependencyDestroyed;

    private int createdBefore;
    private int destroyedBefore;
    private int dependencyDestroyedBefore;

    /**
     * A client window evicted from the session must have its @ClientWindowScoped beans and their dependent objects
     * destroyed, and only those of the evicted client windows.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6152">issue 6152</a>
     */
    @Test
    void testEvictedClientWindowDestroysBeans() {
        int evicted = 2;
        startFreshSession();

        openClientWindows(NUMBER_OF_CLIENT_WINDOWS + evicted);

        assertCounters(NUMBER_OF_CLIENT_WINDOWS + evicted, evicted);
    }

    /**
     * A destroyed session must have the @ClientWindowScoped beans of all its client windows and their dependent objects
     * destroyed.
     *
     * @see <a href="https://github.com/eclipse-ee4j/mojarra/issues/6152">issue 6152</a>
     */
    @Test
    void testDestroyedSessionDestroysBeans() {
        int clientWindows = NUMBER_OF_CLIENT_WINDOWS - 1;
        startFreshSession();

        openClientWindows(clientWindows);
        open("invalidate");

        assertCounters(clientWindows, clientWindows);
    }

    private void startFreshSession() {
        open("invalidate");
        open("counter.xhtml");
        createdBefore = Integer.parseInt(created.getText());
        destroyedBefore = Integer.parseInt(destroyed.getText());
        dependencyDestroyedBefore = Integer.parseInt(dependencyDestroyed.getText());
    }

    private void openClientWindows(int count) {
        for (int i = 0; i < count; i++) {
            open("issue6152.xhtml");
        }
    }

    private void assertCounters(int expectedCreated, int expectedDestroyed) {
        open("counter.xhtml");
        assertEquals(expectedCreated, Integer.parseInt(created.getText()) - createdBefore, "created");
        assertEquals(expectedDestroyed, Integer.parseInt(destroyed.getText()) - destroyedBefore, "destroyed");
        assertEquals(expectedDestroyed, Integer.parseInt(dependencyDestroyed.getText()) - dependencyDestroyedBefore, "dependencyDestroyed");
    }
}
