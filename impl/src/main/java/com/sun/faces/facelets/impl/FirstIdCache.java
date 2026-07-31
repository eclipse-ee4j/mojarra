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

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntSupplier;

/**
 * Holds, per id prefix, the first id each of a Facelet's tags generates within one view build, indexed by the tag's
 * counter slot. Every build of a given view generates the same id for a given tag under a given prefix, so a build can
 * hand back the id it handed back last time instead of building the same string again. Only a tag's <em>first</em> id
 * is worth holding: a tag that generates more than one is being iterated over, where the number of ids follows the
 * data rather than the view.
 *
 * <p>Entries are filled in by whichever build reaches a slot first and are never invalidated, because the id is a pure
 * function of the prefix and the tag. Concurrent builds therefore need no locking, and every race is benign: two builds
 * filling the same slot compute the same string, and a build that grows an array while another holds the pre-growth one
 * loses cached entries but never returns a wrong id, since the loser simply rebuilds the string it could not find.
 */
final class FirstIdCache {

    /**
     * How many prefixes to hold arrays for. A prefix identifies one occurrence of a Facelet in one view's include
     * hierarchy, so the count is bounded by view structure -- except where a view includes the Facelet from inside an
     * iteration, which is bounded by data instead. Stop caching there rather than grow without limit.
     *
     * <p>What this bounds is the number of arrays, so the strings a Facelet can retain for its lifetime is this times
     * its tag count: cheap for the ordinary Facelet included once or twice, and still bounded by the view for one
     * written with thousands of tags and included from as many places.
     */
    private static final int MAX_PREFIXES = 64;

    private final Map<String, String[]> idsByPrefix = new ConcurrentHashMap<>();

    /** How many counter slots the owning Facelet has handed out, which grows as its tags are first applied. */
    private final IntSupplier slotCount;

    FirstIdCache(IntSupplier slotCount) {
        this.slotCount = slotCount;
    }

    /**
     * Returns the first ids generated under {@code prefix}, indexed by counter slot, or {@code null} when this cache
     * already holds as many prefixes as it will. The array is sized to the slot count at the time it is created, so a
     * caller reaching a slot beyond its end grows it through {@link #grow(String, String[])}.
     *
     * @param prefix the id prefix the calling build is generating under
     * @return the per-slot first ids, or {@code null} when at the prefix limit
     */
    String[] ids(String prefix) {
        String[] ids = idsByPrefix.get(prefix);

        if (ids == null) {
            if (idsByPrefix.size() >= MAX_PREFIXES) {
                return null;
            }
            ids = idsByPrefix.computeIfAbsent(prefix, key -> new String[slotCount.getAsInt()]);
        }

        return ids;
    }

    /**
     * Returns {@code prefix}'s ids resized to hold every slot handed out so far, for when tags reserved more slots
     * after the array was sized -- which a Facelet applied for the first time does as its tags are reached.
     *
     * @param prefix the id prefix the calling build is generating under
     * @param ids the array to grow
     * @return the grown array
     */
    String[] grow(String prefix, String[] ids) {
        String[] grown = Arrays.copyOf(ids, slotCount.getAsInt());
        idsByPrefix.put(prefix, grown);
        return grown;
    }
}
