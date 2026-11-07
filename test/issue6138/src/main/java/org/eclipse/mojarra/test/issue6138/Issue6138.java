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
package org.eclipse.mojarra.test.issue6138;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named
@RequestScoped
public class Issue6138 {

    private final List<List<Cell>> nested = List.of(cells(2), cells(2));
    private final List<List<List<Cell>>> nestedTwice = List.of(List.of(cells(2), cells(2)), List.of(cells(2), cells(2)));
    private final List<List<Cell>> wide = List.of(cells(3), cells(3));
    private final List<List<List<Cell>>> wideTwice = List.of(List.of(cells(3), cells(3)), List.of(cells(3), cells(3)));
    private final Map<String, Object> validated = new TreeMap<>();

    public List<List<Cell>> getNested() {
        return nested;
    }

    public List<List<List<Cell>>> getNestedTwice() {
        return nestedTwice;
    }

    public List<List<Cell>> getWide() {
        return wide;
    }

    public List<List<List<Cell>>> getWideTwice() {
        return wideTwice;
    }

    public Map<String, Object> getValidated() {
        return validated;
    }

    private static List<Cell> cells(int count) {
        List<Cell> cells = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cells.add(new Cell());
        }
        return cells;
    }

    public static class Cell {

        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
