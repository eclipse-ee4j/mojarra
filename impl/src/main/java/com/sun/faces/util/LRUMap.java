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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A special implementation of {@link java.util.LinkedHashMap} to provide LRU functionality.
 */
public class LRUMap<K, V> extends LinkedHashMap<K, V> {

    private static final long serialVersionUID = -7137951139094651602L;
    private final int maxCapacity;

    // ------------------------------------------------------------ Constructors

    public LRUMap(int maxCapacity) {
        super(maxCapacity, 1.0f, true);
        this.maxCapacity = maxCapacity;
    }

    // ---------------------------------------------- Methods from LinkedHashMap

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > maxCapacity;
    }

    // TEST: com.sun.faces.TestLRUMap_local
}
