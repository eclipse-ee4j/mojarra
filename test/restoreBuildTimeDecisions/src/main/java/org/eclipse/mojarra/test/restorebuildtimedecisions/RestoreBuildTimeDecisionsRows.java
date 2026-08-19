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
package org.eclipse.mojarra.test.restorebuildtimedecisions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

/**
 * The rows an iteration is rendered over, as a list, a map keyed by string, a map keyed by the very type an index has,
 * and a set, and the empty list it iterates instead once the build time condition which selects between the two no
 * longer holds, so that the rows the response was rendered from outlive the items the restore iterates.
 */
@Named
@SessionScoped
public class RestoreBuildTimeDecisionsRows implements Serializable {

    private static final long serialVersionUID = 1L;

    public static class Row implements Serializable {

        private static final long serialVersionUID = 1L;

        private String name;

        public Row(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final List<Row> items = new ArrayList<>(List.of(new Row("a"), new Row("b")));

    private final Map<String, String> map = entries();

    private final Map<Integer, String> numbers = numbers();

    private final Map<String, String> fullMap = entries();

    private final Map<String, String> partialMap = new LinkedHashMap<>(Map.of("first", "a"));

    private final Set<Row> set = new LinkedHashSet<>(List.of(new Row("a"), new Row("b"), new Row("c")));

    private static Map<String, String> entries() {
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("first", "a");
        entries.put("second", "b");
        return entries;
    }

    private static Map<Integer, String> numbers() {
        Map<Integer, String> numbers = new LinkedHashMap<>();
        numbers.put(1, "a");
        numbers.put(2, "b");
        return numbers;
    }

    public List<Row> getItems() {
        return items;
    }

    public List<Row> getNone() {
        return List.of();
    }

    public Map<String, String> getMap() {
        return map;
    }

    public Map<String, String> getNoMap() {
        return null;
    }

    public Map<String, String> getFullMap() {
        return fullMap;
    }

    public Map<String, String> getPartialMap() {
        return partialMap;
    }

    public Map<Integer, String> getNumbers() {
        return numbers;
    }

    public Set<Row> getSet() {
        return set;
    }

    public String getNames() {
        return items.stream().map(Row::getName).reduce((first, second) -> first + "," + second).orElse("");
    }

    public String getSetNames() {
        return set.stream().map(Row::getName).reduce((first, second) -> first + "," + second).orElse("");
    }
}
