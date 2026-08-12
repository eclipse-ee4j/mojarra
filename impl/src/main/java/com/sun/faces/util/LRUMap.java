/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.faces.util;

import static com.sun.faces.util.Util.calculateMapCapacity;

import java.io.Serial;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A special implementation of {@link java.util.LinkedHashMap} to provide LRU functionality.
 */
public class LRUMap<K, V> extends LinkedHashMap<K, V> {

    @Serial
    private static final long serialVersionUID = -7137951139094651602L;

    private final int maxCapacity;

    // ------------------------------------------------------------ Constructors

    /**
     * Create a {@link LRUMap} with the passed maxCapacity
     */
    public LRUMap(int maxCapacity) {
        // 1) to avoid collisions we should keep the default load factor, which is 0.75f
        // 2) to avoid the map rehash when inserting the maxCapacity+1 element before removing the eldest one, we use maxCapacity+1
        super(calculateMapCapacity(maxCapacity+1) , 0.75f, true);

        // 3) we want exactly no more than maxCapacity elements inside the Map
        this.maxCapacity = maxCapacity;
    }

    // ---------------------------------------------- Methods from LinkedHashMap

    @Override
    protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
        return size() > maxCapacity;
    }

    // TEST: com.sun.faces.TestLRUMap_local
}
