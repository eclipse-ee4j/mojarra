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

package com.sun.faces.facelets.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

/**
 * A view build hands back ids from this cache instead of building the strings again, so the cache has to keep an entry
 * for as long as the Facelet lives, keep the prefixes apart -- the same tag generates a different id under each -- and
 * stop growing at some point, since a Facelet included from inside an iteration produces a prefix per iteration.
 */
class FirstIdCacheTest {

    @Test
    void idsStayPutForTheSamePrefix() {
        FirstIdCache cache = new FirstIdCache(() -> 3);

        String[] ids = cache.ids("p");
        ids[1] = "p_form";

        assertSame(ids, cache.ids("p"), "a later build reaches the ids an earlier one cached");
        assertEquals(3, ids.length, "sized to the slots handed out");
    }

    @Test
    void everyPrefixCachesSeparately() {
        FirstIdCache cache = new FirstIdCache(() -> 2);

        cache.ids("p")[0] = "p_form";
        cache.ids("q")[0] = "q_form";

        assertEquals("p_form", cache.ids("p")[0]);
        assertEquals("q_form", cache.ids("q")[0]);
    }

    @Test
    void growingKeepsWhatWasCached() {
        int[] slots = { 2 };
        FirstIdCache cache = new FirstIdCache(() -> slots[0]);

        String[] ids = cache.ids("p");
        ids[0] = "p_form";

        slots[0] = 4;
        String[] grown = cache.grow("p", ids);

        assertArrayEquals(new String[] { "p_form", null, null, null }, grown);
        assertSame(grown, cache.ids("p"), "and the grown array is what the next build gets");
    }

    @Test
    void cachingStopsAtThePrefixLimit() {
        FirstIdCache cache = new FirstIdCache(() -> 1);

        for (int i = 0; i < 64; i++) {
            assertNotNull(cache.ids("p" + i), "prefix " + i + " is within the limit");
        }

        assertNull(cache.ids("p64"), "a further prefix is not cached at all");
        assertNotNull(cache.ids("p0"), "while the prefixes already held keep working");
    }
}
