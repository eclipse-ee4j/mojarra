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
package org.eclipse.mojarra.test.issue5741;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Objects;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;

/**
 * Mojarra CSP is disabled by default. Under certain scenarios (i.e., exact mapping), the wrong JS event handler might
 * still be rendered, resulting in an error on the page (and a failed TCK run in some environments). This test verifies
 * that, with CSP disabled, which is the default setting, the onclick handler is correctly rendered. Verifying that
 * applications with CSP enabled continue to work as expected is not the scope of this test.
 */
public class Issue5741IT extends BaseIT {

    @Test
    public void testCommandLink() {
        testComponent("commandLink");
    }

    @Test
    public void testCommandButton() {
        testComponent("commandButton");
    }

    @Test
    public void testOutcomeButton() {
        assertResults("outcomeButton.xhtml");
        assertResults("outcomeButton");
    }

    protected void testComponent(String base) {
        for (var ext : List.of(".xhtml", "Ajax.xhtml", "", "Ajax")) {
            assertResults(base + ext);
        }
    }

    protected void assertResults(String page) {
        open(page);
        assertTrue(getNonce().isEmpty(), "For '" + page + "', nonce should be missing for CSP disabled");
        assertFalse(Objects.requireNonNull(browser.getPageSource()).contains("ael"),
                "For '" + page + "', source should not contain 'ael' for CSP disabled");
    }
}
