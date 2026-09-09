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
package org.eclipse.mojarra.test.issue6001;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.mojarra.test.base.BaseIT;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

/**
 * A tag generates its mark id from the number of times it has been applied so far in the current build, so a fragment
 * applied more often than it was during the build before it shifts the mark id of every tag it holds and none of them
 * finds back the component it built. Where a {@code binding} then hands such a tag the component of that earlier
 * build, the component arrives with the children of that build attached and must have them cleaned up like a
 * component found back under its parent, or its tags build a second set beside the first.
 */
class Issue6001IT extends BaseIT {

    @FindBy(id = "form:toggle")
    private WebElement toggle;

    /**
     * Toggling slot 0 from text to table applies the fragment twice where the build before it applied it once, so the
     * tags of slot 1 all shift and the bound component of slot 1 arrives through its binding. Each slot must hold
     * exactly one child.
     */
    @Test
    void testFragmentAppliedMoreOftenThanBefore() {
        open("index.xhtml");
        assertEquals(0, getComponentCount("form:child_slot0"));
        assertEquals(1, getComponentCount("form:child_slot1"));

        guardHttp(toggle::click);
        assertEquals(1, getComponentCount("form:child_slot0"), getPageSource());
        assertEquals(1, getComponentCount("form:child_slot1"), getPageSource());
    }

    /**
     * Toggling back applies the fragment once where the build before it applied it twice, which every tag still
     * matches, and toggling once more applies it twice again with the binding of slot 1 filled by a build which
     * itself reused it.
     */
    @Test
    void testFragmentAppliedRepeatedly() {
        open("index.xhtml");
        guardHttp(toggle::click);
        guardHttp(toggle::click);
        assertEquals(0, getComponentCount("form:child_slot0"), getPageSource());
        assertEquals(1, getComponentCount("form:child_slot1"), getPageSource());

        guardHttp(toggle::click);
        assertEquals(1, getComponentCount("form:child_slot0"), getPageSource());
        assertEquals(1, getComponentCount("form:child_slot1"), getPageSource());
    }

    /**
     * A composite component reached through a binding arrives holding the implementation its earlier build applied,
     * which lives in a facet of its own, and must be cleaned up as the children of any other component are.
     */
    @Test
    void testCompositeComponentFragmentAppliedMoreOftenThanBefore() {
        open("composite.xhtml");
        assertEquals(0, getComponentCount("form:child_slot0:text"));
        assertEquals(1, getComponentCount("form:child_slot1:text"));

        guardHttp(toggle::click);
        assertEquals(1, getComponentCount("form:child_slot0:text"), getPageSource());
        assertEquals(1, getComponentCount("form:child_slot1:text"), getPageSource());
    }

    private int getComponentCount(String clientId) {
        return browser.findElements(By.id(clientId)).size();
    }
}
