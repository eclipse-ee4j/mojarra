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

import java.util.Arrays;
import java.util.List;
import java.util.Collections;

import junit.framework.TestCase;

/**
 * Validate LRU functionality of LRUMap
 */
public class TestLRUMap_local extends TestCase {

    // ------------------------------------------------------------ Constructors
    public TestLRUMap_local() {
        super("TestLRUMap_local");
    }

    public TestLRUMap_local(String name) {
        super(name);
    }

    // ------------------------------------------------------------ Test Methods
    /**
     * Ensure that LRUMap works as advertised.
     */
    public void testLRUMap() {

        LRUMap<String, String> map = new LRUMap<String, String>(5);
        map.put("one", "one");
        map.put("two", "two");
        map.put("three", "three");

        // order should be "three", "two", "one"
        String[] control = {
            "three", "two", "one"
        };

        int count = 3;
        display(control.clone(), map);
        for (String s : map.keySet()) {
            assertEquals(control[--count], s);
        }

        map.put("four", "four");
        map.put("five", "five");
        map.put("three", "three");
        map.put("six", "six");
        control = new String[]{
            "six", "three", "five", "four", "two"
        };
        count = 5;
        display(control.clone(), map);
        for (String s : map.keySet()) {
            assertEquals(control[--count], s);
        }
    }

    // --------------------------------------------------------- Private Methods
    private static void display(String[] expected, LRUMap<String, String> actual) {
        System.out.println("Expected order:");
        List<String> revControl = Arrays.asList(expected);
        Collections.reverse(revControl);
        for (String s : revControl) {
            System.out.print(s + ' ');
        }
        System.out.println('\n');
        System.out.println("Actual order:");
        for (String s : actual.keySet()) {
            System.out.print(s + ' ');
        }
        System.out.println();
    }
}
